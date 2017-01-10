package com.gitplex.jsymbol.javascript.symbols.ui;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.javascript.symbols.ReferenceSymbol;
import com.gitplex.jsymbol.util.HighlightableLabel;

@SuppressWarnings("serial")
public class ReferenceSymbolPanel extends Panel {

	private final ReferenceSymbol symbol;
	
	private final Range highlight;
	
	public ReferenceSymbolPanel(String id, ReferenceSymbol symbol, Range highlight) {
		super(id);
		this.symbol = symbol;
		this.highlight = highlight;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String object = symbol.getObject();
		if (object != null) {
			add(new Label("object", object+"."));
		} else {
			add(new WebMarkupContainer("object").setVisible(false));
		}
		
		/*
		 * highlight only applies to indexed/searchable name
		 */
		add(new HighlightableLabel("property", symbol.getName(), highlight));
	}

}