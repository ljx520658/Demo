package com.gitplex.symbolextractor.javascript.symbols.ui;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.javascript.symbols.PropertySymbol;
import com.gitplex.symbolextractor.util.HighlightableLabel;

@SuppressWarnings("serial")
public class PropertySymbolPanel extends JavaScriptSymbolPanel {

	private final PropertySymbol symbol;
	
	private final Range highlight;
	
	public PropertySymbolPanel(String id, PropertySymbol symbol, Range highlight) {
		super(id);
		this.symbol = symbol;
		this.highlight = highlight;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new HighlightableLabel("name", symbol.getIndexName(), highlight));
	}

}
