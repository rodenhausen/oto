package edu.arizona.biosemantics.oto2.steps.server.persist.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmissionStatus;

public class OntologySynonymSubmissionDAO {

	private TermDAO termDAO;
	private OntologyDAO ontologyDAO;
	private OntologySynonymSubmissionStatusDAO ontologySynonymSubmissionStatusDAO;
	
	public OntologySynonymSubmissionDAO() {} 
	
	public OntologySynonymSubmission get(int id)  {
		OntologySynonymSubmission ontologySynonymSubmission = null;
		try(Query query = new Query("SELECT * FROM otosteps_ontologysynonymsubmission WHERE id = ?")) {
			query.setParameter(1, id);
			ResultSet result = query.execute();
			while(result.next()) {
				ontologySynonymSubmission = createSynonymSubmission(result);
			}
		} catch(Exception e) {
			log(LogLevel.ERROR, "Query Exception", e);
		}
		return ontologySynonymSubmission;
	}
	
	private OntologySynonymSubmission createSynonymSubmission(ResultSet result) throws SQLException {
		int id = result.getInt("id");
		int termId = result.getInt("term");
		String submission_term = result.getString("submission_term");
		int ontologyId = result.getInt("ontology");
		String classIRI = result.getString("class_iri");
		String synonyms = result.getString("synonyms");
		String source = result.getString("source");
		String sampleSentence = result.getString("sample_sentence");
		
		Term term = termDAO.get(termId);
		Ontology ontology = ontologyDAO.get(ontologyId);
		List<OntologySynonymSubmissionStatus> ontologysynonymSubmissionStatuses = ontologySynonymSubmissionStatusDAO.getStatusOfOntologySynonymSubmission(id);
		return new OntologySynonymSubmission(id, term, submission_term, ontology, classIRI, synonyms, source, sampleSentence,
				ontologysynonymSubmissionStatuses);
	}

	public OntologySynonymSubmission insert(OntologySynonymSubmission ontologySynonymSubmission)  {
		if(!ontologySynonymSubmission.hasId()) {
			try(Query insert = new Query("INSERT INTO `otosteps_ontologysynonymsubmission` "
					+ "(`term`, `submission_term`, `ontology`, `class_iri`, `synonyms`, `source`, `sample_sentence`)"
					+ " VALUES(?, ?, ?, ?, ?, ?, ?)")) {
				insert.setParameter(1, ontologySynonymSubmission.getTerm().getId());
				insert.setParameter(2, ontologySynonymSubmission.getSubmissionTerm());
				insert.setParameter(3, ontologySynonymSubmission.getOntology().getId());
				insert.setParameter(4, ontologySynonymSubmission.getClassIRI());
				insert.setParameter(5, ontologySynonymSubmission.getSynonyms());
				insert.setParameter(6, ontologySynonymSubmission.getSource());
				insert.setParameter(7, ontologySynonymSubmission.getSampleSentence());
				insert.setParameter(7, ontologySynonymSubmission.getSampleSentence());
				insert.execute();
				ResultSet generatedKeys = insert.getGeneratedKeys();
				generatedKeys.next();
				int id = generatedKeys.getInt(1);
				
				ontologySynonymSubmission.setId(id);
			} catch(Exception e) {
				log(LogLevel.ERROR, "Query Exception", e);
			}
		}
		return ontologySynonymSubmission;
	}
	
	public void update(OntologySynonymSubmission ontologySynonymSubmission)  {		
		try(Query query = new Query("UPDATE otosteps_ontologysynonymsubmission SET term = ?, submission_term = ?,"
				+ " ontology = ?, class_iri = ?, synonyms = ?, source = ?, sample_sentence = ? WHERE id = ?")) {
			query.setParameter(1, ontologySynonymSubmission.getTerm().getId());
			query.setParameter(2, ontologySynonymSubmission.getSubmissionTerm());
			query.setParameter(3, ontologySynonymSubmission.getOntology().getId());
			query.setParameter(4, ontologySynonymSubmission.getClassIRI());
			query.setParameter(5, ontologySynonymSubmission.getSynonyms());
			query.setParameter(6, ontologySynonymSubmission.getSource());
			query.setParameter(7, ontologySynonymSubmission.getSampleSentence());
			query.setParameter(8, ontologySynonymSubmission.getId());
			query.execute();
			
			for(OntologySynonymSubmissionStatus ontologySynonymSubmissionStatus : ontologySynonymSubmission.getSubmissionStatuses())
				ontologySynonymSubmissionStatusDAO.update(ontologySynonymSubmissionStatus);
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
		}
	}
	
	public void remove(OntologySynonymSubmission ontologySynonymSubmission)  {
		try(Query query = new Query("DELETE FROM otosteps_ontologysynonymsubmission WHERE id = ?")) {
			query.setParameter(1, ontologySynonymSubmission.getId());
			query.execute();
			
			for(OntologySynonymSubmissionStatus ontologySubmissionStatus : ontologySynonymSubmission.getSubmissionStatuses())
				ontologySynonymSubmissionStatusDAO.remove(ontologySubmissionStatus);
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
		}
	}

	public void setTermDAO(TermDAO termDAO) {
		this.termDAO = termDAO;
	}
	
	public void setOntologySynonymSubmissionStatusDAO(
			OntologySynonymSubmissionStatusDAO ontologySynonymSubmissionStatusDAO) {
		this.ontologySynonymSubmissionStatusDAO = ontologySynonymSubmissionStatusDAO;
	}

	public void setOntologyDAO(OntologyDAO ontologyDAO) {
		this.ontologyDAO = ontologyDAO;
	}

	public List<OntologySynonymSubmission> get(Collection collection) {
		List<OntologySynonymSubmission> result = new LinkedList<OntologySynonymSubmission>();
		try(Query query = new Query("SELECT * FROM otosteps_ontologysynonymsubmission s, otosteps_term t WHERE s.term = t.id AND t.collection = ?")) {
			query.setParameter(1, collection.getId());
			ResultSet resultSet = query.execute();
			while(resultSet.next()) {
				result.add(createSynonymSubmission(resultSet));
			}
		} catch(Exception e) {
			log(LogLevel.ERROR, "Query Exception", e);
		}
		return result;
	}	
	
}
