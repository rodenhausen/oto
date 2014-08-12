package edu.arizona.biosemantics.oto.oto.shared.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Label implements Serializable {

	private int id = - 1;
	private String name;
	private int collectionId;
	private String description;
	private List<Term> mainTerms = new LinkedList<Term>();
	private Map<Term, List<Term>> mainTermSynonymsMap = new HashMap<Term, List<Term>>();
	
	public Label() { }
	
	public Label(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public Label(int collectionId, String name, String description) {
		this.collectionId = collectionId;
		this.name = name;
		this.description = description;
	}
	
	public Label(int id, int collectionId, String name, String description) {
		super();
		this.id = id;
		this.collectionId = collectionId;
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String text) {
		this.name = text;
	}

	public List<Term> getMainTerms() {
		return new LinkedList<Term>(mainTerms);
	}

	public void setMainTerms(List<Term> mainTerms) {
		mainTerms.clear();
		mainTermSynonymsMap.clear();
		this.addMainTerms(mainTerms);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void removeMainTerm(Term term) {
		if(mainTermSynonymsMap.containsKey(term)) {
			List<Term> oldSynonyms = mainTermSynonymsMap.get(term);
			for(Term oldSynonym : oldSynonyms) {
				this.addMainTerm(oldSynonym);
			}
			mainTermSynonymsMap.remove(term);
		}
		mainTerms.remove(term);
	}
	
	public void addMainTerm(Term term) {
		mainTerms.add(term);
		mainTermSynonymsMap.put(term, new LinkedList<Term>());
	}
	
	public void addMainTerms(List<Term> mainTerms) {
		for(Term mainTerm : mainTerms) {
			this.addMainTerm(mainTerm);
		}
	}	
	
	public int getId() {
		return id;
	}
	
	public boolean hasId() {
		return id != -1;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public int getCollectionId() {
		return collectionId;
	}

	public void setCollection(int collectionId) {
		this.collectionId = collectionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Label other = (Label) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public void removeMainTerms(List<Term> mainTerms) {
		for(Term mainTerm : mainTerms)
			this.removeMainTerm(mainTerm);
	}

	public void setMainTermSynonymsMap(Map<Term, List<Term>> mainTermSynonymsMap) {
		this.mainTermSynonymsMap = mainTermSynonymsMap;
	}
	
	public List<Term> getSynonyms(Term mainTerm) {
		if(!mainTermSynonymsMap.containsKey(mainTerm))
			return new LinkedList<Term>();
		return mainTermSynonymsMap.get(mainTerm);
	}

	public void addSynonym(Term mainTerm, Term synonymTerm) {
		removeMainTerm(synonymTerm);
		
		if(!mainTermSynonymsMap.containsKey(mainTerm)) 
			mainTermSynonymsMap.put(mainTerm, new LinkedList<Term>());
		mainTermSynonymsMap.get(mainTerm).add(synonymTerm);
	}
	
	public void addSynonyms(Term mainTerm, List<Term> synonymTerms) {
		for(Term synonym : synonymTerms)
			addSynonym(mainTerm, synonym);
	}

	public void setSynonyms(Term mainLabelTerm, List<Term> synonymTerms) {
		for(Term synonymTerm : synonymTerms)
			removeMainTerm(synonymTerm);
		mainTermSynonymsMap.put(mainLabelTerm, synonymTerms);
	}

	public Map<Term, List<Term>> getMainTermSynonymsMap() {
		return mainTermSynonymsMap;
	}

	public void removeSynonyms(Term mainTerm) {
		this.addMainTerms(this.getSynonyms(mainTerm));
		this.setSynonyms(mainTerm, new LinkedList<Term>());
	}

}