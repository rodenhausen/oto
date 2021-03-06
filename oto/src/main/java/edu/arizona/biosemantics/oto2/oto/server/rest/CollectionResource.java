package edu.arizona.biosemantics.oto2.oto.server.rest;

import java.util.Iterator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import com.google.inject.Inject;

import edu.arizona.biosemantics.oto2.oto.server.db.DAOManager;
import edu.arizona.biosemantics.oto2.oto.server.db.HistoricInitializerGlossaryOnly;
import edu.arizona.biosemantics.oto2.oto.server.rpc.CollectionService;
import edu.arizona.biosemantics.oto2.oto.shared.model.Collection;
import edu.arizona.biosemantics.oto2.oto.shared.model.Label;
import edu.arizona.biosemantics.oto2.oto.shared.model.TrashLabel;
import edu.arizona.biosemantics.oto2.oto.shared.rpc.ICollectionService;
import edu.arizona.biosemantics.common.log.LogLevel;

/**
 * Just a REST-like wrapper around the RPC service
 * @author thomas
 */
@Path("/oto/collection")
public class CollectionResource {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	private ICollectionService collectionService;	
		
	@Inject
	public CollectionResource() {
		DAOManager daoManager = new DAOManager();
		HistoricInitializerGlossaryOnly historicInitializer = new HistoricInitializerGlossaryOnly(daoManager);
		collectionService = new CollectionService(daoManager, historicInitializer);	
		log(LogLevel.DEBUG, "CollectionResource initialized");
	}
	
	@PUT
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public Collection put(Collection collection) throws Exception {
		try {
			return collectionService.insert(collection);
		} catch (Exception e) {
			log(LogLevel.ERROR, "Exception", e);
			return null;
		}
	}
	
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public void post(Collection collection, @QueryParam("storeAsFallback") boolean storeAsFallback) throws Exception {
		try {
			collectionService.update(collection, storeAsFallback);
		} catch (Exception e) {
			log(LogLevel.ERROR, "Exception", e);
		}
	}

	@Path("{id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Collection get(@PathParam("id") int id, @QueryParam("secret") String secret) {
		try {
			Collection result = collectionService.get(id, secret);
			//remove trashlabel as charaparser will use it in output if returned
			//move this part into charaparser itself at some point, IF other clients do want
			//to retrieve trash
			removeTrashLabel(result);
			return result;
		} catch (Exception e) {
			log(LogLevel.ERROR, "Exception", e);
			return null;
		}
	}

	private void removeTrashLabel(Collection collection) {
		Iterator<Label> labelIterator = collection.getLabels().iterator();
		while(labelIterator.hasNext()) {
			Label label = labelIterator.next();
			if(label instanceof TrashLabel) {
				labelIterator.remove();
			}
		}
	}
}