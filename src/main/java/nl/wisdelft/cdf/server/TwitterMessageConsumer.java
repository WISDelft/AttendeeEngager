/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.util.Date;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import nl.wisdelft.cdf.client.shared.EngagementStatus;
import nl.wisdelft.cdf.client.shared.TwitterMessage;
import nl.wisdelft.cdf.client.shared.TwitterUser;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import twitter4j.TwitterException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * @author Jasper Oosterman
 * @created Mar 7, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Singleton
@Startup
public class TwitterMessageConsumer {
	@Inject
	private Logger logger;
	@Inject
	private Utility utility;
	@Inject
	private TwitterUserService userService;
	private Connection connection;
	private Session session;
	private MessageConsumer consumerSlow, consumerFast;
	@Inject
	private TwitterMessageService messageService;
	private final String MODULE_ACTIVE = "module_active_messageConsumer";
	private final String MODULE_ACTIVE_FAST = "module_active_messageConsumerFast";

	@Inject
	private TwitterConnection twitter;

	@Inject
	TwitterMessageProducer producer;

	private Gson gson = new Gson();

	@PostConstruct
	public void initializeAndStart() throws JMSException {
		// check from the configuration if this module is active
		if (!isActiveSlowQueue() && !isActiveFastQueue()) {
			logger.info(TwitterMessageConsumer.class.getSimpleName() + " not started. Both FAST and SLOW Disabled in config.");
			return;
		}
		logger.info(TwitterMessageConsumer.class.getSimpleName() + " starting...");
		initialize();
	}

	/**
	 * Closes the allocated resources and stops the thread
	 */
	@PreDestroy
	public void destroy() {
		try {
			if (connection != null) connection.close();
		}
		catch (JMSException ex) {
			ex.printStackTrace();
		}
		logger.info(TwitterMessageConsumer.class.getSimpleName() + " stopped.");
	}

	private boolean isActiveSlowQueue() {
		return utility.getPropertyAsBoolean(MODULE_ACTIVE);
	}

	private boolean isActiveFastQueue() {
		return utility.getPropertyAsBoolean(MODULE_ACTIVE_FAST);
	}

	private void initialize() throws JMSException {
		// Getting JMS connection from the server ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(utility.getPropertyAsString("queueConnection"));
		connection = connectionFactory.createConnection();
		connection.start();
		// Creating session for getting messages
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// Getting the input queue
		Destination queueTwitterMessagesSlow = session.createQueue(utility.getPropertyAsString("queueTwitterMessages"));
		Destination queueTwitterMessagesFast = session.createQueue(utility.getPropertyAsString("queueTwitterMessagesFast"));
		// MessageConsumer is used for receiving (consuming) messages
		consumerSlow = session.createConsumer(queueTwitterMessagesSlow);
		consumerFast = session.createConsumer(queueTwitterMessagesFast);
	}

	/**
	 * Slow QUEUE message sending: every 10 minutes
	 * 
	 * @throws JMSException
	 */
	@Schedule(hour = "*", minute = "0/10", persistent = false)
	public void getMessageFromSlowQueueAndSend() throws JMSException {
		if (!isActiveSlowQueue()) {
			return;
		}

		Message m = consumerSlow.receiveNoWait();
		String json = null;
		TwitterMessage twitterMessage = null;
		if (m != null && m instanceof TextMessage) {
			logger.info("SLOW queue: Received a TwitterMessage for sending");
			try {
				json = ((TextMessage) m).getText();
				twitterMessage = gson.fromJson(json, TwitterMessage.class);
				// send the message
				sendMessage(twitterMessage);
			}
			catch (JsonSyntaxException ex) {
				logger.error("Retrieved message body does not contain a valid serialized TwitterMessage object:\n" + json, ex);
				// stop processing this message and continue
			}
			catch (JMSException ex) {
				logger.error("ActiveMQ exception on receiving TwitterMessage:", ex);
			}
		}
	}

	/**
	 * Fast QUEUE message sending: every 1 minutes
	 * 
	 * @throws JMSException
	 */
	@Schedule(hour = "*", minute = "0/1", persistent = false)
	public void getMessageFromFastQueueAndSend() throws JMSException {
		if (!isActiveFastQueue()) {
			return;
		}

		Message m = consumerFast.receiveNoWait();
		String json = null;
		TwitterMessage twitterMessage = null;
		if (m != null && m instanceof TextMessage) {
			logger.info("FAST queue: Received a TwitterMessage for sending");
			try {
				json = ((TextMessage) m).getText();
				twitterMessage = gson.fromJson(json, TwitterMessage.class);
				// send the message
				sendMessage(twitterMessage);
			}
			catch (JsonSyntaxException ex) {
				logger.error("Retrieved message body does not contain a valid serialized TwitterMessage object:\n" + json, ex);
				// stop processing this message and continue
			}
			catch (JMSException ex) {
				logger.error("ActiveMQ exception on receiving TwitterMessage:", ex);
			}
		}
	}

	/**
	 * Unsuccessful send. This might mean that 1) the user does not exist, or 2)
	 * that we are not allowed to send him messages or 3) there was a twitter
	 * problem. Increase the retryCount of the message and update it. The message
	 * is not resend.
	 * 
	 * @param message
	 * @throws JMSException
	 */
	private void handleUnsuccessfulSend(TwitterMessage message) {
		message.increaseRetryCount();
		// update the message in the DB that it was not sent succesful
		messageService.update(message.getId(), message);
	}

	private void handleSuccessfulSend(TwitterMessage message) {
		// message successfully sent
		// update the message
		Date dateSend = new Date();
		message.setDateSend(dateSend);
		messageService.update(message.getId(), message);
		// Refresh the user from the datastore and update the user
		TwitterUser user = userService.getById(message.getUser().getId());
		user.setDateLastContacted(dateSend);
		EngagementStatus statusAfterSend = message.getUserEngagementStatusOnSuccessfulSend();
		if (statusAfterSend != null) {
			user.setEngagementStatus(statusAfterSend);
		}
		userService.update(user.getId(), user);
	}

	/**
	 * Sends the message using Twitter
	 * 
	 * @param message
	 * @return whether the message was immediately sent.
	 */
	protected void sendMessage(TwitterMessage message) {
		// check if we can send the message
		if (!isAllowedToBeSend(message)) {
			// message is not allowed to be send
			return;
		}

		// we are allowed to send the message
		// send the message
		try {
			twitter.send(message);
			// message was send succesful
			logger.info("Message was sent successful");
			handleSuccessfulSend(message);
		}
		catch (TwitterException ex) {
			handleUnsuccessfulSend(message);
			// A twitter exception caused the message to not be send. Handle
			// specific errors
			boolean requeueMessage = false;
			if (ex.exceededRateLimitation()) {
				logger.warn("Error sending message: Rate limit. Requeing.");
				requeueMessage = true;
			}
			else if (ex.isCausedByNetworkIssue()) {
				logger.warn("Error sending message: Network error. Requeing.");
				requeueMessage = true;
			}
			else {
				logger.error("Error sending message: Twitter error. Message lost.", ex);
			}
			if (requeueMessage) {
				// put the message in the queue such that it will be retried for
				// sending
				producer.queueMessageForSending(message);
			}
		}
	}

	/**
	 * Returns whether the message can (user!=null) and is allowed
	 * (Status==ENGAGED) to be send
	 * 
	 * @param message
	 * @param user
	 * @return
	 */
	public boolean isAllowedToBeSend(TwitterMessage message) {
		// if the user is unknown, we cannot send the message
		if (message.getUser() == null) {
			logger.info("Allowed to send? No. User is <null>.");
			return false;
		}
		// if the the message overrides the check we can send the message
		if (message.isOverrideAllowedToSendCheck()) {
			logger.info("Allowed to send? Yes. Check overridden.");
			return true;
		}
		// else look if the status is ENGAGED
		if (message.getUser().getEngagementStatus() == EngagementStatus.OPTED_IN) {
			logger.info("Allowed to send? Yes. User is ENGAGED.");
			return true;
		}
		else {
			logger.info("Allowed to send? No. User is not ENGAGED.");
			return false;
		}

	}
}
