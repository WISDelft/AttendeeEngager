/**
 * 
 */
package nl.wisdelft.cdf.server;

import junit.framework.Assert;
import nl.wisdelft.cdf.client.shared.TwitterUser;
import org.junit.Before;
import org.junit.Test;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * @author Jasper Oosterman
 * @created Feb 26, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
public class TestTwitter extends BaseTest {
	@Before
	public void setup() {

	}

	@Test
	public void testInitializePropertiesFile() throws TwitterException {
		User u = twitter.verifyCredentials();
		Assert.assertNotNull(u);
		Assert.assertTrue(u.getId() > 0);
	}

	@Test
	public void testUserFromTwitterScreenname() throws TwitterException {
		TwitterUser user = twitterConnection.getUser("joosterman");
		Assert.assertNotNull(user);
		Assert.assertTrue(user.getId() == 158663891L);
	}

	@Test
	public void testUserFromTwitterID() throws TwitterException {

		TwitterUser user = twitterConnection.getUser("158663891");
		Assert.assertNotNull(user);
		Assert.assertEquals(user.getScreenName(), "joosterman");
	}

	@Test
	public void testUserFromTwitterWrongScreenname() throws TwitterException {
		TwitterUser user = twitterConnection.getUser("joostermanDABOSS");
		Assert.assertNull(user);
	}

	@Test
	public void testUserFromTwitterWrongID() throws TwitterException {
		TwitterUser user = twitterConnection.getUser("15866389155555");
		Assert.assertNull(user);
	}
}
