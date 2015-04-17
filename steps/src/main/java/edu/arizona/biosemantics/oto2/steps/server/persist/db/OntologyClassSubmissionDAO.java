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
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmissionStatus;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;

public class OntologyClassSubmissionDAO {

	private TermDAO termDAO;
	private OntologyDAO ontologyDAO;
	private OntologyClassSubmissionStatusDAO ontologyClassSubmissionStatusDAO;
	
	public OntologyClassSubmissionDAO() {} 
	
	public OntologyClassSubmission get(int id)  {
		OntologyClassSubmission classSubmission = null;
		try(Query query = new Query("SELECT * FROM otosteps_ontologyclasssubmission WHERE id = ?")) {
			query.setParameter(1, id);
			ResultSet result = query.execute();
			while(result.next()) {
				classSubmission = createClassSubmission(result);
			}
		} catch(Exception e) {
			log(LogLevel.ERROR, "Query Exception", e);
		}
		return classSubmission;
	}
	
	private OntologyClassSubmission createClassSubmission(ResultSet result) throws SQLException {
		int id = result.getInt("id");
		int termId = result.getInt("term");
		String submission_term = result.getString("submission_term");
		int ontologyId = result.getInt("ontology");
		String superClassIRI = result.getString("superclass_iri");
		String definition = result.getString("definition");
		String synonyms = result.getString("synonyms");
		String source = result.getString("source");
		String sampleSentence = result.getString("sample_sentence");
		String partOfIRI = result.getString("part_of_iri");
		boolean entity = result.getBoolean("entity");
		boolean quality = result.getBoolean("quality");
		
		Term term = termDAO.get(termId);
		Ontology ontology = ontologyDAO.get(ontologyId);
		List<OntologyClassSubmissionStatus> ontologyClassSubmissionStatuses = ontologyClassSubmissionStatusDAO.getStatusOfOntologyClassSubmission(id);
		return new OntologyClassSubmission(id, term, submission_term, ontology, superClassIRI, definition, synonyms, source, sampleSentence,
				partOfIRI, entity, quality, ontologyClassSubmissionStatuses);
	}

	public OntologyClassSubmission insert(OntologyClassSubmission ontologyClassSubmission)  {
		if(!ontologyClassSubmission.hasId()) {
			try(Query insert = new Query("INSERT INTO `otosteps_ontologyclasssubmission` "
					+ "(`term`, `submission_term`, `ontology`, `superclass_iri`, `definition`, `synonyms`, `source`, `sample_sentence`, "
					+ "`part_of_iri`, `entity`, `quality`)"
					+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				insert.setParameter(1, ontologyClassSubmission.getTerm().getId());
				insert.setParameter(2, ontologyClassSubmission.getSubmissionTerm());
				insert.setParameter(3, ontologyClassSubmission.getOntology().getId());
				insert.setParameter(4, ontologyClassSubmission.getSuperclassIRI());
				insert.setParameter(5, ontologyClassSubmission.getDefinition());
				insert.setParameter(6, ontologyClassSubmission.getSynonyms());
				insert.setParameter(7, ontologyClassSubmission.getSource());
				insert.setParameter(8, ontologyClassSubmission.getSampleSentence());
				insert.setParameter(9, ontologyClassSubmission.getPartOfIRI());
				insert.setParameter(10, ontologyClassSubmission.isEntity());
				insert.setParameter(11, ontologyClassSubmission.isQuality());
				insert.execute();
				ResultSet generatedKeys = insert.getGeneratedKeys();
				generatedKeys.next();
				int id = generatedKeys.getInt(1);
				
				ontologyClassSubmission.setId(id);
			} catch(Exception e) {
				log(LogLevel.ERROR, "Query Exception", e);
			}
		}
		return ontologyClassSubmission;
	}
	
	public void update(OntologyClassSubmission ontologyClassSubmission)  {		
		try(Query query = new Query("UPDATE otosteps_ontologyclasssubmission SET term = ?, submission_term = ?,"
				+ " ontology = ?, superclass_iri = ?, definition = ?, synonyms = ?, source = ?, sample_sentence = ?, part_of_iri = ?, "
				+ "entity = ?, quality = ? WHERE id = ?")) {
			query.setParameter(1, ontologyClassSubmission.getTerm().getId());
			query.setParameter(2, ontologyClassSubmission.getSubmissionTerm());
			query.setParameter(3, ontologyClassSubmission.getOntology().getId());
			query.setParameter(4, ontologyClassSubmission.getSuperclassIRI());
			query.setParameter(5, ontologyClassSubmission.getDefinition());
			query.setParameter(6, ontologyClassSubmission.getSynonyms());
			query.setParameter(7, ontologyClassSubmission.getSource());
			query.setParameter(8, ontologyClassSubmission.getSampleSentence());
			query.setParameter(9, ontologyClassSubmission.getPartOfIRI());
			query.setParameter(10, ontologyClassSubmission.isEntity());
			query.setParameter(11, ontologyClassSubmission.isQuality());
			query.setParameter(12, ontologyClassSubmission.getId());
			query.execute();
			
			for(OntologyClassSubmissionStatus ontologyClassSubmissionStatus : ontologyClassSubmission.getSubmissionStatuses())
				ontologyClassSubmissionStatusDAO.update(ontologyClassSubmissionStatus);
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
		}
	}
	
	public void remove(OntologyClassSubmission ontologyClassSubmission)  {
		try(Query query = new Query("DELETE FROM otosteps_ontologyclasssubmission WHERE id = ?")) {
			query.setParameter(1, ontologyClassSubmission.getId());
			query.execute();
			
			for(OntologyClassSubmissionStatus ontologySubmissionStatus : ontologyClassSubmission.getSubmissionStatuses())
				ontologyClassSubmissionStatusDAO.remove(ontologySubmissionStatus);
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
		}
	}

	public void setTermDAO(TermDAO termDAO) {
		this.termDAO = termDAO;
	}

	public void setOntologyClassSubmissionStatusDAO(
			OntologyClassSubmissionStatusDAO ontologyClassSubmissionStatusDAO) {
		this.ontologyClassSubmissionStatusDAO = ontologyClassSubmissionStatusDAO;
	}

	public void setOntologyDAO(OntologyDAO ontologyDAO) {
		this.ontologyDAO = ontologyDAO;
	}

	public List<OntologyClassSubmission> get(Collection collection) {
		List<OntologyClassSubmission> result = new LinkedList<OntologyClassSubmission>();
		try(Query query = new Query("SELECT * FROM otosteps_ontologyclasssubmission s, otosteps_term t WHERE s.term = t.id AND t.collection = ?")) {
			query.setParameter(1, collection.getId());
			ResultSet resultSet = query.execute();
			while(resultSet.next()) {
				result.add(createClassSubmission(resultSet));
			}
		} catch(Exception e) {
			log(LogLevel.ERROR, "Query Exception", e);
		}
		return result;
	}	
	
}
