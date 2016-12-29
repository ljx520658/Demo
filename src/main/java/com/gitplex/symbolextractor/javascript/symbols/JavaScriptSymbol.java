package com.gitplex.symbolextractor.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;

import com.gitplex.symbolextractor.Position;
import com.gitplex.symbolextractor.Symbol;

public abstract class JavaScriptSymbol extends Symbol {

	private static final long serialVersionUID = 1L;
	
	public JavaScriptSymbol(@Nullable Symbol parent, @Nullable String indexName, Position from, Position to) {
		super(parent, indexName, from, to);
	}
	
	public JavaScriptSymbol(@Nullable Symbol parent, @Nullable SyntaxToken token) {
		super(parent, token!=null?removeQuotes(token.text()):null, getFrom(token), getTo(token));
	}
	
	public static Position getFrom(@Nullable SyntaxToken token) {
		return token!=null?new Position(token.line()-1, token.column()):Position.NONE;
	}
	
	public static Position getTo(@Nullable SyntaxToken token) {
		return token!=null?new Position(token.endLine()-1, token.endColumn()):Position.NONE;
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
