package edu.arizona.biosemantics.oto2.ontologize.client.layout;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

import edu.arizona.biosemantics.oto2.ontologize.client.hierarchy.HierarchyView;
import edu.arizona.biosemantics.oto2.ontologize.client.info.TermInfoView;
import edu.arizona.biosemantics.oto2.ontologize.client.ontologyview.OntologyView;
import edu.arizona.biosemantics.oto2.ontologize.client.order.OrderView;
import edu.arizona.biosemantics.oto2.ontologize.client.toontology.ToOntologyView;

public class ContentView extends BorderLayoutContainer {

	private int portalColumnCount = 9;
	//private TermsView termsView;
	//private LabelsView labelsView;
	private TermInfoView termInfoView;
	private OntologyView ontologyView;

	public ContentView(EventBus eventBus) {
		/*SelectionHandler<Widget> handler = new SelectionHandler<Widget>() {
			@Override
			public void onSelection(SelectionEvent<Widget> event) {
				TabPanel panel = (TabPanel) event.getSource();
				Widget w = event.getSelectedItem();
				TabItemConfig config = panel.getConfig(w);
			}
		};*/

		ontologyView = new OntologyView(eventBus);
		TabPanel tabPanel = new TabPanel();
		//tabPanel.addSelectionHandler(handler);
		tabPanel.setWidth(450);
		TabItemConfig toOntologyConfig = new TabItemConfig("To Ontology");
		toOntologyConfig.setEnabled(true);
		tabPanel.add(new ToOntologyView(eventBus), toOntologyConfig);
		/*TabItemConfig hierarchyConfig = new TabItemConfig("Hierarchy");
		hierarchyConfig.setEnabled(false);
		tabPanel.add(new HierarchyView(eventBus), hierarchyConfig);
		TabItemConfig orderConfig = new TabItemConfig("Orders");
		orderConfig.setEnabled(false);
		tabPanel.add(new OrderView(eventBus), orderConfig);*/
		TabItemConfig ontologyViewConfig = new TabItemConfig("Ontology View");
		ontologyViewConfig.setEnabled(true);
		tabPanel.add(ontologyView, ontologyViewConfig);
		/*tabPanel.addSelectionHandler(new SelectionHandler<Widget>() {
			@Override
			public void onSelection(SelectionEvent<Widget> event) {
				if(event.getSelectedItem().equals(ontologyView.asWidget())) {
					ontologyView.refresh();
				}
			}
		});*/
		
		this.setWidget(tabPanel);
		
		//termsView = new TermsView(eventBus);
		//labelsView = new LabelsView(eventBus, portalColumnCount);
		termInfoView = new TermInfoView(eventBus);

		/*ContentPanel cp = new ContentPanel();
		cp.setHeadingText("Terms to be Categorized");
		cp.add(termsView);
		BorderLayoutData d = new BorderLayoutData(.20);
		// d.setMargins(new Margins(0, 1, 1, 1));
		d.setCollapsible(true);
		d.setSplit(true);
		d.setCollapseMini(true);
		setWestWidget(cp, d);*/

		ContentPanel cp = new ContentPanel();
		cp.setHeadingText("Ontologize");
		cp.add(tabPanel);
		BorderLayoutData d = new BorderLayoutData();
		d.setMargins(new Margins(0, 0, 0, 0));
		setCenterWidget(cp, d);

		cp = new ContentPanel();
		cp.setHeadingText("Term Information");
		cp.add(termInfoView);
		d = new BorderLayoutData(.40);
		d.setMargins(new Margins(0, 0, 20, 0));
		d.setCollapsible(true);
		d.setSplit(true);
		d.setCollapseMini(true);
		setSouthWidget(cp, d);

		// cp = new ContentPanel();
		/*
		 * cp.setHeadingText("Search"); d = new BorderLayoutData(.20);
		 * //d.setMargins(new Margins(1)); d.setCollapsible(true);
		 * d.setSplit(true); d.setCollapseMini(true);
		 * setNorthWidget(getMenu(), d);
		 */
	}

	/*public void setCollection(Collection collection) {
		termsView.setCollection(collection);
		labelsView.setCollection(collection);
		termInfoView.setCollection(collection);
	}*/

}