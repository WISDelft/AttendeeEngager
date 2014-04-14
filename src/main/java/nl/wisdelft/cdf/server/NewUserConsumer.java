/**
 * 
 */
package nl.wisdelft.cdf.server;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timer;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import nl.wisdelft.cdf.client.shared.TwitterUser;
import nl.wisdelft.cdf.server.status.Created;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import twitter4j.TwitterException;

/**
 * @author Jasper Oosterman
 * @created Mar 5, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Singleton
@Startup
public class NewUserConsumer implements MessageListener {
	@Inject
	Logger logger;

	@Inject
	private TwitterUserService userService;

	@Inject
	Utility utility;

	@Inject
	@Created
	Event<TwitterUser> newUserCreated;

	private Connection connection;
	private Session session;
	private MessageConsumer consumer;
	private int processedUsers = 0;

	/**
	 * Initializes and starts the receiving of messages
	 * 
	 * @throws JMSException
	 */
	@PostConstruct
	public void initializeAndStart() throws JMSException {
		// check from the configuration if this module is active
		if (!utility.getPropertyAsBoolean("module_active_processIncomingUsers")) {
			logger.info(NewUserConsumer.class.getSimpleName() + " not started. Disabled in config.");
			return;
		}
		logger.info(NewUserConsumer.class.getSimpleName() + " starting...");

	}

	/**
	 * Starts listening to messages only when the whole thing is deployed and
	 * constructed
	 * 
	 * @param timer
	 * @throws JMSException
	 */
	@Schedule(hour = "*", minute = "*", persistent = false)
	protected void init(Timer timer) throws JMSException {
		if (utility.getPropertyAsBoolean("module_active_processIncomingUsers")) {
			initialize();
			start();
		}

		timer.cancel();
	}

	/**
	 * Closes the allocated MessageConsumer, session and connection.
	 */
	@PreDestroy
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
				logger.info(NewUserConsumer.class.getSimpleName() + " stopped.");
			}
		}
		catch (JMSException ex) {
			ex.printStackTrace();
		}
	}

	public void initialize() throws JMSException {
		// Getting JMS connection from the server ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(utility.getPropertyAsString("queueConnection"));
		connection = connectionFactory.createConnection();
		connection.start();
		// Creating session for getting messages
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// Getting the input queue
		Destination queueNewUsers = session.createQueue(utility.getPropertyAsString("queueNewUsers"));
		// MessageConsumer is used for receiving (consuming) messages
		consumer = session.createConsumer(queueNewUsers);
	}

	/**
	 * Starts listening to the queue (sets listener to this)
	 * 
	 * @throws JMSException
	 */
	public void start() throws JMSException {
		consumer.setMessageListener(this);
	}

	/**
	 * Stops listening to the queue (sets listener to null)
	 * 
	 * @throws JMSException
	 */
	public void stop() throws JMSException {
		consumer.setMessageListener(null);
	}

	// Example of a listener, must catch all exceptions, you are not allowed to
	// throw
	/*
	 * (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message message) {
		try {
			// only process text messages
			if (message instanceof TextMessage) {
				TextMessage txtMessage = (TextMessage) message;
				String userScreenNameOrID = txtMessage.getText();
				logger.info("Received user via queue: " + userScreenNameOrID);
				try {
					// create the user
					TwitterUser user = userService.createFromScreenNameOrID(userScreenNameOrID);
					// User is not null only if a new user is created
					if (user != null) {
						newUserCreated.fire(user);
					}
				}
				catch (TwitterException ex) {
					logger.error("Could not get User from Twitter", ex);
					// we were rate limited. Wait for the next window and put the message
					// back in the queue
					if (ex.exceededRateLimitation()) {
						int secondsToWait = ex.getRetryAfter() + 10;
						logger.info("Waiting " + secondsToWait + " seconds for new rate limit window.");
						try {
							Thread.sleep(secondsToWait);
						}
						catch (InterruptedException e) {}
					}
				}
			}
			else {
				logger.warn("Undefined message received:\n" + message.toString());
			}

			logger.info("ProcessedUsers: " + ++processedUsers);
		}
		catch (JMSException e) {
			logger.error("JMS exeption in monitoring function onMessage, cause " + e.getMessage());
		}
	}
}
