package com.gitplex.symbolextractor.java.symbols.ui;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.symbolextractor.java.symbols.FieldDef;
import com.gitplex.symbolextractor.util.HighlightableLabel;

@SuppressWarnings("serial")
public class FieldDefPanel extends Panel {

	private final FieldDef fieldDef;
	
	private final Pair<Integer, Integer> highlight;
	
	public FieldDefPanel(String id, FieldDef fieldDef, Pair<Integer, Integer> highlight) {
		super(id);
		this.fieldDef = fieldDef;
		this.highlight = highlight;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new HighlightableLabel("name", fieldDef.getName(), highlight));
		
		add(new Label("type", fieldDef.getType()).setVisible(fieldDef.getType()!=null));
	}

}
