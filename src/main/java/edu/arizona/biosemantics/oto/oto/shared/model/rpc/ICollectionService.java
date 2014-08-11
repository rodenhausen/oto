package edu.arizona.biosemantics.oto.oto.shared.model.rpc;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.arizona.biosemantics.oto.oto.shared.model.Collection;
import edu.arizona.biosemantics.oto.oto.shared.model.Context;
import edu.arizona.biosemantics.oto.oto.shared.model.Location;
import edu.arizona.biosemantics.oto.oto.shared.model.Ontology;
import edu.arizona.biosemantics.oto.oto.shared.model.Term;


/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("collection")
public interface ICollectionService extends RemoteService {
	
	public Collection get(Collection collection) throws Exception;
	
	public Collection update(Collection collection) throws Exception; 

	public List<Context> getContexts(Term term) throws Exception; 
	
	public List<Location> getLocations(Term term) throws Exception;
	
	public List<Ontology> getOntologies(Term term);
	
}
