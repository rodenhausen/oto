package edu.arizona.biosemantics.oto2.oto.server.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import com.google.inject.Inject;

import edu.arizona.biosemantics.common.context.shared.Context;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.oto.server.db.DAOManager;
import edu.arizona.biosemantics.oto2.oto.server.rpc.ContextService;
import edu.arizona.biosemantics.oto2.oto.shared.rpc.IContextService;

/**
 * Just a REST-like wrapper around the RPC service
 * @author thomas
 */
@Path("/oto/context")
public class ContextResource {

	@javax.ws.rs.core.Context
	UriInfo uriInfo;
	@javax.ws.rs.core.Context
	Request request;		
	
	private IContextService contextService = new ContextService(new DAOManager());

	@Inject
	public ContextResource() {
		log(LogLevel.DEBUG, "ContextResource initialized");
	}
	
	@Path("{collectionId}")
	@PUT
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public List<Context> put(@PathParam("collectionId") int collectionId, @QueryParam("secret") String secret, List<Context> contexts) {
		try {
			return contextService.insert(collectionId, secret, contexts);
		} catch (Exception e) {
			log(LogLevel.ERROR, "Exception", e);
			return null;
		}
	}
}