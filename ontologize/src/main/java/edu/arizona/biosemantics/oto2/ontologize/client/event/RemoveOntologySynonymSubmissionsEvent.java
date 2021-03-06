package edu.arizona.biosemantics.oto2.ontologize.client.event;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.ontologize.client.event.RemoveOntologySynonymSubmissionsEvent.Handler;
import edu.arizona.biosemantics.oto2.ontologize.shared.model.toontology.OntologySynonymSubmission;

public class RemoveOntologySynonymSubmissionsEvent  extends GwtEvent<Handler> {

	public interface Handler extends EventHandler {
		void onRemove(RemoveOntologySynonymSubmissionsEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
	private List<OntologySynonymSubmission> ontologySynonymSubmissions;
	
	public RemoveOntologySynonymSubmissionsEvent(List<OntologySynonymSubmission> ontologySynonymSubmissions) {
		this.ontologySynonymSubmissions = ontologySynonymSubmissions;
	}

	public RemoveOntologySynonymSubmissionsEvent(
			OntologySynonymSubmission selectedSubmission) {
		this.ontologySynonymSubmissions = new LinkedList<OntologySynonymSubmission>();
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onRemove(this);
	}

	public List<OntologySynonymSubmission> getOntologySynonymSubmissions() {
		return ontologySynonymSubmissions;
	}

}
