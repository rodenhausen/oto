package edu.arizona.biosemantics.oto2.oto.client.categorize.all;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sencha.gxt.core.client.Style.HideMode;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.data.shared.TreeStore.TreeNode;
import com.sencha.gxt.dnd.core.client.DND.Operation;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent.DndDropHandler;
import com.sencha.gxt.dnd.core.client.DropTarget;
import com.sencha.gxt.dnd.core.client.TreeDragSource;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.FramedPanel.FramedPanelAppearance;
import com.sencha.gxt.widget.core.client.Portlet;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.BeforeSelectEvent;
import com.sencha.gxt.widget.core.client.event.BeforeSelectEvent.BeforeSelectHandler;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent.BeforeShowHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.tree.Tree;

import edu.arizona.biosemantics.oto2.oto.client.common.Alerter;
import edu.arizona.biosemantics.oto2.oto.client.common.DndDropEventExtractor;
import edu.arizona.biosemantics.oto2.oto.client.common.UncategorizeDialog;
import edu.arizona.biosemantics.oto2.oto.client.event.CategorizeCopyRemoveTermEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.CategorizeCopyTermEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.CategorizeMoveTermEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.LabelModifyEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.LabelRemoveEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.LabelsMergeEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.SynonymCreationEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.SynonymRemovalEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.TermCategorizeEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.TermRenameEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.TermSelectEvent;
import edu.arizona.biosemantics.oto2.oto.client.event.TermUncategorizeEvent;
import edu.arizona.biosemantics.oto2.oto.shared.model.Collection;
import edu.arizona.biosemantics.oto2.oto.shared.model.Label;
import edu.arizona.biosemantics.oto2.oto.shared.model.Label.AddResult;
import edu.arizona.biosemantics.oto2.oto.shared.model.Term;
import edu.arizona.biosemantics.oto2.oto.shared.model.TermTreeNode;
import edu.arizona.biosemantics.oto2.oto.shared.model.TextTreeNode;
import edu.arizona.biosemantics.oto2.oto.shared.model.TextTreeNodeProperties;

public class LabelPortlet extends Portlet {
	
	public enum DropSource {
		INIT, PORTLET
	}
	
	public class MainTermTreeNode extends TermTreeNode {
		public MainTermTreeNode(Term term) {
			super(term);
		}
	}
	
	public class SynonymTermTreeNode extends TermTreeNode {
		public SynonymTermTreeNode(Term term) {
			super(term);
		} 
	}
	
	public class TermMenu extends Menu implements BeforeShowHandler {

		public TermMenu() {
			this.addBeforeShowHandler(this);
			this.setWidth(140);
		}

		@Override
		public void onBeforeShow(BeforeShowEvent event) {
			this.clear();
			
			final List<TermTreeNode> selected = tree.getSelectionModel().getSelectedItems();
			if(selected == null || selected.isEmpty()) {
				event.setCancelled(true);
				this.hide();
			} else {
				final List<Term> terms = new LinkedList<Term>();
				for(TermTreeNode node : selected) 
					terms.add(node.getTerm());

				if(collection.getLabels().size() > 1) {
					Menu moveMenu = new Menu();
					for(final Label collectionLabel : collection.getLabels())
						if(!label.equals(collectionLabel) && !collectionLabel.getMainTerms().containsAll(terms)) {
							moveMenu.add(new MenuItem(collectionLabel.getName(), new SelectionHandler<MenuItem>() {
								@Override
								public void onSelection(SelectionEvent<MenuItem> event) {
									eventBus.fireEvent(new CategorizeMoveTermEvent(terms, label, collectionLabel));
									TermMenu.this.hide();
								}
							}));
						}
					if(moveMenu.getWidgetCount() > 0) {
						MenuItem move = new MenuItem("Move to");
						move.setSubMenu(moveMenu);
						this.add(move);
					}
				}
				
				if(collection.getLabels().size() > 1) {
					Menu copyMenu = new Menu();
					VerticalPanel verticalPanel = new VerticalPanel();
					final List<Label> copyLabels = new LinkedList<Label>();
					final TextButton copyButton = new TextButton("Copy");
					copyButton.setEnabled(false);
					for(final Label collectionLabel : collection.getLabels()) {
						if(!label.equals(collectionLabel) && !collectionLabel.getMainTerms().containsAll(terms)) {
							CheckBox checkBox = new CheckBox();
							checkBox.setBoxLabel(collectionLabel.getName());
							checkBox.setValue(false);
							checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
								@Override
								public void onValueChange(ValueChangeEvent<Boolean> event) {
									if(event.getValue())
										copyLabels.add(collectionLabel);
									else
										copyLabels.remove(collectionLabel);
									copyButton.setEnabled(!copyLabels.isEmpty());
								}
							});
							verticalPanel.add(checkBox);
						}
					}
					if(verticalPanel.getWidgetCount() > 0) {
						copyButton.addSelectHandler(new SelectHandler() {
							@Override
							public void onSelect(SelectEvent event) {
								eventBus.fireEvent(new CategorizeCopyTermEvent(terms, label, copyLabels));
								TermMenu.this.hide();
							}
						});
						verticalPanel.add(copyButton);
						copyMenu.add(verticalPanel);
						MenuItem copy = new MenuItem("Copy to");
						copy.setSubMenu(copyMenu);
						this.add(copy);
					}
				}
				
				if(terms.size() == 1) {
					MenuItem rename = new MenuItem("Rename");
					final Term term = terms.iterator().next();
					rename.addSelectionHandler(new SelectionHandler<Item>() {
						@Override
						public void onSelection(SelectionEvent<Item> event) {
							final PromptMessageBox box = new PromptMessageBox(
									"Term rename", "Please input new spelling");
							box.getButton(PredefinedButton.OK).addBeforeSelectHandler(new BeforeSelectHandler() {
								@Override
								public void onBeforeSelect(BeforeSelectEvent event) {
									if(box.getTextField().getValue().trim().isEmpty()) {
										event.setCancelled(true);
										AlertMessageBox alert = new AlertMessageBox("Empty", "Empty not allowed");
										alert.show();
									}
								}
							});
							box.getTextField().setValue(term.getTerm());
							box.addHideHandler(new HideHandler() {
								@Override
								public void onHide(HideEvent event) {
									String newName = box.getValue();
									eventBus.fireEvent(new TermRenameEvent(term, newName));
								}
							});
							box.show();
						}
					});
					this.add(rename);
				}
				
				MenuItem remove = new MenuItem("Remove");
				remove.addSelectionHandler(new SelectionHandler<Item>() {
					@Override
					public void onSelection(SelectionEvent<Item> event) {
						for(Term term : terms) {
							List<Label> labels = collection.getLabels(term);
							if(labels.size() > 1) {
								UncategorizeDialog dialog = new UncategorizeDialog(eventBus, label, 
										term, labels);
							} else {
								eventBus.fireEvent(new TermUncategorizeEvent(term, label));
							}
						}
					}
				});
				this.add(remove);
				
				if(terms.size() == 1 && label.getMainTerms().size() > 1 && 
						selected.get(0) instanceof MainTermTreeNode) {
					final Term term = terms.iterator().next();
					Menu synonymMenu = new Menu();
					
					VerticalPanel verticalPanel = new VerticalPanel();
					final List<Term> synonymTerms = new LinkedList<Term>();
					final TextButton synonymButton = new TextButton("Synonomize");
					synonymButton.setEnabled(false);
					for(final Term synonymTerm : label.getMainTerms()) {
						if(!synonymTerm.equals(term)) {
							CheckBox checkBox = new CheckBox();
							checkBox.setBoxLabel(synonymTerm.getTerm());
							checkBox.setValue(false);
							checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
								@Override
								public void onValueChange(ValueChangeEvent<Boolean> event) {
									if(event.getValue())
										synonymTerms.add(synonymTerm);
									else
										synonymTerms.remove(synonymTerm);
									synonymButton.setEnabled(!synonymTerms.isEmpty());
								}
							});
							verticalPanel.add(checkBox);
						}
					}
					
					if(verticalPanel.getWidgetCount() > 0) {
						synonymButton.addSelectHandler(new SelectHandler() {
							@Override
							public void onSelect(SelectEvent event) {
								eventBus.fireEvent(new SynonymCreationEvent(label, term, synonymTerms));
								TermMenu.this.hide();
							}
						});
						verticalPanel.add(synonymButton);
						synonymMenu.add(verticalPanel);
						MenuItem addSynonym = new MenuItem("Add Synonym");
						addSynonym.setSubMenu(synonymMenu);
						this.add(addSynonym);
					}					
				}
				
				if(terms.size() == 1 && selected.get(0) instanceof MainTermTreeNode) {
					final Term term = terms.iterator().next();
					if(!label.getSynonyms(term).isEmpty()) {
						Menu synonymMenu = new Menu();
						VerticalPanel verticalPanel = new VerticalPanel();
						final List<Term> toRemove = new LinkedList<Term>();
						final TextButton synonymRemoveButton = new TextButton("Remove");
						synonymRemoveButton.setEnabled(false);
						for(final Term synonymTerm : label.getSynonyms(term)) {
							CheckBox checkBox = new CheckBox();
							checkBox.setBoxLabel(synonymTerm.getTerm());
							checkBox.setValue(false);
							checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
								@Override
								public void onValueChange(ValueChangeEvent<Boolean> event) {
									if(event.getValue())
										toRemove.add(synonymTerm);
									else
										toRemove.remove(synonymTerm);
									synonymRemoveButton.setEnabled(!toRemove.isEmpty());
								}
							});
							verticalPanel.add(checkBox);
						}
						
						if(verticalPanel.getWidgetCount() > 0) {
							synonymRemoveButton.addSelectHandler(new SelectHandler() {
								@Override
								public void onSelect(SelectEvent event) {
									eventBus.fireEvent(new SynonymRemovalEvent(label, term, toRemove));
									TermMenu.this.hide();
								}
							});
							verticalPanel.add(synonymRemoveButton);
							synonymMenu.add(verticalPanel);
							MenuItem removeSynonym = new MenuItem("Remove Synonym");
							removeSynonym.setSubMenu(synonymMenu);
							this.add(removeSynonym);
						}	
					}
				}
				
				boolean showRemoveAllSynonyms = false;
				for(Term term : terms) {
					if(!label.getSynonyms(term).isEmpty()) {
						showRemoveAllSynonyms = true;
					}
				}
				if(showRemoveAllSynonyms) {
					MenuItem removeAllSynonyms = new MenuItem("Remove all Synonyms");
					this.add(removeAllSynonyms);
					removeAllSynonyms.addSelectionHandler(new SelectionHandler<Item>() {
						@Override
						public void onSelection(SelectionEvent<Item> event) {
							for(TermTreeNode node : selected) {
								if(node instanceof MainTermTreeNode) {
									Term term = node.getTerm();
									List<Term> oldSynonyms = label.getSynonyms(term);
									eventBus.fireEvent(new SynonymRemovalEvent(label, term, oldSynonyms));
								}
							}
						}
					});
				}
			}
			
			if(this.getWidgetCount() == 0)
				event.setCancelled(true);
		}
	}
	
	public class LabelMenu extends Menu implements BeforeShowHandler {

		public LabelMenu(final Label label) {
			this.addBeforeShowHandler(this);
			this.setWidth(140);
		}

		@Override
		public void onBeforeShow(BeforeShowEvent event) {
			this.clear();
			
			MenuItem modify = new MenuItem("Modify");
			modify.addSelectionHandler(new SelectionHandler<Item>() {
				@Override
				public void onSelection(SelectionEvent<Item> event) {
					LabelModifyDialog modifyDialog = new LabelModifyDialog(label);
					modifyDialog.show();
				}
			});
			this.add(modify);
			MenuItem remove = new MenuItem("Remove");
			remove.addSelectionHandler(new SelectionHandler<Item>() {
				@Override
				public void onSelection(SelectionEvent<Item> event) {
					eventBus.fireEvent(new LabelRemoveEvent(label));
				}
			});
			this.add(remove);
			
			if(collection.getLabels().size() > 1) {
				MenuItem merge = new MenuItem("Merge with");
				Menu mergeMenu = new Menu();
				VerticalPanel verticalPanel = new VerticalPanel();
				final List<Label> mergeLabels = new LinkedList<Label>();
				final TextButton mergeButton = new TextButton("Merge");
				mergeButton.setEnabled(false);
				for(final Label collectionLabel : collection.getLabels()) {
					if(!label.equals(collectionLabel)) {
						CheckBox checkBox = new CheckBox();
						checkBox.setBoxLabel(collectionLabel.getName());
						checkBox.setValue(false);
						checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
							@Override
							public void onValueChange(ValueChangeEvent<Boolean> event) {
								if(event.getValue())
									mergeLabels.add(collectionLabel);
								else
									mergeLabels.remove(collectionLabel);
								mergeButton.setEnabled(!mergeLabels.isEmpty());
							}
						});
						verticalPanel.add(checkBox);
					}
				}
				mergeButton.addSelectHandler(new SelectHandler() {
					@Override
					public void onSelect(SelectEvent event) {
						LabelMenu.this.hide();
						eventBus.fireEvent(new LabelsMergeEvent(label, mergeLabels));
					}
				});
				verticalPanel.add(mergeButton);
				mergeMenu.add(verticalPanel);
				merge.setSubMenu(mergeMenu);
				this.add(merge);
			}
			
			if(this.getWidgetCount() == 0)
				event.setCancelled(true);
		}
	}
	
	public static class LabelInfoContainer extends SimpleContainer {
		
		private TextField labelName;
		private TextArea labelDescription;

		public LabelInfoContainer(String initialName, String initialDescription) {
			FieldSet fieldSet = new FieldSet();
		    fieldSet.setHeadingText("Category Information");
		    fieldSet.setCollapsible(true);
		    this.add(fieldSet, new MarginData(10));
		 
		    VerticalLayoutContainer p = new VerticalLayoutContainer();
		    fieldSet.add(p);
		    
		    labelName = new TextField();
		    labelName.setAllowBlank(false);
		    labelName.setValue(initialName);
		    p.add(new FieldLabel(labelName, "Name"), new VerticalLayoutData(1, -1));
		 
		    labelDescription = new TextArea();
		    labelDescription.setValue(initialDescription);
		    labelDescription.setAllowBlank(true);
		    p.add(new FieldLabel(labelDescription, "Description"), new VerticalLayoutData(1, -1));
		}

		public TextField getLabelName() {
			return labelName;
		}

		public TextArea getLabelDescription() {
			return labelDescription;
		}
	}
	
	public class LabelModifyDialog extends Dialog {
		
		public LabelModifyDialog(final Label label) {
			this.setHeadingText("Modify Category");	
			LabelInfoContainer labelInfoContainer = new LabelInfoContainer(label.getName(), label.getDescription());
		    this.add(labelInfoContainer);
		 
		    final TextField labelName = labelInfoContainer.getLabelName();
		    final TextArea labelDescription = labelInfoContainer.getLabelDescription();
		    
		    getButtonBar().clear();
		    TextButton save = new TextButton("Save");
		    save.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					if(!labelName.validate()) {
						AlertMessageBox alert = new AlertMessageBox("Category Name", "A category name is required");
						alert.show();
						return;
					}
					eventBus.fireEvent(new LabelModifyEvent(label, labelName.getText(), labelDescription.getText()));
					LabelModifyDialog.this.hide();
				}
		    });
		    TextButton cancel =  new TextButton("Cancel");
		    cancel.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					LabelModifyDialog.this.hide();
				}
		    });
		    addButton(save);
		    addButton(cancel);
		}
	
	}
	
	private static final TextTreeNodeProperties textTreeNodeProperties = GWT.create(TextTreeNodeProperties.class);
	private TreeStore<TermTreeNode> portletStore;
	private Label label;
	private Tree<TermTreeNode, String> tree;
	private EventBus eventBus;
	private Map<Term, TermTreeNode> termTermTreeNodeMap = new HashMap<Term, TermTreeNode>();
	private Collection collection;

	public LabelPortlet(EventBus eventBus, Label label, Collection collection) {
		this(GWT.<FramedPanelAppearance> create(FramedPanelAppearance.class), eventBus, label, collection);
	}
	
	public LabelPortlet(FramedPanelAppearance appearance, EventBus eventBus, Label label, Collection collection) {
		super(appearance);
		this.eventBus = eventBus;
		this.label = label;
		this.collection = collection; 
		this.setHeadingText(label.getName());
		this.setExpanded(false);
		this.setAnimationDuration(500);
		this.setCollapsible(true);
		this.setAnimCollapse(false);
		
		final ToolButton toolButton = new ToolButton(ToolButton.GEAR);
		toolButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				LabelMenu menu = new LabelMenu(LabelPortlet.this.label);
				menu.show(toolButton);
			}});
		this.getHeader().addTool(toolButton);
		this.setContextMenu(new LabelMenu(label));
		
		portletStore = new TreeStore<TermTreeNode>(textTreeNodeProperties.key());
		portletStore.setAutoCommit(true);
		portletStore.addSortInfo(new StoreSortInfo<TermTreeNode>(new Comparator<TermTreeNode>() {
			@Override
			public int compare(TermTreeNode o1, TermTreeNode o2) {
				return o1.getText().compareTo(o2.getText());
			}
		}, SortDir.ASC));
		tree = new Tree<TermTreeNode, String>(portletStore, textTreeNodeProperties.text());
		tree.setContextMenu(new TermMenu());
		
		FlowLayoutContainer flowLayoutContainer = new FlowLayoutContainer();
		flowLayoutContainer.add(tree);
		flowLayoutContainer.setScrollMode(ScrollMode.AUTO);
		flowLayoutContainer.getElement().getStyle().setProperty("maxHeight", "200px");
		
		add(flowLayoutContainer);
		
		bindEvents();
		setupDnD();
		
		for(Term mainTerm : label.getMainTerms()) {
			addMainTerm(mainTerm);
			for(Term synonym : label.getSynonyms(mainTerm)) 
				this.addSynonymTerm(mainTerm, synonym);
		}
	}
	
	public void addMainTerm(Term term) {
		MainTermTreeNode mainTermTreeNode = new MainTermTreeNode(term);
		if(!termTermTreeNodeMap.containsKey(term))  {
			portletStore.add(mainTermTreeNode);
			this.termTermTreeNodeMap.put(term, mainTermTreeNode);
		}
	}
	
	protected void addSynonymTerm(Term mainTerm, Term synonymTerm) {
		MainTermTreeNode mainTermTreeNode = null;
		TermTreeNode termTreeNode = termTermTreeNodeMap.get(mainTerm);
		if(termTreeNode != null) {
			if(termTreeNode instanceof MainTermTreeNode) {
				mainTermTreeNode = (MainTermTreeNode)termTreeNode;
				SynonymTermTreeNode synonymTermTreeNode = new SynonymTermTreeNode(synonymTerm);
				removeTerm(synonymTerm);
				termTermTreeNodeMap.put(synonymTerm, synonymTermTreeNode);
				portletStore.add(mainTermTreeNode, synonymTermTreeNode);
				this.termTermTreeNodeMap.put(synonymTerm, synonymTermTreeNode);
			} 	
		}
	}
	
	protected void removeSynonymTerm(Term mainTerm, Term synonym) {
		TermTreeNode termTreeNode = termTermTreeNodeMap.remove(synonym);
		if(termTreeNode != null && termTreeNode instanceof SynonymTermTreeNode) {
			portletStore.remove(termTreeNode);
			this.addMainTerm(synonym);
		}
	}
		
	private void removeTerm(Term term) {
		if(termTermTreeNodeMap.containsKey(term)) {
			TermTreeNode termTreeNode = termTermTreeNodeMap.get(term);
			if(termTreeNode instanceof MainTermTreeNode) {
				MainTermTreeNode mainTermTreeNode = (MainTermTreeNode)termTreeNode;
				List<TermTreeNode> synonyms = portletStore.getChildren(mainTermTreeNode);
				for(TermTreeNode synonym : synonyms)
					this.removeSynonymTerm(term, synonym.getTerm());
			}
			portletStore.remove(termTermTreeNodeMap.remove(term));
		}
	}
	
	private void removeTerms(List<Term> terms) {
		for(Term term : terms)
			this.removeTerm(term);
	}

	private void bindEvents() {
		eventBus.addHandler(TermSelectEvent.TYPE, new TermSelectEvent.TermSelectHandler() {
			@Override
			public void onSelect(TermSelectEvent event) {
				Term term = event.getTerm();
				TermTreeNode termTreeNode = termTermTreeNodeMap.get(term);
				if(termTreeNode != null) {
					if(!tree.getSelectionModel().isSelected(termTreeNode)) {
						List<TermTreeNode> selectionTree = new LinkedList<TermTreeNode>();
						selectionTree.add(termTreeNode);
						tree.getSelectionModel().setSelection(selectionTree);
					}
					LabelPortlet.this.expand();
				}
			}
		});
		eventBus.addHandler(SynonymRemovalEvent.TYPE, new SynonymRemovalEvent.SynonymRemovalHandler() {
			@Override
			public void onSynonymRemoval(SynonymRemovalEvent event) {
				List<Term> synonyms = event.getSynonyms();
				Term mainTerm = event.getMainTerm();
				if(LabelPortlet.this.label.equals(label)) {
					for(Term synonym : synonyms) {
						LabelPortlet.this.removeSynonymTerm(mainTerm, synonym);
					}
				}
			}
		});
		eventBus.addHandler(SynonymCreationEvent.TYPE, new SynonymCreationEvent.SynonymCreationHandler() {
			@Override
			public void onSynonymCreation(SynonymCreationEvent event) {
				Term mainTerm = event.getMainTerm();
				List<Term> synonymTerms = event.getSynonymTerm();
				if(LabelPortlet.this.label.equals(label)) {
					for(Term synonymTerm : synonymTerms)
						LabelPortlet.this.addSynonymTerm(mainTerm, synonymTerm);
				}
			}
		});
		eventBus.addHandler(TermRenameEvent.TYPE, new TermRenameEvent.RenameTermHandler() {
			@Override
			public void onRename(TermRenameEvent event) {
				Term term = event.getTerm();
				if(termTermTreeNodeMap.get(term) != null) {
					if(portletStore.getAll().contains(termTermTreeNodeMap.get(term))) {
						portletStore.update(termTermTreeNodeMap.get(term));
					}
				}
			}
		});
		eventBus.addHandler(CategorizeCopyTermEvent.TYPE, new CategorizeCopyTermEvent.CategorizeCopyTermHandler() {
			@Override
			public void onCategorize(CategorizeCopyTermEvent event) {
				List<Term> terms = event.getTerms();
				List<Label> targetCategories = event.getTargetCategories();
				for(Label targetCategory : targetCategories) {
					if(targetCategory.equals(label)) {
						for(Term term : terms) 
							addMainTerm(term);
					}
				}
			}
		});
		eventBus.addHandler(LabelModifyEvent.TYPE, new LabelModifyEvent.ModifyLabelHandler()  {
			@Override
			public void onModify(LabelModifyEvent event) {
				if(label.equals(LabelPortlet.this.label))
					LabelPortlet.this.setHeadingText(label.getName());
			}
		});
		eventBus.addHandler(CategorizeMoveTermEvent.TYPE, new CategorizeMoveTermEvent.CategorizeMoveTermHandler() {
			@Override
			public void onCategorize(CategorizeMoveTermEvent event) {
				Label targetLabel = event.getTargetCategory();
				Label sourceLabel = event.getSourceCategory();
				List<Term> terms = event.getTerms();
				if(targetLabel.equals(label)) {
					for(Term term : terms)
						addMainTerm(term);
				}
				if(sourceLabel.equals(label)) {
					for(Term term : terms) {
						removeTerm(term);
					}
				}
			}
		});
		eventBus.addHandler(TermCategorizeEvent.TYPE, new TermCategorizeEvent.TermCategorizeHandler() {
			@Override
			public void onCategorize(TermCategorizeEvent event) {
				List<Label> labels = event.getCategories();
				List<Term> terms = event.getTerms();
				for(Label label : labels)
					if(LabelPortlet.this.label.equals(label)) {
						for(Term term : terms) {
							addMainTerm(term);
						}
					}
			}
		});
		eventBus.addHandler(TermUncategorizeEvent.TYPE, new TermUncategorizeEvent.TermUncategorizeHandler() {
			@Override
			public void onUncategorize(TermUncategorizeEvent event) {
				List<Label> oldLabels = event.getOldLabels();
				List<Term> terms = event.getTerms();
				for(Label oldLabel : oldLabels) {
					if(LabelPortlet.this.label.equals(oldLabel)) {
						for(Term term : terms)
							LabelPortlet.this.removeTerm(term);
					}
				}
			}
		});
		eventBus.addHandler(CategorizeCopyRemoveTermEvent.TYPE, new CategorizeCopyRemoveTermEvent.CategorizeCopyRemoveTermHandler() {
			@Override
			public void onRemove(CategorizeCopyRemoveTermEvent event) {
				Label label = event.getLabel();
				List<Term> terms = event.getTerms();
				if(LabelPortlet.this.label.equals(label)) {
					LabelPortlet.this.removeTerms(terms);
				}
			}
		});
		
		tree.getSelectionModel().addSelectionHandler(new SelectionHandler<TermTreeNode>() {
			@Override
			public void onSelection(SelectionEvent<TermTreeNode> event) {
				TermTreeNode termTreeNode = event.getSelectedItem();
				Term term = termTreeNode.getTerm();
				eventBus.fireEvent(new TermSelectEvent(term));
			}
		});
	}

	public class CopyMoveMenu extends Menu {
		
		public CopyMoveMenu(SelectionHandler<Item> copyHandler, SelectionHandler<Item> moveHandler) {
			MenuItem item = new MenuItem("Copy");
			item.addSelectionHandler(copyHandler);
			add(item);
			item = new MenuItem("Move");
			item.addSelectionHandler(moveHandler);
			add(item);
		}
		
	}
	
	private void setupDnD() {
		TreeDragSource<TermTreeNode> dragSource = new TreeDragSource<TermTreeNode>(tree);
		
		final DndDropHandler portalDropHandler = new DndDropHandler() {
			@Override
			public void onDrop(DndDropEvent event) {
				if(DndDropEventExtractor.isSourceCategorizeView(event)) {
					onDnd(event, DropSource.INIT);
				}
				if(DndDropEventExtractor.isSourceLabelOtherPortlet(event, LabelPortlet.this)) {
					onDnd(event, DropSource.PORTLET);
				}
			}

			private void onDnd(final DndDropEvent dropEvent, DropSource source) {
				switch(source) {
				case INIT:
					List<Term> terms = DndDropEventExtractor.getTerms(dropEvent);
					eventBus.fireEvent(new TermCategorizeEvent(terms, label));
					LabelPortlet.this.expand();
					break;
				case PORTLET:
					Menu menu = new CopyMoveMenu(new SelectionHandler<Item>() {
						@Override
						public void onSelection(SelectionEvent<Item> event) {
							LabelPortlet sourcePortlet = DndDropEventExtractor.getLabelPortletSource(dropEvent);
							List<Term> terms = DndDropEventExtractor.getTerms(dropEvent);
							
							eventBus.fireEvent(new CategorizeCopyTermEvent(terms, sourcePortlet.getLabel(), label));
						}
					}, new SelectionHandler<Item>() {
						@Override
						public void onSelection(SelectionEvent<Item> event) {
							LabelPortlet sourcePortlet = DndDropEventExtractor.getLabelPortletSource(dropEvent);
							List<Term> terms = DndDropEventExtractor.getTerms(dropEvent);
							eventBus.fireEvent(new CategorizeMoveTermEvent(terms, sourcePortlet.getLabel(), label));
						}
					});
					menu.show(LabelPortlet.this);
					LabelPortlet.this.expand();
					break;
				default:
					break;
				}
			}
		};
		DropTarget dropTarget = new DropTarget(this);
		dropTarget.setAllowSelfAsSource(false);
		dropTarget.setOperation(Operation.COPY);
		dropTarget.addDropHandler(portalDropHandler);
		
		// let our events take care of tree/list store updates, hence own
		// implementation to take care of move/copy		
		final StoreTargetTreeDropTarget<TermTreeNode> treeDropTarget = new StoreTargetTreeDropTarget<TermTreeNode>(
				tree);
		treeDropTarget.setAllowDropOnLeaf(true);
		treeDropTarget.setAllowSelfAsSource(true);
		treeDropTarget.setOperation(Operation.COPY);
		treeDropTarget.addDropHandler(new DndDropHandler() {
			@Override
			public void onDrop(DndDropEvent event) {
				Object data = event.getData();
				List<Term> synonymTerms = new LinkedList<Term>();
				if (data instanceof List) {
					List<?> list = (List<?>) data;
					if (list.get(0) instanceof TreeStore.TreeNode) {
						@SuppressWarnings("unchecked")
						List<TreeNode<TermTreeNode>> nodes = (List<TreeNode<TermTreeNode>>) list;
						for (TreeNode<TermTreeNode> node : nodes) {
							//drops from bucket tree are TermTreeNode, SynonymTermTreeNode can't be used as synonym again until made mainterm 
							if (node.getData() != null && node.getData() instanceof TermTreeNode && !(node.getData() instanceof SynonymTermTreeNode)) {
								synonymTerms.add(((TermTreeNode) node.getData()).getTerm());
							}					
						}
						//drops from bucket list
					} else if(list.get(0) instanceof Term) {
						List<Term> nodes = (List<Term>)list;
						synonymTerms.addAll(nodes);
					}
					
				}
				
				
				//not a target of this portlet, don't allow for now
				for(Term synonymTerm : synonymTerms)
					if(!label.containsTerm(synonymTerm)) {
						portalDropHandler.onDrop(event);
						return;
					}
						
				TermTreeNode target = treeDropTarget.getAndNullTarget();
				if(target != null) { 
					Term mainLabelTerm = target.getTerm();
					label.setSynonymy(mainLabelTerm, synonymTerms);
					eventBus.fireEvent(new SynonymCreationEvent(label,
							mainLabelTerm, synonymTerms));
				}
			}
		});
	}
	
	public Label getLabel() {
		return label;
	}
	
	public void setCollection(Collection collection) {
		this.collection = collection;
	}

	public boolean containsMainTerm(Term term) {
		if(termTermTreeNodeMap.containsKey(term)) 
			return termTermTreeNodeMap.get(term) instanceof MainTermTreeNode;
		return false;
	}	

}