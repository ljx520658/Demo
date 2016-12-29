package com.gitplex.symbolextractor.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.javascript.symbols.ui.ClassSymbolPanel;

public class ClassSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;
	
	private final DeclarationType declarationType;
	
	public ClassSymbol(@Nullable Symbol parent, @Nullable SyntaxToken token, DeclarationType declarationType) {
		super(parent, token);
		this.declarationType = declarationType;
	}

	public DeclarationType getDeclarationType() {
		return declarationType;
	}

	@Override
	public boolean isPrimary() {
		return true;
	}

	@Override
	public Component render(String componentId, Range highlight) {
		return new ClassSymbolPanel(componentId, this, highlight);
	}

}
