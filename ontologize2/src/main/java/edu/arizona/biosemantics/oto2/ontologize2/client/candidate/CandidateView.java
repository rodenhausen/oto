package edu.arizona.biosemantics.oto2.ontologize2.client.candidate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.util.DelayedTask;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.Store.StoreFilter;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.TreeDragSource;
import com.sencha.gxt.messages.client.DefaultMessages;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.MultiLinePromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent;
import com.sencha.gxt.widget.core.client.event.CheckChangeEvent;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent.BeforeShowHandler;
import com.sencha.gxt.widget.core.client.event.CheckChangeEvent.CheckChangeHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.tree.Tree;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.common.TextAreaMessageBox;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateCandidateEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.FilterEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.FilterEvent.FilterTarget;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.ClearEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveCandidateEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.SelectTermEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.MenuTermsGrid;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid.Row;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.BucketTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.CandidateTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.TextTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.TextTreeNodeProperties;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;

public class CandidateView extends SimpleContainer {

	private static final TextTreeNodeProperties textTreeNodeProperties = GWT.create(TextTreeNodeProperties.class);
	
	
	
	private Tree<TextTreeNode, TextTreeNode> tree;
	private TreeStore<TextTreeNode> treeStore;
	private Map<String, CandidateTreeNode> candidateNodeMap = new HashMap<String, CandidateTreeNode>();
	private Map<String, BucketTreeNode> bucketNodesMap = new HashMap<String, BucketTreeNode>();	
	private EventBus eventBus;
	private ToolBar buttonBar;

	private CheckMenuItem checkFilterItem;
	private TextField filterField;
	
	private DelayedTask addCandidatesTask = new DelayedTask() {
		@Override
		public void onExecute() {
			final String newTerms = addTermsField.getValue().trim();
			if(newTerms.isEmpty()) {
				Alerter.showAlert("Add Term", "Term field is empty");
				return;
			}
			
			String[] newTermsArray = newTerms.split(",");
			List<Candidate> candidates = new LinkedList<Candidate>();
			for(String newTerm : newTermsArray) {
				int lastSeparatorIndex = newTerm.lastIndexOf("/");
				if(newTerm.length() == lastSeparatorIndex + 1) {
					Alerter.showAlert("Add term", "Malformed input to add term");
					return;
				}
				
				String term = newTerm.trim();
				String path = ""; 
				if(lastSeparatorIndex != -1) {
					term = newTerm.substring(lastSeparatorIndex + 1).trim();
					path = newTerm.substring(0, lastSeparatorIndex).trim();	
				}
				
				if(ModelController.getCollection().contains(term)) {
					String termPath = ModelController.getCollection().getCandidates().getPath(term);
					if(termPath == null)
						termPath = "/";
					Alerter.showAlert("Candidate exists", "Candidate <i>" + term + "</i> already exists at <i>" +
							termPath + "</i>");
					return;
				} else {
					if(path.isEmpty()) {
						/*BucketTreeNode bucketNode = getSelectedBucket();
						if(bucketNode != null)
							path = bucketNode.getPath();*/
					} else {
						if(!path.startsWith("/"))
							path = "/" + path;
					}
					candidates.add(new Candidate(term, path));
				}
			}
			addTermsField.setText("");
			eventBus.fireEvent(new CreateCandidateEvent(candidates));
		}
	};
	
	private DelayedTask filterTask = new DelayedTask() {		
		@Override
		public void onExecute() {
			String filter = filterField.getText().trim();
			if(filter.isEmpty())
				checkFilterItem.setChecked(false);
			else
				checkFilterItem.setChecked(true);
			onFilter(filter);
		}
	};



	private TextField addTermsField;
	
	private CandidateView() {
		treeStore = new TreeStore<TextTreeNode>(textTreeNodeProperties.key());
		treeStore.setAutoCommit(true);
		treeStore.addSortInfo(new StoreSortInfo<TextTreeNode>(new Comparator<TextTreeNode>() {
			@Override
			public int compare(TextTreeNode o1, TextTreeNode o2) {
				return o1.getText().compareTo(o2.getText());
			}
		}, SortDir.ASC));
		tree = new Tree<TextTreeNode, TextTreeNode>(treeStore, new IdentityValueProvider<TextTreeNode>());
		tree.setIconProvider(new TermTreeNodeIconProvider());
		tree.setCell(new AbstractCell<TextTreeNode>() {
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context,	TextTreeNode value, SafeHtmlBuilder sb) {
				OntologyGraph g = ModelController.getCollection().getGraph();
				if(g.getVertex(value.getText()) != null)
					sb.append(SafeHtmlUtils.fromTrustedString("<div style=\"color:gray;\">" + value.getText() +  "</div>"));
				else
					sb.append(SafeHtmlUtils.fromTrustedString("<div >" + value.getText() +  "</div>"));
			}
		});
		tree.getElement().setAttribute("source", "termsview");
		tree.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
		/*tree.getSelectionModel().addSelectionHandler(new SelectionHandler<TextTreeNode>() {
			@Override
			public void onSelection(SelectionEvent<TextTreeNode> event) {
				eventBus.fireEvent(new SelectTermEvent(event.getSelectedItem().getText()));
			}
		});*/
		tree.setAutoExpand(true);
		tree.setContextMenu(createContextMenu());
		
		TreeDragSource<TextTreeNode> dragSource = new TreeDragSource<TextTreeNode>(tree) {
			@Override
			protected void onDragStart(DndDragStartEvent event) {
				super.onDragStart(event);
				List<Candidate> data = new LinkedList<Candidate>();
				for(TextTreeNode node : tree.getSelectionModel().getSelectedItems()) {
					addTermTreeNodes(node, data);
				}
				event.setData(data);
			}
		};
		
		buttonBar = new ToolBar();
		
		TextButton filterButton = new TextButton("Filter");
		final Menu menu = new Menu();
		checkFilterItem = new CheckMenuItem(DefaultMessages.getMessages().gridFilters_filterText());
		menu.add(checkFilterItem);
		
		final Menu filterMenu = new Menu();
		filterField = new TextField() {
			protected void onKeyUp(Event event) {
				super.onKeyUp(event);
				int key = event.getKeyCode();
				if (key == KeyCodes.KEY_ENTER) {
					event.stopPropagation();
					event.preventDefault();
					filterMenu.hide(true);
				}
				
				filterTask.delay(500);
			}
		};
		checkFilterItem.setSubMenu(filterMenu);
		checkFilterItem.addCheckChangeHandler(new CheckChangeHandler<CheckMenuItem>() {
	        @Override
	        public void onCheckChange(CheckChangeEvent<CheckMenuItem> event) {
	        	if(!checkFilterItem.isChecked())
	        		filterField.setText("");
	        	onFilter(filterField.getText());
	        }
	    });
		filterMenu.add(filterField);
		filterButton.setMenu(menu);
		buttonBar.add(filterButton);
		
//		TextButton importButton = new TextButton("Import");
//		importButton.addSelectHandler(new SelectHandler() {
//			@Override
//			public void onSelect(SelectEvent event) {
//				final TextAreaMessageBox box = new TextAreaMessageBox("Import terms", "");
//				/*box.setResizable(true);
//				box.setResize(true);
//				box.setMaximizable(true);*/
//				box.setModal(true);
//				box.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
//					@Override
//					public void onSelect(SelectEvent event) {
//						String input = box.getValue();
//						String[] lines = input.split("\\n");
//						List<Candidate> candidates = new LinkedList<Candidate>();
//						for(String line : lines) {
//							String[] candidatePath = line.split(",");
//							if(candidatePath.length == 1) {
//								String candidate = candidatePath[0];
//								if(!ModelController.getCollection().getCandidates().contains(candidate))
//									candidates.add(new Candidate(candidate));
//								else
//									Alerter.showAlert("Candidate exists", "Candidate + \"" + candidate + "\" already exists at \"" +
//											ModelController.getCollection().getCandidates().getPath(candidate) + "\"");
//							} else if(candidatePath.length >= 2) {
//								String candidate = candidatePath[0];
//								if(!ModelController.getCollection().getCandidates().contains(candidate))
//									candidates.add(new Candidate(candidatePath[0], candidatePath[1]));
//								else
//									Alerter.showAlert("Candidate exists", "Candidate + \"" + candidate + "\" already exists at \"" +
//											ModelController.getCollection().getCandidates().getPath(candidate) + "\"");
//							}
//						}
//						
//						eventBus.fireEvent(new CreateCandidateEvent(candidates));
//					}
//				});
//				box.show();
//			}
//		});
		
		TextButton removeButton = new TextButton("Remove");
		Menu removeMenu = new Menu();
		MenuItem selectedRemove = new MenuItem("Selected");
		selectedRemove.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				removeNodes(tree.getSelectionModel().getSelectedItems());
			}
		});
		
		MenuItem allRemove = new MenuItem("All");
		allRemove.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				removeNodes(tree.getStore().getAll());
			}
		});
		removeMenu.add(selectedRemove);
		removeMenu.add(allRemove);
		removeButton.setMenu(removeMenu);
		
		//buttonBar.add(importButton);
		buttonBar.add(removeButton);
		
		HorizontalLayoutContainer hlc = new HorizontalLayoutContainer();
		addTermsField = new TextField() {
			protected void onKeyUp(Event event) {
				super.onKeyUp(event);
				int key = event.getKeyCode();
				if (key == KeyCodes.KEY_ENTER) {
					event.stopPropagation();
					event.preventDefault();
					addCandidatesTask.delay(200);
				}
			}
		};	
		TextButton addButton = new TextButton("Add");
		addButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				addCandidatesTask.delay(200);
			}
		});
		hlc.add(addTermsField, new HorizontalLayoutData(1, -1));
		hlc.add(addButton);
		
		FieldLabel field = new FieldLabel(hlc, "Add Term");
		
		VerticalLayoutContainer vlc = new VerticalLayoutContainer();
		vlc.add(buttonBar, new VerticalLayoutData(1, -1));
		vlc.add(tree, new VerticalLayoutData(1, 1));
		vlc.add(field, new VerticalLayoutData(1, 25));
		this.add(vlc);
	}
	
	protected void onFilter(final String text) {
		if(checkFilterItem.isChecked()) {
			treeStore.removeFilters();
			treeStore.addFilter(new StoreFilter<TextTreeNode>() {
				@Override
				public boolean select(Store<TextTreeNode> store, TextTreeNode parent, TextTreeNode item) {
					return item.getText().contains(text);
				}
			});
			treeStore.setEnableFilters(true);
		} else {
			treeStore.removeFilters();
			treeStore.setEnableFilters(false);
		}
	}

	private Menu createContextMenu() {
		final Menu menu = new Menu();
		menu.addBeforeShowHandler(new BeforeShowHandler() {
			@Override
			public void onBeforeShow(BeforeShowEvent event) {
				menu.clear();
				
				if(!tree.getSelectionModel().getSelectedItems().isEmpty()) {
					final String text = tree.getSelectionModel().getSelectedItem().getText();
					
					MenuItem filterItem = new MenuItem("Filter: " + text);
					Menu filterMenu = new Menu();
					filterItem.setSubMenu(filterMenu);
					for(final FilterTarget filterTarget : FilterTarget.values()) {
						MenuItem menuItem = new MenuItem(filterTarget.getDisplayName());
						menuItem.addSelectionHandler(new SelectionHandler<Item>() {
							@Override
							public void onSelection(SelectionEvent<Item> event) {
									eventBus.fireEvent(new FilterEvent(text, 
											filterTarget, Type.values()));
							}
						});
						filterMenu.add(menuItem);
					}
					MenuItem context = new MenuItem("Show Term Context");
					context.addSelectionHandler(new SelectionHandler<Item>() {
						@Override
						public void onSelection(SelectionEvent<Item> event) {
							eventBus.fireEvent(new SelectTermEvent(text));
						}
					});
					menu.add(filterItem);
					menu.add(context);
				}
				event.setCancelled(menu.getWidgetCount() == 0);
			}
		});		
		return menu;
	}

	private BucketTreeNode getSelectedBucket() {
		List<TextTreeNode> selection = tree.getSelectionModel().getSelectedItems();
		if(!selection.isEmpty()) {
			TextTreeNode node = selection.get(0);
			if(node instanceof BucketTreeNode) {
				return (BucketTreeNode)node;
			}
			if(node instanceof CandidateTreeNode) {
				TextTreeNode parent = tree.getStore().getParent(node);
				if(parent instanceof BucketTreeNode)
					return (BucketTreeNode) parent;
			}
		}
		return null;
	}
	
	protected void removeNodes(List<TextTreeNode> nodes) {
		final List<Candidate> remove = new LinkedList<Candidate>();
		for(TextTreeNode node : nodes) {
			if(node instanceof BucketTreeNode) {
				addTerms((BucketTreeNode)node, remove);
			}
			if(node instanceof CandidateTreeNode) {
				remove.add(((CandidateTreeNode)node).getCandidate());
			}
		}
		eventBus.fireEvent(new RemoveCandidateEvent(remove));
	}

	private void addTerms(BucketTreeNode node, List<Candidate> list) {
		for(TextTreeNode childNode : tree.getStore().getChildren(node)) {
			if(childNode instanceof CandidateTreeNode) {
				list.add(((CandidateTreeNode)childNode).getCandidate());
			} else if(childNode instanceof BucketTreeNode) {
				this.addTerms((BucketTreeNode)childNode, list);
			}
		}
	}

	protected void addTermTreeNodes(TextTreeNode node, List<Candidate> data) {
		if(node instanceof BucketTreeNode) {
			for(TextTreeNode child : tree.getStore().getChildren(node)) {
				this.addTermTreeNodes(child, data);
			}
		} else if(node instanceof CandidateTreeNode) {
			Candidate candidate = ((CandidateTreeNode)node).getCandidate();
			data.add(candidate);
		}
	}

	public CandidateView(EventBus eventBus) {
		this();
		this.eventBus = eventBus;
		
		bindEvents();
	}
	
	private void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				if(!event.isEffectiveInModel())
					setCollection(event.getCollection());
			}
		}); 
		eventBus.addHandler(CreateCandidateEvent.TYPE, new CreateCandidateEvent.Handler() {
			@Override
			public void onCreate(CreateCandidateEvent event) {
				add(Arrays.asList(event.getCandidates()));
			}
		});
		eventBus.addHandler(RemoveCandidateEvent.TYPE, new RemoveCandidateEvent.Handler() {
			@Override
			public void onRemove(RemoveCandidateEvent event) {
				remove(Arrays.asList(event.getCandidates()));
			}
		});
		eventBus.addHandler(CreateRelationEvent.TYPE, new CreateRelationEvent.Handler() {
			@Override
			public void onCreate(CreateRelationEvent event) {
				if(event.isEffectiveInModel()) {
					for(Edge e : event.getRelations()) {
						String[] nodes = new String[] { e.getSrc().getValue(), e.getDest().getValue() };
						for(String node : nodes) {
							if(candidateNodeMap.containsKey(node)) {
								treeStore.update(candidateNodeMap.get(node));
							}
						}
					}
				}
			}
		});
		eventBus.addHandler(RemoveRelationEvent.TYPE, new RemoveRelationEvent.Handler() {
			@Override
			public void onRemove(RemoveRelationEvent event) {
				if(event.isEffectiveInModel()) {
					OntologyGraph g = ModelController.getCollection().getGraph();
					for(Edge e : event.getRelations()) {
						String[] nodes = new String[] { e.getSrc().getValue(), e.getDest().getValue() };
						for(String node : nodes) {
							if(candidateNodeMap.containsKey(node)) {
								if(g.getVertex(node) == null)
									treeStore.update(candidateNodeMap.get(node));
							}
						}
					}
				}
			}
		});
		eventBus.addHandler(ClearEvent.TYPE, new ClearEvent.Handler() {
			@Override
			public void onClear(ClearEvent event) {
				if(event.isEffectiveInModel()) {
					for(TextTreeNode node : treeStore.getAll())
						treeStore.update(node);
				}
			}
		});
	}
	
	public void setCollection(Collection collection) {
		tree.getStore().clear();
		candidateNodeMap.clear();
		bucketNodesMap.clear();
		add(collection.getCandidates());
	}

	protected void remove(Iterable<Candidate> candidates) {
		for(Candidate candidate : candidates)
			if(candidateNodeMap.containsKey(candidate.getText())) {
				tree.getStore().remove(candidateNodeMap.get(candidate.getText()));
				candidateNodeMap.remove(candidate.getText());
			}
	}

	private void add(Iterable<Candidate> candidates) {
		for(Candidate candidate : candidates) {
			createBucketNodes(bucketNodesMap, candidate.getPath());
			addTermTreeNode(bucketNodesMap.get(candidate.getPath()), new CandidateTreeNode(candidate));
		}
	}

	protected void createBucketNodes(Map<String, BucketTreeNode> bucketsMap, String path) {
		if(path == null) 
			return;
		String[] buckets = path.split("/");
		String cumulativePath = "";
		String parentPath = "";
		for(String bucket : buckets) {
			if(!bucket.isEmpty()) {
				cumulativePath += "/" + bucket;
				if(!bucketsMap.containsKey(cumulativePath)) {
					BucketTreeNode bucketTreeNode = new BucketTreeNode(cumulativePath);
					if(parentPath.isEmpty())
						tree.getStore().add(bucketTreeNode);
					else
						tree.getStore().add(bucketsMap.get(parentPath), bucketTreeNode);
					bucketsMap.put(cumulativePath, bucketTreeNode);
				}
				parentPath = cumulativePath;
			}
		}
	}

	protected void addTermTreeNode(BucketTreeNode bucketNode, CandidateTreeNode candidateTreeNode) {
		this.candidateNodeMap.put(candidateTreeNode.getCandidate().getText(), candidateTreeNode);
		if(bucketNode == null)
			this.tree.getStore().add(candidateTreeNode);
		else
			this.tree.getStore().add(bucketNode, candidateTreeNode);
	}
	
	private void initializeCollapsing(Map<String, BucketTreeNode> bucketTreeNodes) {
//		for(BucketTreeNode node : bucketTreeNodes.values()) {
//			if(tree.getStore().getChildren(node).get(0) instanceof TermTreeNode) {
//				tree.setExpanded(node, false);
//			} else {
//				tree.setExpanded(node, true);
//			}
//		}
	}	
}
