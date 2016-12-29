package com.gitplex.symbolextractor.javascript.symbols.ui;

import org.apache.wicket.request.resource.CssResourceReference;

public class JavaScriptSymbolResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public JavaScriptSymbolResourceReference() {
		super(JavaScriptSymbolResourceReference.class, "javascript-symbol.css");
	}

}
