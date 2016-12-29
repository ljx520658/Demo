package com.gitplex.symbolextractor.javascript.symbols.ui;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.javascript.symbols.AssignedSymbol;
import com.gitplex.symbolextractor.util.HighlightableLabel;

@SuppressWarnings("serial")
public class ReferenceSymbolPanel extends JavaScriptSymbolPanel {

	private final AssignedSymbol symbol;
	
	private final Range highlight;
	
	public ReferenceSymbolPanel(String id, AssignedSymbol symbol, Range highlight) {
		super(id);
		this.symbol = symbol;
		this.highlight = highlight;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String object = symbol.getObject();
		if (object != null) {
			add(new Label("icon", "P").add(AttributeAppender.append("title", "property")));
			add(new Label("object", object+"."));
		} else {
			add(new Label("icon", "O").add(AttributeAppender.append("title", "object")));
			add(new WebMarkupContainer("object").setVisible(false));
		}
		
		add(new HighlightableLabel("property", symbol.getDisplayName(), highlight));
	}

}
