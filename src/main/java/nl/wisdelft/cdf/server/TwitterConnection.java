/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import nl.wisdelft.cdf.client.shared.TwitterMessage;
import nl.wisdelft.cdf.client.shared.TwitterUser;
import org.slf4j.Logger;
import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.RequestToken;

/**
 * @author Jasper Oosterman
 * @created Feb 27, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@ApplicationScoped
public class TwitterConnection {
	protected Twitter twitter;

	@Inject
	protected Utility utility;

	@Inject
	protected Logger logger;

	public TwitterConnection() {}

	@PostConstruct
	private void initialize() {
		twitter = TwitterFactory.getSingleton();
	}

	/**
	 * Send the message to the user assuming the user is the ScreenName
	 * 
	 * @param twitterMessage
	 * @return true if the message was successfully send.
	 * @throws TwitterException
	 */
	public boolean send(TwitterMessage message) throws TwitterException {
		String screenName = message.getUser().getScreenName();
		String messageText = message.getMessage();
		// in debug mode always send to the test user account
		if (utility.getPropertyAsBoolean("debug")) {
			screenName = utility.getPropertyAsString("testUser");
		}

		if (message.isSendAsDirectMessage()) {
			return sendAsDirectMessage(screenName, messageText);
		}
		else {
			return sendAsStatusUpdate(screenName, messageText);
		}
	}

	private boolean sendAsDirectMessage(String screenName, String messageText) throws TwitterException {
		twitter.sendDirectMessage(screenName, messageText);
		return true;
	}

	private boolean sendAsStatusUpdate(String screenName, String messageText) throws TwitterException {
		String preparedMessage = "@" + screenName + " " + messageText;
		twitter.updateStatus(preparedMessage);
		return true;
	}

	/**
	 * Gets the twitter user and converts into a TwitterUser. The TwitterUser is
	 * not persisted.
	 * 
	 * @param name
	 * @return The TwitterUser or null if the user does not exists on Twitter.
	 * @throws TwitterException if there was any other problem
	 */
	public TwitterUser getUser(String name) throws TwitterException {
		User u;
		// check whether the name is an ID or a ScreenName
		Long id = utility.parseLong(name);
		try {
			if (id != null) {
				// provided name was an ID
				u = twitter.showUser(id);
			}
			else {
				// provided name was a ScreenName
				u = twitter.showUser(name);
			}
			// Convert the twitter user into a TwitterUser
			TwitterUser user = utility.fromTwitter4JUser(u);
			user.setDateRetrievedFromTwitter(new Date());
			return user;
		}
		catch (TwitterException ex) {
			if (ex.resourceNotFound()) {
				return null;
			}
			else {
				// otherwise throw the exception that the user could not be retrieved.
				logger.error("Error getting User from Twitter: " + name, ex);
				throw ex;
			}
		}
	}

	public List<Long> getFollowers() {
		List<Long> followers = new ArrayList<Long>();
		try {
			long cursor = -1;
			IDs ids = null;
			while (ids == null || ids.hasNext()) {
				ids = twitter.getFollowersIDs(cursor);
				for (long id : ids.getIDs()) {
					followers.add(id);
				}
				cursor = ids.getNextCursor();
			}
			logger.info("There are " + followers.size() + " followers.");
			return followers;
		}
		catch (TwitterException e) {
			// on error signal null
			return null;
		}
	}

	public RequestToken getOAuthRequestToken(String callbackURL) throws TwitterException {
		return twitter.getOAuthRequestToken(callbackURL);
	}
}
