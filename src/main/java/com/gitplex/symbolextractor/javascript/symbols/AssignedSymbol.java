package com.gitplex.symbolextractor.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;

import com.gitplex.symbolextractor.Position;
import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.javascript.symbols.ui.ReferenceSymbolPanel;

public class AssignedSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;

	private final String displayName;
	
	private final String object;
	
	public AssignedSymbol(@Nullable Symbol parent, @Nullable String indexName, 
			Position from, Position to, String displayName, @Nullable String object) {
		super(parent, indexName, from, to);
		this.displayName = displayName;
		this.object = object;
	}

	public AssignedSymbol(@Nullable Symbol parent, SyntaxToken token, @Nullable String object) {
		super(parent, token);
		this.displayName = removeQuotes(token.text());
		this.object = object;
	}
	
	public String getObject() {
		return object;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public boolean isPrimary() {
		return false;
	}
	
	@Override
	public Component render(String componentId, Range highlight) {
		return new ReferenceSymbolPanel(componentId, this, highlight);
	}

	public String getRootObject() {
		if (object != null) {
			return StringUtils.substringBefore(StringUtils.substringBefore(object, "."), "[");
		} else {
			return super.getIndexName();
		}
	}
	
}
