/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * @author Jasper Oosterman
 * @created Mar 3, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Stateless
@Path("/clicktracker")
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class LinkClickedEndpoint {

	@Inject
	Event<LinkClicked> linkClicked;

	/**
	 * A JPA EntityManager which is configured according to the
	 * {@code forge-default} persistence context defined in
	 * {@code /WEB-INF/persistence.xml}. Note that this field is not initialized
	 * by the application: it is injected by the EJB container.
	 */
	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;

	@GET
	@Path("/{link:.*}")
	public Response get(@PathParam("link") String link, @Context HttpServletRequest request) {
		try {
			// get "all" info from the requester and store it
			LinkClicked lnk = new LinkClicked(link);
			lnk.setSessionID(request.getRequestedSessionId());
			lnk.setRemoteHost(request.getRemoteHost());
			lnk.setRemoteAddr(request.getRemoteAddr());
			// store the link
			em.persist(lnk);
			// fire the event that a link has been clicked
			linkClicked.fire(lnk);
			URI uri = new URI(link);

			return Response.temporaryRedirect(uri).build();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}
}
