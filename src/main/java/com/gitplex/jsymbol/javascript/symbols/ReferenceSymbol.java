package com.gitplex.jsymbol.javascript.symbols;

import java.util.ArrayList;
import java.util.List;

public class ReferenceSymbol extends ObjectSymbol {

	private static final long serialVersionUID = 1L;

	private JavaScriptSymbol referencedParent;
	
	private List<String> referencedPath = new ArrayList<>();
	
	public JavaScriptSymbol getReferencedParent() {
		return referencedParent;
	}

	public void setReferencedParent(JavaScriptSymbol referencedParent) {
		this.referencedParent = referencedParent;
	}

	public List<String> getReferencedPath() {
		return referencedPath;
	}

	public void setReferencedPath(List<String> referencedPath) {
		this.referencedPath = referencedPath;
	}

}
