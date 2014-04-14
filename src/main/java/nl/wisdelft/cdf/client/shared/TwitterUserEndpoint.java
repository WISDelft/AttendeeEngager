/**
 * 
 */
package nl.wisdelft.cdf.client.shared;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * @author Jasper Oosterman
 * @created Feb 25, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Path("/twitterusers")
public interface TwitterUserEndpoint {

	@POST
	@Consumes("application/json")
	public Response create(@QueryParam("user") String user);

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces("application/json")
	public Response get(@PathParam("id") Long id);

	@GET
	@Path("/{dashboardPath:........-....-....-....-............}")
	@Produces("application/json")
	public Response getByDashboardPath(@PathParam("dashboardPath") String dashboardPath);

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(@PathParam("id") Long id, TwitterUser entity);

	@PUT
	@Path("/{id:[0-9][0-9]*}/recommendations/{recid:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response updateRecommendation(@PathParam("id") Long id, @PathParam("recid") Long recId, Recommendation entity);

	@GET
	@Path("/{id:[0-9][0-9]*}/recommendations/")
	@Produces("application/json")
	public List<Recommendation> getAllRecommendations(@PathParam("id") Long id);

	@PUT
	@Path("/{id:[0-9][0-9]*}/status/{status}")
	public Response updateStatus(@PathParam("id") Long id, @PathParam("status") String status);

	@PUT
	@Path("/{id:[0-9][0-9]*}/dashboard/visited/")
	public Response dashboardVisited(@PathParam("id") Long id);

	@PUT
	@Path("/{id:[0-9][0-9]*}/locale/{locale}")
	public Response updateCommunicationLocale(@PathParam("id") Long id, @PathParam("locale") String locale);

}