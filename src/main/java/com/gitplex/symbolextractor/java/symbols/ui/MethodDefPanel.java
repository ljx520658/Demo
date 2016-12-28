package com.gitplex.symbolextractor.java.symbols.ui;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.symbolextractor.java.symbols.MethodDef;
import com.gitplex.symbolextractor.util.HighlightableLabel;

@SuppressWarnings("serial")
public class MethodDefPanel extends Panel {

	private final MethodDef methodDef;
	
	private final Pair<Integer, Integer> highlight;
	
	public MethodDefPanel(String id, MethodDef methodDef, Pair<Integer, Integer> matchRange) {
		super(id);
		this.methodDef = methodDef;
		this.highlight = matchRange;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new HighlightableLabel("name", methodDef.getName(), highlight));
		add(new Label("params", methodDef.getParams()));
		add(new Label("type", methodDef.getType()).setVisible(methodDef.getType()!=null));
	}

}
