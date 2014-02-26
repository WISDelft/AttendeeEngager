/**
 * 
 */
package nl.wisdelft.cdf.server;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import nl.wisdelft.cdf.client.shared.TwitterMessage;
import nl.wisdelft.cdf.client.shared.TwitterMessageEndpoint;

/**
 * @author Jasper Oosterman
 * @created Feb 25, 2014 Delft University of Technology Web Information Systems
 */
@Stateless
public class TwitterMessageEndpointImpl implements TwitterMessageEndpoint {

	@Inject
	@Created
	private Event<TwitterMessage> created;

	@Inject
	TwitterMessageService service;

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.wisdelft.cdf.client.shared.TwitterMessageEndpoint#create(nl.wisdelft
	 * .cdf.client.shared.TwitterMessage)
	 */
	@Override
	public Response create(TwitterMessage entity) {
		// check if there is either an ID or a username
		if (entity.getUser() == null || entity.getUser().isEmpty()) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		// Store the Message in the Database
		service.create(entity);
		created.fire(entity);

		// message is stored. return the URL to this entity
		return Response.created(UriBuilder.fromResource(TwitterMessageEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.wisdelft.cdf.client.shared.TwitterMessageEndpoint#get(java.lang.Long)
	 */
	@Override
	public Response get(Long id) {
		TwitterMessage message = service.getById(id);
		return Response.ok(message).build();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.wisdelft.cdf.client.shared.TwitterMessageEndpoint#update(java.lang.Long,
	 * nl.wisdelft.cdf.client.shared.TwitterMessage)
	 */
	@Override
	public Response update(Long id, TwitterMessage entity) {
		service.update(id, entity);
		return Response.ok().build();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.wisdelft.cdf.client.shared.TwitterMessageEndpoint#delete(java.lang.Long)
	 */
	@Override
	public Response delete(Long id) {
		service.delete(id);
		return Response.ok().build();
	}
}
