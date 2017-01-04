package com.gitplex.symbolextractor.java.symbols.ui;

import org.apache.wicket.markup.html.basic.Label;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.java.symbols.MethodDef;
import com.gitplex.symbolextractor.util.HighlightableLabel;

@SuppressWarnings("serial")
public class MethodDefPanel extends JavaSymbolPanel {

	private final MethodDef methodDef;
	
	private final Range highlight;
	
	public MethodDefPanel(String id, MethodDef methodDef, Range highlight) {
		super(id);
		this.methodDef = methodDef;
		this.highlight = highlight;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new HighlightableLabel("name", methodDef.getIndexName(), highlight));
		add(new Label("params", methodDef.getParams()));
		add(new Label("type", methodDef.getType()).setVisible(methodDef.getType()!=null));
	}

}
