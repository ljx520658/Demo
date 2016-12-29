package com.gitplex.symbolextractor.javascript.symbols.ui;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.javascript.symbols.DeclarationType;
import com.gitplex.symbolextractor.javascript.symbols.VariableSymbol;
import com.gitplex.symbolextractor.util.HighlightableLabel;

@SuppressWarnings("serial")
public class VariableSymbolPanel extends JavaScriptSymbolPanel {

	private final VariableSymbol symbol;
	
	private final Range highlight;
	
	public VariableSymbolPanel(String id, VariableSymbol symbol, Range highlight) {
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
					.add(AttributeAppender.replace("title", "exported variable")));
		} else if (symbol.getDeclarationType() == DeclarationType.IMPORT) {
			add(new WebMarkupContainer("icon")
					.add(AttributeAppender.append("class", "import"))
					.add(AttributeAppender.replace("title", "imported variable")));
		} else {
			add(new WebMarkupContainer("icon"));
		}
		
		add(new HighlightableLabel("name", symbol.getIndexName(), highlight));
	}

}
