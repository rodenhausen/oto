package edu.arizona.biosemantics.oto2.steps.client.toontology;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.data.shared.IconProvider;

import edu.arizona.biosemantics.oto2.steps.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.TermTreeNode;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.TextTreeNode;

public class TermTreeNodeIconProvider implements IconProvider<TextTreeNode> {

	private static TermStatusImages termStatusImages = GWT.create(TermStatusImages.class);
	private Collection collection;
	private EventBus eventBus;
		
	public TermTreeNodeIconProvider(EventBus eventBus) {
		this.eventBus = eventBus;
		bindEvents();
	}

	private void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				collection = event.getCollection();
			}
		});
	}

	@Override
	public ImageResource getIcon(TextTreeNode node) {
		if(node instanceof TermTreeNode) {
			TermTreeNode termTreeNode = (TermTreeNode)node;
			if(collection != null && collection.isUsed(termTreeNode.getTerm())) {
				return termStatusImages.green();
			} else if(termTreeNode.getTerm().isRemoved()) {
				return termStatusImages.yellow();
			}
			return null;
		}
		return null;
	}

}