package com.gitplex.symbolextractor.javascript.symbols.ui;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.javascript.symbols.FunctionSymbol;
import com.gitplex.symbolextractor.javascript.symbols.DeclarationType;
import com.gitplex.symbolextractor.util.HighlightableLabel;

@SuppressWarnings("serial")
public class FunctionSymbolPanel extends JavaScriptSymbolPanel {

	private final FunctionSymbol symbol;
	
	private final Range highlight;
	
	public FunctionSymbolPanel(String id, FunctionSymbol symbol, Range highlight) {
		super(id);
		this.symbol = symbol;
		this.highlight = highlight;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (symbol.getDeclarationType() == DeclarationType.EXPORT) {
			add(new WebMarkupContainer("icon")
					.add(AttributeAppender.append("class", "export"))
					.add(AttributeAppender.replace("title", "exported function")));
		} else if (symbol.getDeclarationType() == DeclarationType.IMPORT) {
			add(new WebMarkupContainer("icon")
					.add(AttributeAppender.append("class", "import"))
					.add(AttributeAppender.replace("title", "imported function")));
		} else {
			add(new WebMarkupContainer("icon"));
		}
			
		if (symbol.getIndexName() != null)
			add(new HighlightableLabel("name", symbol.getIndexName(), highlight));
		else
			add(new WebMarkupContainer("name").setVisible(false));
		
		add(new Label("params", symbol.getParams()));
	}

}
