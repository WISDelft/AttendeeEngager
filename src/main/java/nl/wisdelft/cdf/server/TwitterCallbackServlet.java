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
import nl.wisdelft.cdf.client.shared.TwitterUser;
import org.slf4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * @author Jasper Oosterman
 * @created Apr 1, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
public class TwitterCallbackServlet extends HttpServlet {
	private static final long serialVersionUID = 1657390011452788111L;

	@Inject
	Logger logger;

	@Inject
	TwitterUserService userService;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
		RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
		String verifier = request.getParameter("oauth_verifier");
		String path = "/";
		try {
			AccessToken token = twitter.getOAuthAccessToken(requestToken, verifier);
			// store the access token
			long userID = token.getUserId();
			String accessToken = token.getToken();
			String accessTokenSecret = token.getTokenSecret();
			TwitterUser user = userService.getById(userID);
			if (user != null) {
				user.setAccessToken(accessToken);
				user.setAccessTokenSecret(accessTokenSecret);
				userService.update(userID, user);
				path = "/#dashboard;user=" + user.getDashboardPath();
			}
			else {
				logger.warn("User logged in but is not known in the system. User: " + token.getUserId() + " , Screenname: " + token.getScreenName());
				path = "/#home;errorText=" + "User unknown";
			}
			request.getSession().removeAttribute("requestToken");
		}
		catch (TwitterException e) {
			throw new ServletException(e);
		}
		response.sendRedirect(request.getContextPath() + path);
	}
}