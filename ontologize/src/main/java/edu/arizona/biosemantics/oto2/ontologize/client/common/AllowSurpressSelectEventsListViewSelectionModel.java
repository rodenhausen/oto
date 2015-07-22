package edu.arizona.biosemantics.oto2.ontologize.client.common;

import java.util.List;

import com.sencha.gxt.widget.core.client.ListViewSelectionModel;

public class AllowSurpressSelectEventsListViewSelectionModel<M> extends
		ListViewSelectionModel<M> {
	public void setSelection(List<M> selection, boolean surpressEvents) {
		doSelect(selection, false, surpressEvents);
	}
}