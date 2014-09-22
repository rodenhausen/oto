package edu.arizona.biosemantics.oto2.oto.client.categorize.all;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.theme.blue.client.panel.BlueFramedPanelAppearance;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.FramedPanel.FramedPanelAppearance;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import com.sencha.gxt.widget.core.client.container.PortalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent.BeforeShowHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import edu.arizona.biosemantics.oto2.oto.client.categorize.all.LabelPortlet.LabelInfoContainer;
import edu.arizona.biosemantics.oto2.oto.client.event.LabelCreateEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.LabelRemoveEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.LabelsMergeEvent;
import edu.arizona.biosemantics.oto2.oto.shared.model.Collection;
import edu.arizona.biosemantics.oto2.oto.shared.model.Label;
import edu.arizona.biosemantics.oto2.oto.shared.model.Term;
import edu.arizona.biosemantics.oto2.oto.shared.model.Label.AddResult;
import edu.arizona.biosemantics.oto2.oto.shared.rpc.ICollectionService;
import edu.arizona.biosemantics.oto2.oto.shared.rpc.ICollectionServiceAsync;
import edu.arizona.biosemantics.oto2.oto.shared.rpc.RPCCallback;

public class LabelPortletsView extends PortalLayoutContainer {
	
	public class LabelsMenu extends Menu implements BeforeShowHandler {

		public LabelsMenu() {
			this.setWidth(140);
			this.addBeforeShowHandler(this);			
		}

		@Override
		public void onBeforeShow(BeforeShowEvent event) {
			this.clear();
			
			MenuItem add = new MenuItem("Add Category");
			add.addSelectionHandler(new SelectionHandler<Item>() {
				@Override
				public void onSelection(SelectionEvent<Item> event) {
					LabelAddDialog labelAddDialog = new LabelAddDialog();
					labelAddDialog.show();
				}
			});
			
			MenuItem collapse = new MenuItem("Collapse All");
			collapse.addSelectionHandler(new SelectionHandler<Item>() {
				@Override
				public void onSelection(SelectionEvent<Item> event) {
					for(Label label : collection.getLabels()) {
						LabelPortlet portlet = labelPortletsMap.get(label);
						if(portlet != null)
							portlet.collapse();
					}
				}
			});
			
			
			MenuItem expand = new MenuItem("Expand All");
			expand.addSelectionHandler(new SelectionHandler<Item>() {
				@Override
				public void onSelection(SelectionEvent<Item> event) {
					for(Label label : collection.getLabels()) {
						LabelPortlet portlet = labelPortletsMap.get(label);
						if(portlet != null)
							portlet.expand();
					}
				}
			});
			
			this.add(add);
			this.add(expand);
			this.add(collapse);
			
			MenuItem collapseExpand = new MenuItem("Collapse/Expand");
			Menu collapseExpandMenu = new Menu();
			VerticalPanel verticalPanel = new VerticalPanel();
			final Set<Label> collapseLabels = new HashSet<Label>();
			final Set<Label> expandLabels = new HashSet<Label>();
			final TextButton collapseExpandButton = new TextButton("Collapse/Expand");
			collapseExpandButton.setEnabled(false);			
			for(final Label collectionLabel : collection.getLabels()) {
				LabelPortlet portlet = labelPortletsMap.get(collectionLabel);
				if(portlet != null) {
					CheckBox checkBox = new CheckBox();
					checkBox.setBoxLabel(collectionLabel.getName());
					checkBox.setValue(portlet.isExpanded());
					checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							if(event.getValue()) {
								expandLabels.add(collectionLabel); 
								collapseLabels.remove(collectionLabel);
							}
							else {
								collapseLabels.add(collectionLabel);
								expandLabels.remove(collectionLabel); 
							}
							collapseExpandButton.setEnabled(!collapseLabels.isEmpty() || !expandLabels.isEmpty());
						}
					});
					verticalPanel.add(checkBox);
				}
			}
			if(verticalPanel.getWidgetCount() > 0) {
				collapseExpandButton.addSelectHandler(new SelectHandler() {
					@Override
					public void onSelect(SelectEvent event) {
						for(Label expandLabel : expandLabels) {
							LabelPortlet portlet = labelPortletsMap.get(expandLabel);
							if(portlet != null)
								portlet.expand();
						}
						for(Label collapseLabel : collapseLabels) {
							LabelPortlet portlet = labelPortletsMap.get(collapseLabel);
							if(portlet != null)
								portlet.collapse();
						}
						LabelsMenu.this.hide();
					}
				});
				verticalPanel.add(collapseExpandButton);
				collapseExpandMenu.add(verticalPanel);
				collapseExpand.setSubMenu(collapseExpandMenu);
				this.add(collapseExpand);
			}
		}
	}
	
	public class LabelAddDialog extends Dialog {
		
		public LabelAddDialog() {
			this.setHeadingText("Add Category");
			LabelInfoContainer labelInfoContainer = new LabelInfoContainer("", "");
		    this.add(labelInfoContainer);
		 
		    final TextField labelName = labelInfoContainer.getLabelName();
		    final TextArea labelDescription = labelInfoContainer.getLabelDescription();
		    
		    getButtonBar().clear();
		    TextButton add = new TextButton("Add");
		    add.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					if(!labelName.validate()) {
						AlertMessageBox alert = new AlertMessageBox("Category Name", "A category name is required");
						alert.show();
						return;
					}
					
					final Label newLabel = new Label(labelName.getText(), labelDescription.getText());
					collectionService.addLabel(newLabel, collection.getId(), new RPCCallback<Label>() {
						@Override
						public void onSuccess(Label result) {
							eventBus.fireEvent(new LabelCreateEvent(result));
							LabelAddDialog.this.hide();
						}
					});
				}
		    });
		    TextButton cancel =  new TextButton("Cancel");
		    cancel.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					LabelAddDialog.this.hide();
				}
		    });
		    addButton(add);
		    addButton(cancel);
		}
	}

	private ICollectionServiceAsync collectionService = GWT.create(ICollectionService.class);
	private EventBus eventBus;
	private int portalColumnCount;
	private Map<Label, LabelPortlet> labelPortletsMap = new HashMap<Label, LabelPortlet>();
	private Collection collection;
	
	public LabelPortletsView(EventBus eventBus, int portalColumnCount) {
		super(portalColumnCount);
		this.eventBus = eventBus;
		this.portalColumnCount = portalColumnCount;
		double portalColumnWidth = 1.0 / portalColumnCount;
		for(int i=0; i<portalColumnCount; i++) {
			this.setColumnWidth(i, portalColumnWidth);
		}
		this.getElement().getStyle().setBackgroundColor("white");
		this.setContextMenu(new LabelsMenu());
		bindEvents();
	}

	private void bindEvents() {
		eventBus.addHandler(LabelsMergeEvent.TYPE, new LabelsMergeEvent.MergeLabelsHandler() {
			@Override
			public void onMerge(LabelsMergeEvent event) {
				List<Label> sources = event.getSources();
				Label destination = event.getDestination();
				for(Label source : sources) {
					LabelPortlet sourcePortlet = labelPortletsMap.remove(source);
					sourcePortlet.removeFromParent();
					//LabelsView.this.remove(sourcePortlet, LabelsView.this.getPortletColumn(sourcePortlet));
					LabelPortlet destinationPortlet = labelPortletsMap.get(destination);
					for(Term term : source.getMainTerms()) {
						if(destination.isMainTerm(term) && !destinationPortlet.containsMainTerm(term))
							destinationPortlet.addMainTerm(term);
						for(Term synonym : source.getSynonyms(term))
							if(destination.isMainTerm(synonym) && 
									!destinationPortlet.containsMainTerm(synonym))
								destinationPortlet.addMainTerm(synonym);
					}
				}
			}
		});
		eventBus.addHandler(LabelCreateEvent.TYPE, new LabelCreateEvent.CreateLabelHandler() {
			@Override
			public void onCreate(LabelCreateEvent event) {
				Label label = event.getLabel();
				LabelPortlet labelPortlet = createLabelPortlet(label);
				add(labelPortlet, 0);
				labelPortletsMap.put(label, labelPortlet);
			}
		});
		eventBus.addHandler(LabelRemoveEvent.TYPE, new LabelRemoveEvent.RemoveLabelHandler() {
			@Override
			public void onRemove(LabelRemoveEvent event) {
				Label label = event.getLabel();
				LabelPortlet portlet = labelPortletsMap.remove(label);
				LabelPortletsView.this.remove(portlet, LabelPortletsView.this.getPortletColumn(portlet));
				portlet.removeFromParent();
			}
		});
	}

	protected LabelPortlet createLabelPortlet(Label label) {
		LabelPortlet labelPortlet = null;
		try { 
			HighlightLabel.valueOf(label.getName().trim());
			//labelPortlet = new LabelPortlet(GWT.<OtoFramedPanelAppearance> create(OtoFramedPanelAppearance.class), 
			//		eventBus, label, collection);
			labelPortlet = new LabelPortlet(eventBus, label, collection);
			labelPortlet.setHeadingHtml("<b>" + label.getName() + "</b>");
		} catch(Exception e) {
			//labelPortlet = new LabelPortlet(GWT.<OtoFramedPanelAppearance> create(OtoFramedPanelAppearance.class), 
			//		eventBus, label, collection);
			labelPortlet = new LabelPortlet(eventBus, label, collection);
			labelPortlet.setHeadingHtml("<em>" + label.getName() + "</em>");
		}
		return labelPortlet;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;

		clear();
		labelPortletsMap.clear();
		for(Label label : collection.getLabels()) {
			LabelPortlet labelPortlet = createLabelPortlet(label);
			labelPortlet.collapse();
			add(labelPortlet, labelPortletsMap.size() % portalColumnCount);
			labelPortletsMap.put(label, labelPortlet);
		}
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				forceLayout();
			}
		});
	}

	public void forceLayout() {
		((CssFloatLayoutContainer)getContainer()).forceLayout();
	}
	
}