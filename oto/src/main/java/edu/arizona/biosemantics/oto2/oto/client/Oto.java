package edu.arizona.biosemantics.oto2.oto.client;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

import edu.arizona.biosemantics.oto2.oto.client.event.SaveEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.SaveEvent.SaveHandler;
import edu.arizona.biosemantics.oto2.oto.client.layout.OtoPresenter;
import edu.arizona.biosemantics.oto2.oto.client.layout.OtoView;

public class Oto {
		
	private SimpleEventBus eventBus;
	private OtoView view;
	private OtoPresenter presenter;
	private HandlerRegistration saveHandlerRegistration;

	public Oto() {
		this.eventBus = new SimpleEventBus();
		this.presenter = new OtoPresenter(eventBus);
		this.view = new OtoView(eventBus);
		presenter.setView(view);
	}
	
	public OtoView getView() {
		return view;
	}
	
	public void loadCollection(int collectionId, String secret) {
		presenter.loadCollection(collectionId, secret);
	}
	
	public void setSaveHandler(SaveHandler saveHandler) {
		if(saveHandlerRegistration != null)
			saveHandlerRegistration.removeHandler();
		saveHandlerRegistration = this.eventBus.addHandler(SaveEvent.TYPE, saveHandler);
	}

}
