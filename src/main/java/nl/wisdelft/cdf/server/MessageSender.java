/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.util.Date;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import nl.wisdelft.cdf.client.shared.TwitterMessage;
import nl.wisdelft.cdf.client.shared.TwitterUser;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 * @author Jasper Oosterman
 * @created Feb 25, 2014
 * @organization: Delft University of Technology - Web Information Systems
 */
@Stateless
public class MessageSender {

	@Inject
	TwitterMessageService messageService;

	@Inject
	private QueuedTwitterMessagesService queueService;

	@Inject
	private TwitterUserService userService;

	@Inject
	@Sent
	private Event<TwitterMessage> messageSent;

	private Twitter twitter;

	/**
	 * Initializes the Twitter4J environment and prepares a connection to send the
	 * messages over.
	 * 
	 * @throws TwitterException
	 */
	public MessageSender() {
		twitter = TwitterFactory.getSingleton();
	}

	/**
	 * Creates an Status or DirectMessage and sends that. If the sending fails the
	 * message is put into the queue to be retried at a later moment.
	 * 
	 * @param message
	 */
	public void processMessage(@Observes @Created TwitterMessage message) {
		// check whether the ID or the screenname has been provided
		long id;
		try {
			id = Long.parseLong(message.getUser());
		}
		catch (NumberFormatException ex) {
			id = -1;
		}
		// If the provided name was an ID
		TwitterUser user = null;
		try {
			if (id > 0) {
				user = getUser(id);
			}
			else {
				user = getUser(message.getUser());
			}
		}
		catch (TwitterException ex) {
			// something went wrong getting the user from twitter. Queue the message
			// for retry.
			queueService.create(new QueuedTwitterMessage(message, ex.getErrorMessage()));
		}

		// if we have a real user prepare and send the message
		if (user != null && user.getScreenName() != null && !user.getScreenName().isEmpty()) {
			try {
				if (message.isSendAsDirectMessage()) {
					twitter.sendDirectMessage(user.getId(), message.getMessage());
				}
				else {
					String preparedMesage = prepareMessage(user.getScreenName(), message.getMessage());
					System.out.println("Prepared message: " + preparedMesage);
					twitter.updateStatus(preparedMesage);
				}
				// message has been send successfully. Update the status
				message.setDateSend(new Date());
				messageService.update(message.getId(), message);
				messageSent.fire(message);
			}
			catch (TwitterException ex) {
				// the user exists, so we are looking at a network error or a rate
				// limit error.
				// In both cases, add the message to the message to the queue.
				queueService.create(new QueuedTwitterMessage(message, ex.getErrorMessage()));
			}
		}
	}

	protected String prepareMessage(String screenName, String message) {
		return "@" + screenName + " " + message;
	}

	protected TwitterUser getUser(long id) throws TwitterException {
		TwitterUser user = userService.getById(id);
		if (user == null) {
			user = getUserFromTwitter(id, true);
		}
		return user;
	}

	protected TwitterUser getUser(String screenName) throws TwitterException {
		TwitterUser user = userService.getByScreenName(screenName);
		if (user == null) {
			user = getUserFromTwitter(screenName, true);
		}
		return user;
	}

	/**
	 * @param screenName
	 * @param persist Whether the retrieved Twitter user needs to be persisted.
	 * @return The user from Twitter or null if the screenName not exists.
	 * @throws TwitterException
	 */
	protected TwitterUser getUserFromTwitter(String screenName, boolean persist) throws TwitterException {
		User u = null;
		try {
			u = twitter.showUser(screenName);
		}
		catch (TwitterException ex) {
			// if the user does not exists return null. Otherwise rethrow the message
			if (ex.resourceNotFound()) return null;
			else throw ex;
		}
		return convertAndPersistUser(u, persist);
	}

	/**
	 * @param id
	 * @param persist Whether the retrieved Twitter user needs to be persisted.
	 * @return The user from Twitter or null if the id not exists.
	 * @throws TwitterException
	 */
	protected TwitterUser getUserFromTwitter(long id, boolean persist) throws TwitterException {
		User u;
		try {
			u = twitter.showUser(id);
		}
		catch (TwitterException ex) {
			// if the user does not exists return null. Otherwise rethrow the message
			if (ex.resourceNotFound()) return null;
			else throw ex;
		}
		return convertAndPersistUser(u, persist);
	}

	protected TwitterUser convertAndPersistUser(User u, boolean persist) {
		if (u == null) return null;
		TwitterUser user = Utility.fromTwitter4JUser(u);
		user.setDateRetrievedFromTwitter(new Date());
		if (user != null && persist) {
			userService.create(user);
		}
		return user;
	}
}