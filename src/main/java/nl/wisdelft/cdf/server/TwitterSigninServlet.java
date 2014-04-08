/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author Jasper Oosterman
 * @created Apr 1, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
public class TwitterSigninServlet extends HttpServlet {
	private static final long serialVersionUID = -6205814293093350242L;

	@Inject
	Logger logger;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Get the configuration from the auto loaded twitter4J.properties
		Configuration conf = new TwitterFactory().getInstance().getConfiguration();
		String consumerKey = conf.getOAuthConsumerKey();
		String consumerSecret = conf.getOAuthConsumerSecret();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret).setOAuthAccessToken(null).setOAuthAccessTokenSecret(null);

		// build a new instance without the access tokens
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();

		request.getSession().setAttribute("twitter", twitter);
		try {
			StringBuffer callbackURL = request.getRequestURL();
			int index = callbackURL.lastIndexOf("/");
			callbackURL.replace(index, callbackURL.length(), "").append("/twittercallback");

			RequestToken requestToken = twitter.getOAuthRequestToken(callbackURL.toString());
			request.getSession().setAttribute("requestToken", requestToken);
			response.sendRedirect(requestToken.getAuthenticationURL());
		}
		catch (TwitterException e) {
			throw new ServletException(e);
		}

	}
}
