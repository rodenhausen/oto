package edu.arizona.biosemantics.oto2.steps.server.persist.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.Superclass;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.Synonym;

public class OntologyClassSubmissionSuperclassDAO {
	
	public OntologyClassSubmissionSuperclassDAO() {} 
	
	public Superclass get(int id) throws QueryException  {
		Superclass superclass = null;
		try(Query query = new Query("SELECT * FROM otosteps_ontologyclasssubmission_superclass WHERE id = ?")) {
			query.setParameter(1, id);
			ResultSet result = query.execute();
			while(result.next()) {
				superclass = createSuperclass(result);
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return superclass;
	}
	
	public List<String> getSuperclasses(int ontologyClassSubmissionId) throws QueryException {
		List<String> superclasses = new LinkedList<String>();
		try(Query query = new Query("SELECT * FROM otosteps_ontologyclasssubmission_superclass WHERE ontologyclasssubmission = ?")) {
			query.setParameter(1, ontologyClassSubmissionId);
			ResultSet result = query.execute();
			while(result.next()) {
				superclasses.add(result.getString("superclass"));
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return superclasses;
	}
	
	private Superclass createSuperclass(ResultSet result) throws SQLException {
		int id = result.getInt("id");
		int ontologyClassSubmission = result.getInt("ontologyclasssubmission");
		String superclass = result.getString("superclass");
		return new Superclass(id, ontologyClassSubmission, superclass);
	}

	public Superclass insert(Superclass superclass) throws QueryException  {
		if(!superclass.hasId()) {
			try(Query insert = new Query("INSERT INTO `otosteps_ontologyclasssubmission_superclass` (`ontologyclasssubmission`, `superclass`) VALUES(?, ?)")) {
				insert.setParameter(1, superclass.getOntologyClassSubmission());
				insert.setParameter(2, superclass.getSuperclass());
				insert.execute();
				ResultSet generatedKeys = insert.getGeneratedKeys();
				generatedKeys.next();
				int id = generatedKeys.getInt(1);
				
				superclass.setId(id);
			} catch(QueryException | SQLException e) {
				log(LogLevel.ERROR, "Query Exception", e);
				throw new QueryException(e);
			}
		}
		return superclass;
	}
	
	public void update(Superclass superclass) throws QueryException  {		
		try(Query query = new Query("UPDATE otosteps_ontologyclasssubmission_superclass SET ontologyclasssubmission = ?, superclass = ? WHERE id = ?")) {
			query.setParameter(1, superclass.getOntologyClassSubmission());
			query.setParameter(2, superclass.getSuperclass());
			query.setParameter(3, superclass.getId());
			query.execute();
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
		}
	}
	
	public void remove(Superclass superclass) throws QueryException  {
		try(Query query = new Query("DELETE FROM otosteps_ontologyclasssubmission_superclass WHERE id = ?")) {
			query.setParameter(1, superclass.getId());
			query.execute();
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
		}
	}

	public List<Superclass> insert(int ontologyClassSubmissionId, List<String> superclasses) throws QueryException {
		List<Superclass> result = new LinkedList<Superclass>();
		for(String superclass : superclasses)
			result.add(insert(new Superclass(ontologyClassSubmissionId, superclass)));
		return result;
	}

	public void update(int ontologyClassSubmissionId, List<String> superclasses) throws QueryException {
		remove(ontologyClassSubmissionId);
		for(String superclass : superclasses)
			insert(new Superclass(ontologyClassSubmissionId, superclass));
	}
	
	public void remove(int ontologyClassSubmissionId) throws QueryException {
		try(Query query = new Query("DELETE FROM otosteps_ontologyclasssubmission_superclass WHERE ontologyclasssubmission = ?")) {
			query.setParameter(1, ontologyClassSubmissionId);
			query.execute();
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
		}
	}

	public void remove(List<Superclass> superclasses) throws QueryException {
		for(Superclass superclass : superclasses)
			this.remove(superclass);
	}
	
}