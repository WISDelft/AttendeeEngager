/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import nl.wisdelft.cdf.client.shared.EngagementStatus;
import nl.wisdelft.cdf.client.shared.Recommendation;
import nl.wisdelft.cdf.client.shared.TwitterUser;
import nl.wisdelft.cdf.client.shared.TwitterUserEndpoint;
import nl.wisdelft.cdf.server.status.Created;
import org.jboss.errai.bus.server.annotations.Service;
import org.slf4j.Logger;
import twitter4j.TwitterException;

/**
 * @author Jasper Oosterman
 * @created Feb 25, 2014 Delft University of Technology Web Information Systems
 */
@ApplicationScoped
@Service
public class TwitterUserEndpointImpl implements TwitterUserEndpoint {

	@Inject
	@Created
	private Event<TwitterUser> created;

	@Inject
	private TwitterUserService userService;

	@Inject
	private RecommendationService recommendationService;

	@Inject
	Logger logger;

	@Inject
	Utility utility;

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.wisdelft.cdf.client.shared.TwitterUserEndpoint#create(java.lang.String)
	 */
	@Override
	public Response create(String name) {
		// check if there is either an ID or a username
		if (name == null || (name = name.trim()).isEmpty()) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		TwitterUser user = null;
		try {
			user = userService.createFromScreenNameOrID(name);
		}
		catch (TwitterException e) {
			Response.serverError().entity(e.getMessage());
		}

		if (user != null) {
			// User exists. Return the URL to this entity
			return Response.created(UriBuilder.fromResource(TwitterUserEndpoint.class).path(String.valueOf(user.getId())).build()).build();
		}
		else {
			// the user could not be found
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@Override
	public Response get(Long id) {
		TwitterUser user = userService.getById(id);
		if (user != null) {
			return Response.ok(user).build();
		}
		else {
			logger.warn("User requested but not found: " + id);
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@Override
	public Response getByDashboardPath(String dashboardPath) {
		TwitterUser user = userService.getByDashboardPath(dashboardPath);
		if (user != null) {
			return Response.ok(user).build();
		}
		else {
			logger.warn("User requested but not found: " + dashboardPath);
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@Override
	public Response update(Long id, TwitterUser entity) {
		userService.update(id, entity);
		return Response.ok().build();
	}

	@Override
	public List<Recommendation> getAllRecommendations(Long userID) {
		return recommendationService.getByUser(userID);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.wisdelft.cdf.client.shared.TwitterUserEndpoint#updateStatus(java.lang
	 * .Long, nl.wisdelft.cdf.client.shared.EngagementStatus)
	 */
	@Override
	public Response updateStatus(Long id, String status) {
		EngagementStatus es = EngagementStatus.valueOf(status);
		TwitterUser user = userService.getById(id);
		if (user != null) {
			user.setEngagementStatus(es);
			userService.update(id, user);
			return Response.ok().build();
		}
		else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.wisdelft.cdf.client.shared.TwitterUserEndpoint#updateCommunicationLocale
	 * (java.lang.Long, java.lang.String)
	 */
	@Override
	public Response updateCommunicationLocale(Long id, String locale) {
		TwitterUser user = userService.getById(id);
		if (user != null) {
			user.setLangPreference(locale);
			userService.update(id, user);
			return Response.ok().build();
		}
		else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@Override
	public Response updateRecommendation(Long id, Long recId, Recommendation entity) {
		// if the recommendation exists and the userid is correct
		Recommendation rec = recommendationService.getById(recId);
		if (rec != null && rec.getUserID() == id) {
			recommendationService.update(recId, entity);
			return Response.ok().build();
		}
		else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@Override
	public Response dashboardVisited(Long id) {
		TwitterUser user = userService.getById(id);
		if (user != null) {
			user.incDashboardVisited();
			userService.update(id, user);
			return Response.ok().build();
		}
		else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}
}
