package edu.arizona.biosemantics.oto2.ontologize.client.content;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;

import edu.arizona.biosemantics.oto2.ontologize.client.content.submission.EditSubmissionView;
import edu.arizona.biosemantics.oto2.ontologize.client.content.submissions.ClassSubmissionsGrid;
import edu.arizona.biosemantics.oto2.ontologize.client.content.submissions.SubmissionsView;
import edu.arizona.biosemantics.oto2.ontologize.client.content.submissions.SynonymSubmissionsGrid;
import edu.arizona.biosemantics.oto2.ontologize.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.ontologize.shared.model.toontology.OntologySynonymSubmission;
import edu.arizona.biosemantics.oto2.ontologize.shared.model.toontology.SubmissionType;

public class BioportalSubmissionsView implements IsWidget {

	private BorderLayoutContainer borderLayoutContainer;
	private SubmissionsView submissionsView;
	private EditSubmissionView editSubmissionView;

	public BioportalSubmissionsView(EventBus eventBus) {
		submissionsView = new SubmissionsView(eventBus, SubmissionType.BIOPORTAL);
		editSubmissionView = new EditSubmissionView(eventBus, false, false, false, true, false, true);
			
		borderLayoutContainer = new BorderLayoutContainer();
		ContentPanel cp = new ContentPanel();
		cp.setHeading("Ontology Submissions");
		cp.add(submissionsView);
		BorderLayoutData d = new BorderLayoutData(0.5);
		d.setMargins(new Margins(0, 1, 1, 1));
		d.setCollapsible(true);
		d.setSplit(true);
		d.setCollapseMini(true);
		borderLayoutContainer.setWestWidget(cp, d);
		
		d = new BorderLayoutData(0.5);
		d.setMargins(new Margins(0, 1, 1, 1));
		d.setCollapsible(true);
		d.setSplit(true);
		d.setCollapseMini(true);
		borderLayoutContainer.setCenterWidget(editSubmissionView, d);
	}

	@Override
	public Widget asWidget() {
		return borderLayoutContainer;
	}

	public void refreshGridHeaders() {
		submissionsView.refreshGridHeaders();
	}
}
