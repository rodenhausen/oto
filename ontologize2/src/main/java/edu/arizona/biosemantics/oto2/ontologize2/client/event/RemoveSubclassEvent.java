package edu.arizona.biosemantics.oto2.ontologize2.client.event;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveSubclassEvent.Handler;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Term;

public class RemoveSubclassEvent extends GwtEvent<Handler> implements HasRowId, Serializable {

	public interface Handler extends EventHandler {
		void onRemove(RemoveSubclassEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
	private Term superclass;
	private Term[] subclasses = new Term[] { };
	private int rowId = -1;

	private RemoveSubclassEvent() { }

    public RemoveSubclassEvent(Term superclass, Term... subclasses) {
    	this.superclass = superclass;
    	this.subclasses = subclasses;
    }
    
	public RemoveSubclassEvent(Term superclass, List<Term> subclasses) {
		this.superclass = superclass;
		this.subclasses = subclasses.toArray(this.subclasses);
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onRemove(this);
	}

	public Term getSuperclass() {
		return superclass;
	}

	public Term[] getSubclasses() {
		return subclasses;
	}
	
	@Override
	public int getRowId() {
		return rowId;
	}

	@Override
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}

	@Override
	public boolean hasRowId() {
		return rowId != -1;
	}

	public boolean hasSubclasses() {
		return subclasses.length > 0;
	}	
	
}
