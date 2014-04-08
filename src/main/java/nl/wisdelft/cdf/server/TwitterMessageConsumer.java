/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.util.Date;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.persistence.PersistenceException;
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
public class TwitterMessageConsumer implements MessageListener {
	@Inject
	private Logger logger;
	@Inject
	private Utility utility;
	@Inject
	private TwitterUserService userService;
	private Connection connection;
	private Session session;
	private MessageConsumer consumer;
	@Inject
	private TwitterMessageService messageService;

	@Inject
	private TwitterConnection twitter;

	@Inject
	TwitterMessageProducer producer;

	private Gson gson = new Gson();

	@PostConstruct
	public void initializeAndStart() throws JMSException {
		// check from the configuration if this module is active
		if (!utility.getPropertyAsBoolean("module_active_messageConsumer")) {
			logger.info(TwitterMessageConsumer.class.getSimpleName() + " not started. Disabled in config.");
			return;
		}
		logger.info(TwitterMessageConsumer.class.getSimpleName() + " starting...");
		initialize();
		start();
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

	private void initialize() throws JMSException {
		// Getting JMS connection from the server ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(utility.getPropertyAsString("queueConnection"));
		connection = connectionFactory.createConnection();
		connection.start();
		// Creating session for getting messages
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// Getting the input queue
		Destination queueTwitterMessages = session.createQueue(utility.getPropertyAsString("queueTwitterMessages"));
		// MessageConsumer is used for receiving (consuming) messages
		consumer = session.createConsumer(queueTwitterMessages);
	}

	public void start() throws JMSException {
		consumer.setMessageListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message m) {
		TwitterMessage twitterMessage = null;
		String json = null;
		// only process text messages
		if (m instanceof TextMessage) {
			logger.info("Recieved a TwitterMessage for sending");
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
		else {
			// stop processing this message and continue
			logger.warn("Received a non-TextMessage message: " + m);

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
		logger.info("Starting sending message to user: " + message);
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
			logger.error("Could not send message via Twitter", ex);
			handleUnsuccessfulSend(message);
			// A twitter exception caused the message to not be send. Handle
			// specific errors
			boolean requeueMessage = false;
			int sleepTimeInSeconds = 0;
			if (ex.exceededRateLimitation()) {
				logger.warn("Error sending messages: Rate limit.", ex);
				requeueMessage = true;
				sleepTimeInSeconds = ex.getRetryAfter();
			}
			if (ex.isCausedByNetworkIssue()) {
				logger.warn("Error sending messages: Network error.", ex);
				requeueMessage = true;
				sleepTimeInSeconds = 30;
			}
			if (requeueMessage) {
				// sleep for the specified time
				try {
					logger.info("MessageSender sleeping for " + sleepTimeInSeconds + " seconds.");
					Thread.sleep(sleepTimeInSeconds * 1000);
				}
				catch (InterruptedException e) {}
				// put the message in the queue such that it will be retried for
				// sending
				producer.queueMessageForSending(message);
			}
		}
		catch (PersistenceException ex) {
			logger.warn("Cannot update message/user after successful send. Retrying...", ex);
			handleSuccessfulSend(message);
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
