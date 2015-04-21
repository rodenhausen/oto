package edu.arizona.biosemantics.oto2.steps.client.toontology;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;

import edu.arizona.biosemantics.oto2.steps.client.common.Alerter;
import edu.arizona.biosemantics.oto2.steps.client.common.CreateOntologyDialog;
import edu.arizona.biosemantics.oto2.steps.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.OntologySynonymSubmissionSelectEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.SubmitSynonymEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.TermSelectEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;
import edu.arizona.biosemantics.oto2.steps.shared.model.OntologyProperties;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;
import edu.arizona.biosemantics.oto2.steps.shared.model.TermProperties;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.IToOntologyService;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.IToOntologyServiceAsync;

public class SubmitSynonymView implements IsWidget {
	
	private OntologyProperties ontologyProperties = GWT.create(OntologyProperties.class);
	private TermProperties termProperties = GWT.create(TermProperties.class);
	private IToOntologyServiceAsync toOntologyService = GWT.create(IToOntologyService.class);
	private EventBus eventBus;
	private Collection collection;
	
	private ListStore<Ontology> ontologiesStore = new ListStore<Ontology>(ontologyProperties.key());
	private ListStore<Term> termStore = new ListStore<Term>(termProperties.key());
	private TextButton submitButton = new TextButton("Submit");
	private TextField submissionTermField = new TextField();
	private TextField categoryField = new TextField();
	private TextButton browseOntologiesButton = new TextButton("Browse Ontology");
	private ComboBox<Ontology> ontologyComboBox;
	private TextField classIRIField = new TextField();
	private TextField synonymsField = new TextField();
	private TextField sourceField = new TextField();
	private TextArea sampleArea = new TextArea();
	private CheckBox isEntityCheckBox = new CheckBox();
	private CheckBox isQualityCheckBox = new CheckBox();
	private ComboBox<Term> termComboBox;
	private VerticalLayoutContainer vlc;
	private TextButton createOntologyButton = new TextButton("Create");
	
	public SubmitSynonymView(EventBus eventBus) {
		this.eventBus = eventBus;
		
	    ontologyComboBox = new ComboBox<Ontology>(ontologiesStore, ontologyProperties.prefixLabel());
	    ontologyComboBox.setAllowBlank(true);
	    ontologyComboBox.setForceSelection(false);
	    ontologyComboBox.setTriggerAction(TriggerAction.ALL);
	    
	    termComboBox = new ComboBox<Term>(termStore, termProperties.nameLabel());
	    categoryField.setEnabled(false);
	    
	    vlc = new VerticalLayoutContainer();
	    VerticalLayoutContainer formContainer = new VerticalLayoutContainer();
	    formContainer.add(new FieldLabel(termComboBox, "Candiate Term"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(submissionTermField, "Term"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(categoryField, "Category"), new VerticalLayoutData(1, -1));
	    formContainer.add(browseOntologiesButton, new VerticalLayoutData(1, -1));
	    VerticalLayoutContainer ontologyVlc = new VerticalLayoutContainer();
	    ontologyVlc.add(createOntologyButton, new VerticalLayoutData(1, -1));
	    ontologyVlc.add(ontologyComboBox, new VerticalLayoutData(1, -1));
	    ontologyVlc.add(browseOntologiesButton, new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(ontologyVlc, "Ontology"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(classIRIField, "Class IRI"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(synonymsField, "Synonyms"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(sourceField, "Source"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(sampleArea, "Sample Sentence"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(isEntityCheckBox, "Entity"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(isQualityCheckBox, "Quality"), new VerticalLayoutData(1, -1));
	    formContainer.add(submitButton, new VerticalLayoutData(1, -1));
	    formContainer.setScrollMode(ScrollMode.AUTO);
	    formContainer.setAdjustForScroll(true);
	    vlc.add(formContainer, new VerticalLayoutData(1, 1));
	    vlc.add(submitButton, new VerticalLayoutData(1,-1));
		
		bindEvents();		
	}

	private void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				SubmitSynonymView.this.collection = event.getCollection();
				
				initCollection();
			}
		});
		eventBus.addHandler(TermSelectEvent.TYPE, new TermSelectEvent.Handler() {
			@Override
			public void onSelect(TermSelectEvent event) {
				termComboBox.setValue(event.getTerm(), true);
			}
		});
		eventBus.addHandler(OntologySynonymSubmissionSelectEvent.TYPE, new OntologySynonymSubmissionSelectEvent.Handler() {
			@Override
			public void onSelect(OntologySynonymSubmissionSelectEvent event) {
				setOntologySynonymSubmission(event.getOntologySynonymSubmission());
			}
		});
		termComboBox.addValueChangeHandler(new ValueChangeHandler<Term>() {
			@Override
			public void onValueChange(ValueChangeEvent<Term> event) {
				eventBus.fireEvent(new TermSelectEvent(event.getValue()));
				categoryField.setValue(termComboBox.getValue().getCategory());
				submissionTermField.setValue(termComboBox.getValue().getTerm());
			}
		});
		createOntologyButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				final CreateOntologyDialog dialog = new CreateOntologyDialog();
				dialog.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
					@Override
					public void onSelect(SelectEvent event) {
						Ontology ontology = new Ontology();
						ontology.setName(dialog.getName());
						ontology.setAcronym(dialog.getAcronym());
						ontology.setTaxonGroups(dialog.getTaxonGroups());
						ontology.setCollectionId(collection.getId());
						
						toOntologyService.createOntology(collection, ontology, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Alerter.failedToCreateOntology();
							}
							@Override
							public void onSuccess(Void result) {
								Alerter.succesfulCreatedOntology();
								refreshOntologies();
							}
						});
					}
				});
				dialog.getButton(PredefinedButton.CANCEL).addSelectHandler(new SelectHandler() {
					@Override
					public void onSelect(SelectEvent event) { }
				});
				dialog.show();
			}
		});
		browseOntologiesButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				if(ontologyComboBox.getValue() == null) {
					Alerter.noOntologySelected();
					return;
				}
				if(	ontologyComboBox.getValue().getBrowseURL() != null && !ontologyComboBox.getValue().getBrowseURL().isEmpty())
					Window.open(ontologyComboBox.getValue().getBrowseURL(), "_blank", "");
				else
					Alerter.failedToBrowseOntology();
			} 
		});
		
		submitButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				OntologySynonymSubmission submission = getSynonymSubmission();
				toOntologyService.submitSynonym(submission, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						Alerter.failedToSubmitSynonym(caught);
					}

					@Override
					public void onSuccess(Void result) {
					} 
				});
				eventBus.fireEvent(new SubmitSynonymEvent(submission));
			}
		});
	}

	protected void setOntologySynonymSubmission(
			OntologySynonymSubmission ontologySynonymSubmission) {
		this.termComboBox.setValue(ontologySynonymSubmission.getTerm());
		this.submissionTermField.setValue(ontologySynonymSubmission.getSubmissionTerm()); 
		this.ontologyComboBox.setValue(ontologySynonymSubmission.getOntology());
		this.classIRIField.setValue(ontologySynonymSubmission.getClassIRI());
		this.synonymsField.setValue(ontologySynonymSubmission.getSynonyms());
		this.sourceField.setValue(ontologySynonymSubmission.getSource());
		this.sampleArea.setValue(ontologySynonymSubmission.getSampleSentence());
		this.isEntityCheckBox.setValue(ontologySynonymSubmission.isEntity());
		this.isQualityCheckBox.setValue(ontologySynonymSubmission.isQuality());
	}

	protected OntologySynonymSubmission getSynonymSubmission() {
		return new OntologySynonymSubmission(termComboBox.getValue(), submissionTermField.getValue(), 
				ontologyComboBox.getValue(), classIRIField.getValue(),
				synonymsField.getValue(), sourceField.getValue(), sampleArea.getValue(), 
				isEntityCheckBox.getValue(), isQualityCheckBox.getValue());
	}

	protected void initCollection() {
		refreshOntologies();
		refreshTerms();
	}

	private void refreshTerms() {
		termStore.clear();
		termStore.addAll(collection.getTerms());
	}

	private void refreshOntologies() {
		toOntologyService.getOntologies(collection, new AsyncCallback<List<Ontology>>() {
			@Override
			public void onFailure(Throwable caught) {
				Alerter.getOntologiesFailed(caught);
			}

			@Override
			public void onSuccess(List<Ontology> result) {
				ontologiesStore.clear();
				ontologiesStore.addAll(result);
			}
		});
	}

	@Override
	public Widget asWidget() {
		return vlc;
	}

}
