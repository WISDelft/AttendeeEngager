/**
 * 
 */
package nl.wisdelft.cdf.client.shared;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * @author Jasper Oosterman
 * @created Feb 25, 2014
 * Delft University of Technology
 * Web Information Systems
 * 
 */
@Path("/twittermessages")
public interface TwitterMessageEndpoint {
	@POST
  @Consumes("application/json")
  public Response create(TwitterMessage entity);
	
	@GET
	@Path("/{id:[0-9][0-9]*}")
  public Response get(@PathParam("id") Long id);
	
	@PUT
  @Path("/{id:[0-9][0-9]*}")
  @Consumes("application/json")
  public Response update(@PathParam("id") Long id, TwitterMessage entity);

  @DELETE
  @Path("/{id:[0-9][0-9]*}")
  public Response delete(@PathParam("id") Long id);
	
}
