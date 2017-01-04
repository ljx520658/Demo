package com.gitplex.symbolextractor.javascript.symbols.ui;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.javascript.symbols.MethodAccessorType;
import com.gitplex.symbolextractor.javascript.symbols.MethodSymbol;
import com.gitplex.symbolextractor.util.HighlightableLabel;

@SuppressWarnings("serial")
public class MethodSymbolPanel extends Panel {

	private final MethodSymbol symbol;
	
	private final Range highlight;
	
	public MethodSymbolPanel(String id, MethodSymbol symbol, Range highlight) {
		super(id);
		this.symbol = symbol;
		this.highlight = highlight;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (symbol.getAccessorType() == MethodAccessorType.GET)
			add(new Label("accessor", "get"));
		else if (symbol.getAccessorType() == MethodAccessorType.SET)
			add(new Label("accessor", "set"));
		else
			add(new WebMarkupContainer("accessor").setVisible(false));
		
		add(new HighlightableLabel("name", symbol.getIndexName(), highlight));
		add(new Label("params", symbol.getParams()));
	}

}
