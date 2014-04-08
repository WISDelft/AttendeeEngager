/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import nl.wisdelft.cdf.client.shared.EngagementStatus;
import nl.wisdelft.cdf.client.shared.Recommendation;
import nl.wisdelft.cdf.client.shared.TwitterMessage;
import nl.wisdelft.cdf.client.shared.TwitterUser;
import nl.wisdelft.cdf.client.shared.Venue;
import nl.wisdelft.cdf.server.status.Created;
import nl.wisdelft.cdf.server.status.Engaged;
import nl.wisdelft.cdf.server.status.NotResponded;
import nl.wisdelft.cdf.server.status.OptedOut;
import nl.wisdelft.cdf.server.status.Sent;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import com.google.gson.Gson;

/**
 * @author Jasper Oosterman
 * @created Feb 25, 2014
 * @organization: Delft University of Technology - Web Information Systems
 */
@Singleton
@Startup
public class TwitterMessageProducer {
	@Inject
	protected TwitterMessageService messageService;

	@Inject
	protected TwitterUserService userService;

	@Inject
	@Sent
	private Event<TwitterMessage> messageSent;

	@Inject
	protected Utility utility;

	private Gson gson = new Gson();

	@Inject
	protected Logger logger;

	@Inject
	protected RecommendationManager recManager;

	protected Connection connection;
	protected Session session;
	protected MessageProducer producer;

	private final String MODULE_ACTIVE = "module_active_messagesProducer";

	protected Queue<TwitterMessage> localMessageQueue = new LinkedList<TwitterMessage>();

	private boolean isActive() {
		return utility.getPropertyAsBoolean(MODULE_ACTIVE);
	}

	/**
	 * Connects to the queue and sets up the producer
	 * 
	 * @throws JMSException
	 */
	@PostConstruct
	protected void initialize() throws JMSException {
		if (!isActive()) {
			logger.info(TwitterMessageProducer.class.getSimpleName() + " not started. Disabled in config.");
			return;
		}

		// Getting JMS connection from the server ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(utility.getPropertyAsString("queueConnection"));
		connection = connectionFactory.createConnection();
		connection.start();
		// Creating session for getting messages
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// Getting the input queue
		Destination queueTwitterMessages = session.createQueue(utility.getPropertyAsString("queueTwitterMessages"));
		// MessageConsumer is used for receiving (consuming) messages
		producer = session.createProducer(queueTwitterMessages);
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		logger.info(TwitterMessageProducer.class.getSimpleName() + " starting...");
	}

	/**
	 * Closes the allocated producer, session and connection.
	 */
	@PreDestroy
	protected void destroy() {
		try {
			if (connection != null) connection.close();
		}
		catch (JMSException ex) {
			ex.printStackTrace();
		}
		logger.info(TwitterMessageProducer.class.getSimpleName() + " stopped.");
	}

	/**
	 * Put the message in the queue. If there are messages in the local queue
	 * these are also send
	 * 
	 * @param message A TwitterMessage
	 */
	public void queueMessageForSending(TwitterMessage message) {
		if (!isActive()) {
			return;
		}

		if (message == null) {
			logger.warn("Tried to queue a null message. Message not queued.");
			return;
		}
		logger.info("Putting message in the queue...: " + message);
		try {
			// add the new message to the messagequeue
			localMessageQueue.add(message);
			// process the messagequeue
			TwitterMessage textMessage;
			while ((textMessage = localMessageQueue.peek()) != null) {
				String json = gson.toJson(textMessage);
				producer.send(session.createTextMessage(json));
				// successfully added, remove from local queue
				localMessageQueue.poll();
			}
		}
		catch (JMSException ex) {
			logger.warn("Message could not be queued on server (see exception). Local queue now contains " + localMessageQueue.size() + " items",
					ex);
		}
	}

	/**
	 * Method observes a new user being created. The new user is send a welcome
	 * message via a status update and its EngagementStatus is set to CONTACTED
	 * 
	 * @param newUser A newly created user with status NOT_CONTACTED
	 */
	public void processNewUser(@Observes @Created TwitterUser newUser) {
		if (!isActive()) {
			return;
		}
		logger.info("New User creation observed: " + newUser);
		if (newUser.getEngagementStatus() == EngagementStatus.NOT_CONTACTED) {
			// get the number of possible messages
			int nrMessages = utility.getPropertyAsInt("nrDifferentMessages");
			// messagenumber is the postfix for the message
			int messageNumber = (int) Math.round((nrMessages * Math.random()));
			String messagePropertyname = "welcomeMessage_" + messageNumber;

			// create unique dashboard url
			String dashboardURL = utility.getPropertyAsString("dashboardURL") + "?user=" + newUser.getId() + "&time=" + new Date().getTime();
			String welcomeMessage = String.format(utility.getPropertyAsString(messagePropertyname, newUser.getLangPreference()), dashboardURL);
			// prepare the message object
			TwitterMessage message = new TwitterMessage(newUser, welcomeMessage, false);
			// override the check
			message.setOverrideAllowedToSendCheck(true);
			// the user status should be set to CONTACTED when send
			message.setUserEngagementStatusOnSuccessfulSend(EngagementStatus.CONTACTED);
			// persist the message
			messageService.create(message);
			// send the message (overriding the allowed to send check)
			queueMessageForSending(message);
		}
	}

	/**
	 * Method observes a user who has not yet replied. The user is send a welcome
	 * message via a status update after which its EngagementStatus is set to
	 * CONTACTED_REMINDED
	 * 
	 * @param engagedUser A user with status CONTACTED
	 */
	public void processNotResponsedUser(@Observes @NotResponded TwitterUser notRespondedUser) {
		if (!isActive()) {
			return;
		}
		logger.info("Non-responding User observed: " + notRespondedUser);
		if (notRespondedUser.getEngagementStatus() == EngagementStatus.CONTACTED) {
			// get the number of possible messages
			int nrMessages = utility.getPropertyAsInt("nrDifferentMessages");
			// messagenumber is the postfix for the message
			int messageNumber = (int) Math.round((nrMessages * Math.random()));
			String messagePropertyname = "reminderMessage_" + messageNumber;

			// create unique dashboard url
			String dashboardURL = utility.getPropertyAsString("dashboardURL") + "?user=" + notRespondedUser.getId() + "&time="
					+ new Date().getTime();
			String reminderMessage = String.format(utility.getPropertyAsString(messagePropertyname, notRespondedUser.getLangPreference()),
					dashboardURL);
			// prepare the message object
			TwitterMessage message = new TwitterMessage(notRespondedUser, reminderMessage, false);
			// sets the user status to CONTACTED_REMINDED on successful send
			message.setUserEngagementStatusOnSuccessfulSend(EngagementStatus.CONTACTED_REMINDED);
			// override the check
			message.setOverrideAllowedToSendCheck(true);
			// persist the message
			messageService.create(message);
			// send the message
			queueMessageForSending(message);
		}
	}

	/**
	 * Method observes a user transitioning into ENGAGED. The user is send a
	 * message with a link to the dashboard.
	 * 
	 * @param engagedUser A user with status ENGAGED
	 */
	public void processEngagedUser(@Observes @Engaged TwitterUser engagedUser) {
		if (!isActive()) {
			return;
		}
		logger.info("Engaged User observed: " + engagedUser);
		if (engagedUser.getEngagementStatus() == EngagementStatus.OPTED_IN) {
			// prepare the messages
			String engagedTweet = utility.getPropertyAsString("engagedTweet", engagedUser.getLangPreference());
			TwitterMessage tm_engagedTweet = new TwitterMessage(engagedUser, engagedTweet, false);

			String engagedDMDashboard = utility.getPropertyAsString("engagedDMDashboard1", engagedUser.getLangPreference());
			TwitterMessage tm_engagedDMDashboard1 = new TwitterMessage(engagedUser, engagedDMDashboard, true);

			String engagedDMDashboardURL = String.format(utility.getPropertyAsString("engagedDMDashboard2", engagedUser.getLangPreference()),
					engagedUser.getDashboardPath());
			TwitterMessage tm_engagedDMDashboard2 = new TwitterMessage(engagedUser, engagedDMDashboardURL, true);

			// persist the messages
			messageService.create(tm_engagedTweet);
			messageService.create(tm_engagedDMDashboard1);
			messageService.create(tm_engagedDMDashboard2);

			// send the messages
			queueMessageForSending(tm_engagedTweet);
			queueMessageForSending(tm_engagedDMDashboard1);
			queueMessageForSending(tm_engagedDMDashboard2);
		}
	}

	/**
	 * Method observes a user transitioning into OPTED_OUT.
	 * 
	 * @param engagedUser A user with status OPTED_OUT
	 */
	public void processOptedOutUser(@Observes @OptedOut TwitterUser optedOutUser) {
		if (!isActive()) {
			return;
		}
		logger.info("Opted out User observed: " + optedOutUser);
	}

	public void processRecommendation(@Observes @Created Recommendation recommendation) {
		if (!isActive()) {
			return;
		}
		logger.info("New Recommendation observed: " + recommendation);
		// get the user to get the language preference
		TwitterUser user = recommendation.getUser();
		// prepare the message
		String template = utility.getPropertyAsString("recommendationDM", user.getLangPreference());
		Venue venue = recManager.getVenue(recommendation.getVenueID());
		String address = venue == null ? "" : venue.getAddress();
		String name = venue == null ? "" : venue.getName();

		String recommendationMessage = String.format(template, name, address);
		// prepare the message object
		TwitterMessage message = new TwitterMessage(recommendation.getUser(), recommendationMessage, true);
		// persist the message
		messageService.create(message);
		// send the message
		queueMessageForSending(message);
	}

	public void processNewRecommendationsForUser(@Observes @Sent TwitterUser user) {
		if (!isActive()) {
			return;
		}
		logger.info("New Recommendations for user observed: " + user);
		// prepare the message
		String template = utility.getPropertyAsString("recommendationTweet", user.getLangPreference());
		String dashboardURL = utility.getPropertyAsString("dashboardURL");
		String recommendationMessage = String.format(template, dashboardURL);
		// prepare the message object
		TwitterMessage message = new TwitterMessage(user, recommendationMessage, false);
		// persist the message
		messageService.create(message);
		// send the message
		queueMessageForSending(message);
	}
}