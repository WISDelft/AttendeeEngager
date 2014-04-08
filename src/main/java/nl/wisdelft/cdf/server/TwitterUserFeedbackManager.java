/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import nl.wisdelft.cdf.client.shared.EngagementStatus;
import nl.wisdelft.cdf.client.shared.TwitterUser;
import nl.wisdelft.cdf.server.status.Engaged;
import nl.wisdelft.cdf.server.status.NotResponded;
import nl.wisdelft.cdf.server.status.OptedOut;
import org.slf4j.Logger;

/**
 * @author Jasper Oosterman
 * @created Feb 28, 2014
 * @organization Delft University of Technology - Web Information Systems Checks
 *               Twitter for replies (tweets containing our username), direct
 *               messages, and the followers.
 */
@Singleton
@Startup
public class TwitterUserFeedbackManager {
	@Inject
	Utility utility;

	@Inject
	TwitterConnection twitter;

	@Inject
	TwitterUserService userService;

	@Inject
	@Engaged
	Event<TwitterUser> engagedUser;

	@Inject
	@NotResponded
	Event<TwitterUser> notRespondingUser;

	@Inject
	@OptedOut
	Event<TwitterUser> optedOutUser;

	@Inject
	Logger logger;

	/**
	 * Starts a timed thread that monitors the TwitterUser feedback on scheduled
	 * intervals
	 */
	@PostConstruct
	public void inititialize() {
		if (!utility.getPropertyAsBoolean("module_active_processTwitterUserFeedback")) {
			logger.info(TwitterUserFeedbackManager.class.getSimpleName() + " not started. Disabled in config.");
			return;
		}
		logger.info(TwitterUserFeedbackManager.class.getSimpleName() + " starting...");
	}

	@PreDestroy
	public void destroy() {
		logger.info(TwitterUserFeedbackManager.class.getSimpleName() + " stopped.");
	}

	@Schedule(persistent = false, hour = "*", minute = "0/2")
	@Asynchronous
	public void getFeedback() {
		if (!utility.getPropertyAsBoolean("module_active_processTwitterUserFeedback")) {
			return;
		}
		System.out.println("Getting Twitter user feedback...");
		// get the feedback
		checkDirectMessages();
		checkReplies();
		checkFollowers();

	}

	/**
	 * Check the account for replies from users. At this moment this functionality
	 * is not used and implemented.
	 */
	public void checkReplies() {}

	/**
	 * Check the account for direct messages from users. At this moment this
	 * functionality is not used and implemented.
	 */
	public void checkDirectMessages() {}

	/**
	 * Checks all the followers of the account and updates the information in the
	 * TwitterUser objects and persist the changes to the DB
	 */
	public void checkFollowers() {
		// Get all the followers
		List<Long> followers = twitter.getFollowers();
		// null indicates the followers could not be retrieved from Twitter.
		if (followers == null) {
			return;
		}

		// put them in a set for quick lookup
		HashSet<Long> followerSet = new HashSet<Long>(followers);
		// loop over all existing users and update their status
		List<TwitterUser> currentUsers = userService.getAll();
		for (TwitterUser user : currentUsers) {
			// The user is currently following us
			if (followerSet.contains(user.getId())) {
				// but the user was not yet following us
				if (!user.isFollower()) {
					// Update the status and fire the event.
					user.setFollower(true);
					user.setEngagementStatus(EngagementStatus.OPTED_IN);
					userService.update(user.getId(), user);
					engagedUser.fire(user);
				}
			}
			// the user is currently not following us
			else {
				// but the user was following us.
				if (user.isFollower()) {
					user.setFollower(false);
					user.setEngagementStatus(EngagementStatus.OPTED_OUT);
					userService.update(user.getId(), user);
					optedOutUser.fire(user);
				}
			}
			// check if the user should be send a reminder message
			if (shouldReminderMessageBeSent(user)) {
				notRespondingUser.fire(user);
			}
		}
	}

	/**
	 * A user should be send a reminder message if we have sent a welcome message
	 * and he has not responded within 24 hours. Responses are 1) The user starts
	 * following our account 2) The user opts-in or 3) the user opts-out.
	 * 
	 * @param user
	 * @return true if the status of the user is CONTACTED and
	 *         <code>Date.now() - user.dateLastContacted</code> is more than the
	 *         configured amount.
	 */
	private boolean shouldReminderMessageBeSent(TwitterUser user) {
		// check if the user is CONTACTED
		if (user.getEngagementStatus() != EngagementStatus.CONTACTED) {
			return false;
		}
		// check if there is a dateLastContacted
		if (user.getDateLastContacted() == null) {
			logger.error("Consistency error. User status is CONTACTED but dateLastContacted is empty. Reminder message will not be send");
			return false;
		}
		// get the minimum duration we have to wait before we can send a reminder
		// message
		Integer minimumDelayInHours = utility.getPropertyAsInt("minimumHoursBetweenWelcomeAndReminder");
		if (minimumDelayInHours == null) {
			minimumDelayInHours = 24;
		}
		// get the hours since last contact
		GregorianCalendar now = new GregorianCalendar();
		long nowMillis = now.getTimeInMillis();
		now.setTime(user.getDateLastContacted());
		long lastContactMillis = now.getTimeInMillis();
		long delay = nowMillis - lastContactMillis;
		float delayInHours = delay / (1000 * 60 * 60);
		return delayInHours > minimumDelayInHours;
	}
}
