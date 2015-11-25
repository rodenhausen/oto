package edu.arizona.biosemantics.oto2.ontologize.server.persist.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.ontologize.server.Configuration;
import edu.arizona.biosemantics.oto2.ontologize.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.ontologize.server.persist.file.PermanentOntologyFileDAO;
import edu.arizona.biosemantics.oto2.ontologize.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize.shared.model.Ontology;
import edu.arizona.biosemantics.oto2.ontologize.shared.model.Term;
import edu.arizona.biosemantics.oto2.ontologize.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.ontologize.shared.model.toontology.OntologySynonymSubmission;
import edu.arizona.biosemantics.oto2.ontologize.shared.model.toontology.OntologySynonymSubmissionStatus;
import edu.arizona.biosemantics.oto2.ontologize.shared.model.toontology.StatusEnum;
import edu.arizona.biosemantics.oto2.ontologize.shared.model.toontology.Synonym;

public class OntologySynonymSubmissionDAO {

	private TermDAO termDAO;
	private OntologyDAO ontologyDAO;
	private OntologySynonymSubmissionStatusDAO ontologySynonymSubmissionStatusDAO;
	private OntologySynonymSubmissionSynonymDAO ontologySynonymSubmissionSynonymDAO;
	private PermanentOntologyFileDAO permanentOntologyFileDAO;
	
	public OntologySynonymSubmissionDAO() {} 
	
	public OntologySynonymSubmission get(int id) throws Exception  {
		OntologySynonymSubmission ontologySynonymSubmission = null;
		try(Query query = new Query("SELECT * FROM ontologize_ontologysynonymsubmission WHERE id = ?")) {
			query.setParameter(1, id);
			ResultSet result = query.execute();
			while(result.next()) {
				ontologySynonymSubmission = createSynonymSubmission(result);
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return ontologySynonymSubmission;
	}
	
	private String getLabel(String classIri) throws Exception {
		if(classIri.startsWith(Configuration.etcOntologyBaseIRI)) {
			try(Query query = new Query("SELECT * FROM ontologize_ontologyclasssubmission s"
					+ " WHERE s.class_iri = ?")) {
				query.setParameter(1, classIri);
				ResultSet resultSet = query.execute();
				if(resultSet.next()) {
					return resultSet.getString("submission_term");
				}
				return null;
			} catch(QueryException | SQLException e) {
				log(LogLevel.ERROR, "Query Exception", e);
				throw new QueryException(e);
			}
		} else {
			return permanentOntologyFileDAO.getClassLabel(classIri);
		}
	}
	
	private OntologySynonymSubmission createSynonymSubmission(ResultSet result) throws Exception {
		int id = result.getInt("id");
		int collectionId = result.getInt("collection");
		int termId = result.getInt("term");
		Term term = null;
		if(!result.wasNull())
			term = termDAO.get(termId);
		String submissionTerm = result.getString("submission_term");
		int ontologyId = result.getInt("ontology");
		String classIRI = result.getString("class_iri");
		String source = result.getString("source");
		String sampleSentence = result.getString("sample_sentence");
		String user = result.getString("user");
		
		Ontology ontology = ontologyDAO.get(ontologyId);
		List<OntologySynonymSubmissionStatus> ontologysynonymSubmissionStatuses = ontologySynonymSubmissionStatusDAO.getStatusOfOntologySynonymSubmission(id);
		return new OntologySynonymSubmission(id, collectionId, term, submissionTerm, ontology, classIRI, getLabel(classIRI),
				ontologySynonymSubmissionSynonymDAO.getSynonyms(id), 
				source, sampleSentence,	user, ontologysynonymSubmissionStatuses);
	}

	public OntologySynonymSubmission insert(OntologySynonymSubmission ontologySynonymSubmission) throws QueryException  {
		if(ontologySynonymSubmission.hasId())
			this.remove(ontologySynonymSubmission);
		try(Query insert = new Query("INSERT INTO `ontologize_ontologysynonymsubmission` "
				+ "(`collection`, `term`, `submission_term`, `ontology`, `class_iri`, `source`, `sample_sentence`, "
				+ "`user`)"
				+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?)")) {
			insert.setParameter(1, ontologySynonymSubmission.getCollectionId());
			if(ontologySynonymSubmission.getTerm() == null)
				insert.setParameterNull(2, java.sql.Types.BIGINT);
			else
				insert.setParameter(2,ontologySynonymSubmission.getTerm().getId());
			insert.setParameter(3, ontologySynonymSubmission.getSubmissionTerm());
			insert.setParameter(4, ontologySynonymSubmission.getOntology().getId());
			insert.setParameter(5, ontologySynonymSubmission.getClassIRI());
			insert.setParameter(6, ontologySynonymSubmission.getSource());
			insert.setParameter(7, ontologySynonymSubmission.getSampleSentence());
			insert.setParameter(8, ontologySynonymSubmission.getUser());
			insert.execute();
			ResultSet generatedKeys = insert.getGeneratedKeys();
			generatedKeys.next();
			int id = generatedKeys.getInt(1);
			
			ontologySynonymSubmission.setId(id);
			
			for(Synonym synonym : ontologySynonymSubmission.getSynonyms())
				synonym.setSubmission(id);				
			ontologySynonymSubmissionSynonymDAO.insert(ontologySynonymSubmission.getSynonyms());
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return ontologySynonymSubmission;
	}
	
	public void update(OntologySynonymSubmission ontologySynonymSubmission) throws QueryException  {		
		try(Query query = new Query("UPDATE ontologize_ontologysynonymsubmission SET collection = ?, term = ?, "
				+ "submission_term = ?, ontology = ?, class_iri = ?, source = ?, sample_sentence = ?, "
				+ "user = ? WHERE id = ?")) {
			ontologySynonymSubmission.setCollectionId(ontologySynonymSubmission.getCollectionId());
			query.setParameter(1, ontologySynonymSubmission.getCollectionId());
			if(ontologySynonymSubmission.getTerm() == null)
				query.setParameterNull(2, java.sql.Types.BIGINT);
			else
				query.setParameter(2,ontologySynonymSubmission.getTerm().getId());
			query.setParameter(3, ontologySynonymSubmission.getSubmissionTerm());
			query.setParameter(4, ontologySynonymSubmission.getOntology().getId());
			query.setParameter(5, ontologySynonymSubmission.getClassIRI());
			query.setParameter(6, ontologySynonymSubmission.getSource());
			query.setParameter(7, ontologySynonymSubmission.getSampleSentence());
			query.setParameter(8, ontologySynonymSubmission.getUser());
			query.setParameter(9, ontologySynonymSubmission.getId());
			query.execute();
			
			ontologySynonymSubmissionStatusDAO.update(ontologySynonymSubmission.getSubmissionStatuses());
			ontologySynonymSubmissionSynonymDAO.update(ontologySynonymSubmission.getId(), ontologySynonymSubmission.getSynonyms());
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
		}
	}
	
	public void remove(OntologySynonymSubmission ontologySynonymSubmission) throws QueryException  {
		try(Query query = new Query("DELETE FROM ontologize_ontologysynonymsubmission WHERE id = ?")) {
			query.setParameter(1, ontologySynonymSubmission.getId());
			query.execute();
			
			ontologySynonymSubmissionStatusDAO.remove(ontologySynonymSubmission.getSubmissionStatuses());
			ontologySynonymSubmissionSynonymDAO.remove(ontologySynonymSubmission.getId());
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
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
	
	public void setOntologySynonymSubmissionSynonymDAO(
			OntologySynonymSubmissionSynonymDAO ontologySynonymSubmissionSynonymDAO) {
		this.ontologySynonymSubmissionSynonymDAO = ontologySynonymSubmissionSynonymDAO;
	}

	public List<OntologySynonymSubmission> get(Collection collection) throws Exception {
		return this.getByCollectionId(collection.getId());
	}
	

	public List<OntologySynonymSubmission> getByCollectionId(
			int ontologyId) throws Exception {
		List<OntologySynonymSubmission> result = new LinkedList<OntologySynonymSubmission>();
		try(Query query = new Query("SELECT * FROM ontologize_ontologysynonymsubmission WHERE collection = ?")) {
			query.setParameter(1, ontologyId);
			ResultSet resultSet = query.execute();
			while(resultSet.next()) {
				result.add(createSynonymSubmission(resultSet));
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return result;
	}	

	public List<OntologySynonymSubmission> get(Collection collection, StatusEnum status) throws Exception {
		List<OntologySynonymSubmission> result = new LinkedList<OntologySynonymSubmission>();
		try(Query query = new Query("SELECT * FROM ontologize_ontologysynonymsubmission s, "
				+ "ontologize_ontologysynonymsubmission_status ss, ontologize_status st"
				+ " WHERE s.collection = ? AND ss.ontologysynonymsubmission = s.id AND ss.status = st.id AND"
				+ " st.name = ?")) {
			query.setParameter(1, collection.getId());
			query.setParameter(2, status.getDisplayName());
			ResultSet resultSet = query.execute();
			while(resultSet.next()) {
				result.add(createSynonymSubmission(resultSet));
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return result;
	}

	public List<OntologySynonymSubmission> get(Collection collection, StatusEnum status, String term) throws Exception {
		List<OntologySynonymSubmission> result = new LinkedList<OntologySynonymSubmission>();
		try(Query query = new Query("SELECT * FROM ontologize_ontologysynonymsubmission s, "
				+ "ontologize_ontologysynonymsubmission_status ss, ontologize_status st, ontologize_ontologysynonymsubmission_synonym ssy"
				+ " WHERE "
				+ "s.collection = ? AND ss.ontologysynonymsubmission = s.id AND ss.status = st.id AND st.name = ? AND ssy.ontologysynonymsubmission = s.id"
				+ " AND (s.submission_term = ? OR ssy.synonym = ?)")) {
			query.setParameter(1, collection.getId());
			query.setParameter(2, status.getDisplayName());
			query.setParameter(3, term);
			query.setParameter(4, term);
			ResultSet resultSet = query.execute();
			while(resultSet.next()) {
				result.add(createSynonymSubmission(resultSet));
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return result;
	}
	
	public void setPermanentOntologyFileDAO(PermanentOntologyFileDAO permanentOntologyFileDAO) {
		this.permanentOntologyFileDAO = permanentOntologyFileDAO;
	}
	
	public List<OntologySynonymSubmission> getByClassIRI(String classIRI) throws Exception {		
		List<OntologySynonymSubmission> ontologySynonymSubmissions = new LinkedList<OntologySynonymSubmission>();
		try(Query query = new Query("SELECT * FROM ontologize_ontologysynonymsubmission WHERE class_iri = ?")) {
			query.setParameter(1, classIRI);
			ResultSet result = query.execute();
			while(result.next()) {
				ontologySynonymSubmissions.add(createSynonymSubmission(result));
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return ontologySynonymSubmissions;
	}

	public void remove(OntologyClassSubmission ontologyClassSubmission) throws Exception {
		if(ontologyClassSubmission.hasClassIRI()) {
			List<OntologySynonymSubmission> ontologySynonymSubmissions = this.getByClassIRI(ontologyClassSubmission.getClassIRI());
			for(OntologySynonymSubmission ontologySynonymSubmission : ontologySynonymSubmissions) {
				this.remove(ontologySynonymSubmission);
			}
		}
	}
	
}
