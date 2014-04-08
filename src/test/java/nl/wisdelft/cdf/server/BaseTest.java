/**
 * 
 */
package nl.wisdelft.cdf.server;

import org.junit.Before;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

/**
 * @author Jasper Oosterman
 * @created Mar 26, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
public class BaseTest {

	public Utility utility;
	public TwitterConnection twitterConnection;
	public Twitter twitter;

	@Before
	public void setupGlobal() {
		utility = new Utility();
		utility.logger = LoggerFactory.getLogger(Utility.class);
		utility.initialize();
		utility.forceDebug();
		twitter = TwitterFactory.getSingleton();
		twitterConnection = new TwitterConnection();
		twitterConnection.logger = LoggerFactory.getLogger(TwitterConnection.class);
		twitterConnection.utility = utility;
		twitterConnection.twitter = twitter;
	}

}
