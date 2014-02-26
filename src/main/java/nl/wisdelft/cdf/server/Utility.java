/**
 * 
 */
package nl.wisdelft.cdf.server;

import nl.wisdelft.cdf.client.shared.TwitterUser;
import twitter4j.User;

/**
 * @author Jasper Oosterman
 * @created Feb 26, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
public class Utility {

	private Utility() {}

	/**
	 * Returns a filled TwitterUser object. The object is not persisted.
	 * 
	 * @param user Twitter4J User object
	 * @return
	 */
	public static TwitterUser fromTwitter4JUser(User u) {
		TwitterUser user = new TwitterUser();
		user.setId(u.getId());
		user.setDateCreated(u.getCreatedAt());
		user.setDescription(u.getDescription());
		user.setFollowerCount(u.getFollowersCount());
		user.setFriendsCount(u.getFriendsCount());
		user.setLang(u.getLang());
		user.setLocation(u.getLocation());
		user.setProtectedAccount(u.isProtected());
		user.setScreenName(u.getScreenName());
		user.setURL(u.getURL());
		return user;
	}
}
