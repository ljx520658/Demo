package com.gitplex.jsymbol.java.symbols;

import com.gitplex.jsymbol.Symbol;
import com.gitplex.jsymbol.TokenPosition;

public abstract class JavaSymbol extends Symbol {

	private static final long serialVersionUID = 1L;
	
	public JavaSymbol(Symbol parent, String name, TokenPosition position, TokenPosition scope, boolean local) {
		super(parent, name, position, scope, local);
	}

}
