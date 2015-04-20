package edu.arizona.biosemantics.oto2.steps.server.persist.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.Status;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmissionStatus;

public class OntologySynonymSubmissionStatusDAO {
	
	private StatusDAO statusDAO;
	
	public OntologySynonymSubmissionStatusDAO() {} 
	
	public OntologySynonymSubmissionStatus get(int id)  {
		OntologySynonymSubmissionStatus ontologySynonymSubmissionStatus = null;
		try(Query query = new Query("SELECT * FROM otosteps_ontologysynonymsubmission_status WHERE id = ?")) {
			query.setParameter(1, id);
			ResultSet result = query.execute();
			while(result.next()) {
				ontologySynonymSubmissionStatus = createOntologySynonymSubmissionStatus(result);
			}
		} catch(Exception e) {
			log(LogLevel.ERROR, "Query Exception", e);
		}
		return ontologySynonymSubmissionStatus;
	}
	
	private OntologySynonymSubmissionStatus createOntologySynonymSubmissionStatus(ResultSet result) throws SQLException {
		int id = result.getInt("id");
		int ontologysynonymsubmissionId = result.getInt("ontologysynonymsubmission");
		Status status = statusDAO.get(result.getInt("status"));
		String externalId= result.getString("external_id");
		return new OntologySynonymSubmissionStatus(id, ontologysynonymsubmissionId, status, externalId);
	}

	public OntologySynonymSubmissionStatus insert(OntologySynonymSubmissionStatus ontologySynonymSubmissionStatus)  {
		if(!ontologySynonymSubmissionStatus.hasId()) {
			try(Query insert = new Query("INSERT INTO `otosteps_ontologysynonymsubmission_status` "
					+ "(`ontologysynonymsubmission`, `status`, `external_id`) VALUES(?, ?, ?)")) {
				insert.setParameter(1, ontologySynonymSubmissionStatus.getOntologySynonymSubmissionId());
				insert.setParameter(2, ontologySynonymSubmissionStatus.getStatus().getId());
				insert.setParameter(3, ontologySynonymSubmissionStatus.getExternalId());
				insert.execute();
				ResultSet generatedKeys = insert.getGeneratedKeys();
				generatedKeys.next();
				int id = generatedKeys.getInt(1);
				
				ontologySynonymSubmissionStatus.setId(id);
			} catch(Exception e) {
				log(LogLevel.ERROR, "Query Exception", e);
			}
		}
		return ontologySynonymSubmissionStatus;
	}
	
	public void update(OntologySynonymSubmissionStatus ontologySynonymSubmissionStatus)  {		
		try(Query query = new Query("UPDATE otosteps_ontologysynonymsubmission_status SET ontologysynonymsubmission = ?, "
				+ "status = ?, external_id = ? WHERE id = ?")) {
			query.setParameter(1, ontologySynonymSubmissionStatus.getOntologySynonymSubmissionId());
			query.setParameter(2, ontologySynonymSubmissionStatus.getStatus().getId());
			query.setParameter(3, ontologySynonymSubmissionStatus.getExternalId());
			query.setParameter(4, ontologySynonymSubmissionStatus.getId());
			query.execute();
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
		}
	}
	
	public void remove(OntologySynonymSubmissionStatus ontologySynonymSubmissionStatus)  {
		try(Query query = new Query("DELETE FROM otosteps_ontologysynonymsubmission_status WHERE id = ?")) {
			query.setParameter(1, ontologySynonymSubmissionStatus.getId());
			query.execute();
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
		}
	}

	public List<OntologySynonymSubmissionStatus> getStatusOfOntologySynonymSubmission(int ontologySynonymSubmissionId) {
		List<OntologySynonymSubmissionStatus> ontologySynonymSubmissionStatuses = new LinkedList<OntologySynonymSubmissionStatus>();
		try(Query query = new Query("SELECT id FROM otosteps_ontologysynonymsubmission_status WHERE ontologysynonymsubmission = ?")) {
			query.setParameter(1, ontologySynonymSubmissionId);
			ResultSet result = query.execute();
			while(result.next()) {
				int id = result.getInt(1);
				OntologySynonymSubmissionStatus ontologySynonymSubmissionStatus = get(id);
				if(ontologySynonymSubmissionStatus != null)
					ontologySynonymSubmissionStatuses.add(ontologySynonymSubmissionStatus);
			}
		} catch(Exception e) {
			log(LogLevel.ERROR, "Query Exception", e);
		}
		return ontologySynonymSubmissionStatuses;
	}

	public void setStatusDAO(StatusDAO statusDAO) {
		this.statusDAO = statusDAO;
	}
	
}