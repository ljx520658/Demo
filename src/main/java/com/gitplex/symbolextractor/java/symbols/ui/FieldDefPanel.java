package com.gitplex.symbolextractor.java.symbols.ui;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.java.symbols.FieldDef;
import com.gitplex.symbolextractor.util.HighlightableLabel;

@SuppressWarnings("serial")
public class FieldDefPanel extends Panel {

	private final FieldDef fieldDef;
	
	private final Range highlight;
	
	public FieldDefPanel(String id, FieldDef fieldDef, Range highlight) {
		super(id);
		this.fieldDef = fieldDef;
		this.highlight = highlight;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new HighlightableLabel("name", fieldDef.getIndexName(), highlight));
		add(new Label("type", fieldDef.getType()).setVisible(fieldDef.getType()!=null));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new JavaSymbolResourceReference()));
	}

}
