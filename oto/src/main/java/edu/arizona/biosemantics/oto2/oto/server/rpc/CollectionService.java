package edu.arizona.biosemantics.oto2.oto.server.rpc;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;

import edu.arizona.biosemantics.oto2.oto.server.Configuration;
import edu.arizona.biosemantics.oto2.oto.server.db.DAOManager;
import edu.arizona.biosemantics.oto2.oto.server.db.HistoricInitializer;
import edu.arizona.biosemantics.oto2.oto.server.db.HistoricInitializerGlossaryOnly;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.oto.shared.model.Collection;
import edu.arizona.biosemantics.oto2.oto.shared.model.Comment;
import edu.arizona.biosemantics.oto2.oto.shared.model.Label;
import edu.arizona.biosemantics.oto2.oto.shared.model.Location;
import edu.arizona.biosemantics.oto2.oto.shared.model.Term;
import edu.arizona.biosemantics.oto2.oto.shared.rpc.ICollectionService;

public class CollectionService extends RemoteServiceServlet implements ICollectionService {
	
	private HistoricInitializerGlossaryOnly historicInitializer;
	private DAOManager daoManager;
	
	@Inject
	public CollectionService(DAOManager daoManager, HistoricInitializerGlossaryOnly historicInitializer) {
		this.daoManager = daoManager;
		this.historicInitializer = historicInitializer;
	}
	
	@Override
	public Collection get(Collection collection) {
		return daoManager.getCollectionDAO().get(collection.getId());
	}
	
	@Override
	public Collection get(int id, String secret) {
		Collection collection = new Collection(id, secret);
		return get(collection);
	}

	@Override
	public void update(Collection collection, boolean storeAsFallback) {
		log(LogLevel.INFO, "Update collection " + collection.getId());
		try {
			daoManager.getCollectionDAO().update(collection, storeAsFallback);
		} catch(Exception e) {
			log(LogLevel.ERROR, "Exception", e);
		}
	}	
	
	@Override
	public Term addTerm(Term term, int bucketId) {
		return daoManager.getTermDAO().insert(term, bucketId);
	}

	@Override
	public Label addLabel(Label label, int collectionId) {
		return daoManager.getLabelDAO().insert(label, collectionId);
	}
	
	@Override
	public Comment addComment(Comment comment, int termId) {
		return daoManager.getCommentDAO().insert(comment, termId);
	}
	
	@Override
	public Collection insert(Collection collection)  {
		if(collection.getSecret() == null || collection.getSecret().isEmpty())
			createDefaultSecret(collection);
		//collection.getLabels().add(new TrashLabel("Useless", "This category can be uesd to label terms as uselss"));
		collection = daoManager.getCollectionDAO().insert(collection);
		log(LogLevel.INFO, "Inserted collection " + collection.getId());
		return collection;
	}
	
	private void createDefaultSecret(Collection collection) {
		//collection id not available here yet
		String secret = String.valueOf(collection.hashCode());// Encryptor.getInstance().encrypt(Integer.toString(collection.getId()));
		collection.setSecret(secret);
	}
	
	@Override
	public List<Location> getLocations(Term term) {
		List<Location> result = new LinkedList<Location>();
		Set<Label> labels = daoManager.getLabelingDAO().getLabels(term);
		if(labels.isEmpty())
			result.add(new Location(term.getTerm(), null));
		for(Label label : labels) {
			result.add(new Location(term.getTerm(), label));
		}
		return result;
	}

	@Override
	public Collection reset(Collection collection, boolean resetAll) {
		log(LogLevel.INFO, "Reset collection " + collection.getId());
		return daoManager.getCollectionDAO().reset(collection, resetAll);
	}
	
	@Override
	public Collection initializeFromHistory(Collection collection) {
		log(LogLevel.INFO, "Initialize from history collection " + collection.getId());
		collection = daoManager.getCollectionDAO().get(collection.getId());
		historicInitializer.initialize(collection);
		return daoManager.getCollectionDAO().get(collection.getId());
	}

}
