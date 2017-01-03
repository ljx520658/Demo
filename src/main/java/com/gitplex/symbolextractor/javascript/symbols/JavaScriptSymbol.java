package com.gitplex.symbolextractor.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;

import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.TokenPosition;

public abstract class JavaScriptSymbol extends Symbol {

	private static final long serialVersionUID = 1L;
	
	public JavaScriptSymbol(@Nullable Symbol parent, @Nullable String indexName, TokenPosition position) {
		super(parent, indexName, position);
	}
	
	public JavaScriptSymbol(@Nullable Symbol parent, @Nullable SyntaxToken token) {
		super(parent, token!=null?removeQuotes(token.text()):null, getPosition(token));
	}
	
	public static TokenPosition getPosition(@Nullable SyntaxToken token) {
		return token!=null?new TokenPosition(token.line()-1, token.column(), token.endLine()-1, token.endColumn()):null;
	}
	
	public static String removeQuotes(String name) {
		return StringUtils.stripEnd(StringUtils.stripStart(name, "'\""), "'\"");
	}
	
	@Override
	public String getScope() {
		String scope;
		if (getParent() != null) {
			String parentScope = getParent().getScope();
			String parentName = getParent().getIndexName();
			if (parentName == null)
				parentName = "{}";
			if (parentScope != null)
				scope = parentScope + ">" + parentName;
			else
				scope = parentName;
		} else {
			scope = null;
		}
		return scope;
	}

}
