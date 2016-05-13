package edu.arizona.biosemantics.oto2.ontologize2.client.relations.cell;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.menu.Menu;

import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid.Row;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.cell.AttachedTermCell.Templates;

public class LeadTermCell extends MenuExtendedCell<String> {
	
	interface Templates extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<div class=\"{0}\" qtip=\"{4}\">" +
				"<div class=\"{1}\" " +
				"style=\"" +
				"width: calc(100% - 9px); " +
				"height:14px; " +
				"background: no-repeat 0 0;" +
				"background-image:{6};" +
				"background-color:{5};" +
				"\">{3}<a class=\"{2}\" style=\"height: 22px;\"></a>" +
				"</div>" +
				"</div>")
		SafeHtml cell(String grandParentStyleClass, String parentStyleClass,
				String aStyleClass, String value, String quickTipText, String colorHex, String backgroundImage);
	}
	
	protected static Templates templates = GWT.create(Templates.class);
	
	@Override
	protected Menu createContextMenu(int column, int row) {
		Menu menu = new Menu();
		menu.add(new Label("test"));
		return menu;
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		SafeHtml rendered = templates.cell("", columnHeaderStyles.headInner(),
				columnHeaderStyles.headButton(), value, "", "", "");
		sb.append(rendered);
	}

}