package com.gitplex.symbolextractor.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.javascript.symbols.ui.VariableSymbolPanel;

public class VariableSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;
	
	private final DeclarationType declarationType;
	
	public VariableSymbol(@Nullable Symbol parent, SyntaxToken token, DeclarationType declarationType) {
		super(parent, token);
		this.declarationType = declarationType;
	}

	@Override
	public boolean isPrimary() {
		return true;
	}

	public DeclarationType getDeclarationType() {
		return declarationType;
	}

	@Override
	public Component render(String componentId, Range highlight) {
		return new VariableSymbolPanel(componentId, this, highlight);
	}

}
