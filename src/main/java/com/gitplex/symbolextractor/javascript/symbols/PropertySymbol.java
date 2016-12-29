package com.gitplex.symbolextractor.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.javascript.symbols.ui.PropertySymbolPanel;

public class PropertySymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;
	
	public PropertySymbol(@Nullable Symbol parent, SyntaxToken token) {
		super(parent, token);
	}

	@Override
	public boolean isPrimary() {
		return false;
	}

	@Override
	public Component render(String componentId, Range highlight) {
		return new PropertySymbolPanel(componentId, this, highlight);
	}

}
