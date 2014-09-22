package edu.arizona.biosemantics.oto2.oto.client.event;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.oto.client.event.CategorizeCopyTermEvent.CategorizeCopyTermHandler;
import edu.arizona.biosemantics.oto2.oto.shared.model.Label;
import edu.arizona.biosemantics.oto2.oto.shared.model.Label.AddResult;
import edu.arizona.biosemantics.oto2.oto.shared.model.Term;

public class CategorizeCopyTermEvent extends GwtEvent<CategorizeCopyTermHandler> {

	public interface CategorizeCopyTermHandler extends EventHandler {
		void onCategorize(CategorizeCopyTermEvent event);
	}
	
    public static Type<CategorizeCopyTermHandler> TYPE = new Type<CategorizeCopyTermHandler>();
    
    private List<Term> terms;
	private Label sourceCategory;
	private List<Label> targetCategories;
	
	public CategorizeCopyTermEvent(List<Term> terms, Label sourceCategory, List<Label> targetCategories) {
		this.terms = terms;
		this.sourceCategory = sourceCategory;
		this.targetCategories = targetCategories;
	}
	
	public CategorizeCopyTermEvent(Term term, Label sourceCategory, List<Label> targetCategories) {
		this.terms = new LinkedList<Term>();
		terms.add(term);
		this.sourceCategory = sourceCategory;
		this.targetCategories = targetCategories;
	}
	
	public CategorizeCopyTermEvent(Term term, Label sourceCategory, Label targetCategory) {
		this.terms = new LinkedList<Term>();
		terms.add(term);
		this.sourceCategory = sourceCategory;
		this.targetCategories = new LinkedList<Label>();
		targetCategories.add(targetCategory);
	}
    
    public CategorizeCopyTermEvent(List<Term> terms, Label sourceCategory, Label targetCategory) {
        this.terms = terms;
        this.sourceCategory = sourceCategory;	
        this.targetCategories = new LinkedList<Label>();
		targetCategories.add(targetCategory);
    }
	
	@Override
	public Type<CategorizeCopyTermHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(CategorizeCopyTermHandler handler) {
		handler.onCategorize(this);
	}

	public List<Term> getTerms() {
		return terms;
	}

	public Label getSourceCategory() {
		return sourceCategory;
	}

	public List<Label> getTargetCategories() {
		return targetCategories;
	}

}