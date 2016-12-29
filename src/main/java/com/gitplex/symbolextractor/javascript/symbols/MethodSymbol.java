package com.gitplex.symbolextractor.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.javascript.symbols.ui.MethodSymbolPanel;

public class MethodSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;
	
	private final MethodAccessorType accessorType;
	
	private final String params;
	
	public MethodSymbol(@Nullable Symbol parent, SyntaxToken token, 
			MethodAccessorType accessorType, String params) {
		super(parent, token);
		this.accessorType = accessorType;
		this.params = params;
	}

	public MethodAccessorType getAccessorType() {
		return accessorType;
	}

	public String getParams() {
		return params;
	}

	@Override
	public boolean isPrimary() {
		return false;
	}

	@Override
	public Component render(String componentId, Range highlight) {
		return new MethodSymbolPanel(componentId, this, highlight);
	}

}
