/**
 * 
 */
package nl.wisdelft.cdf.server;

import junit.framework.Assert;
import nl.wisdelft.cdf.client.shared.TwitterUser;
import org.junit.Test;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 * @author Jasper Oosterman
 * @created Feb 26, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
public class TestTwitter {
	@Test
	public void testInitializePropertiesFile() throws TwitterException {
		Twitter tw = TwitterFactory.getSingleton();
		System.out.println(tw.getAuthorization());
		User u = tw.verifyCredentials();
		Assert.assertNotNull(u);
		Assert.assertTrue(u.getId() > 0);
	}

	@Test
	public void testUserFromTwitterScreenname() throws TwitterException {
		MessageSender sender = new MessageSender();
		TwitterUser user = sender.getUserFromTwitter("joosterman", false);
		Assert.assertNotNull(user);
		Assert.assertTrue(user.getId() == 158663891L);
	}

	@Test
	public void testUserFromTwitterID() throws TwitterException {
		MessageSender sender = new MessageSender();
		TwitterUser user = sender.getUserFromTwitter(158663891L, false);
		Assert.assertNotNull(user);
		Assert.assertEquals(user.getScreenName(), "joosterman");
	}

	@Test
	public void testUserFromTwitterWrongScreenname() throws TwitterException {
		MessageSender sender = new MessageSender();
		TwitterUser user = sender.getUserFromTwitter("joostermanDABOSS", false);
		Assert.assertNull(user);
	}

	@Test
	public void testUserFromTwitterWrongID() throws TwitterException {
		MessageSender sender = new MessageSender();
		TwitterUser user = sender.getUserFromTwitter(15866389155555L, false);
		Assert.assertNull(user);
	}

	@Test
	public void testPrepareMessage() throws TwitterException {
		MessageSender sender = new MessageSender();
		String message = sender.prepareMessage("joosterman", "test");
		Assert.assertEquals(message, "@joosterman test");
	}
}
