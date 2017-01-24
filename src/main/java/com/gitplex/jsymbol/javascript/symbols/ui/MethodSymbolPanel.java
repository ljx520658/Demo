package com.gitplex.jsymbol.javascript.symbols.ui;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.javascript.symbols.MethodAccess;
import com.gitplex.jsymbol.javascript.symbols.MethodSymbol;
import com.gitplex.jsymbol.util.HighlightableLabel;

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
		
		if (symbol.getMethodAccess() == MethodAccess.GET)
			add(new Label("accessor", "get"));
		else if (symbol.getMethodAccess() == MethodAccess.SET)
			add(new Label("accessor", "set"));
		else
			add(new WebMarkupContainer("accessor").setVisible(false));
		
		/*
		 * highlight only applies to indexed/searchable name
		 */
		add(new HighlightableLabel("name", symbol.getName(), highlight));
		
		add(new Label("params", symbol.getParameters()));
	}

}
