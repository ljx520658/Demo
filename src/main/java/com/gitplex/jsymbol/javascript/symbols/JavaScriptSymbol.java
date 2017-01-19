package com.gitplex.jsymbol.javascript.symbols;

import javax.annotation.Nullable;

import com.gitplex.jsymbol.Symbol;
import com.gitplex.jsymbol.TokenPosition;

public abstract class JavaScriptSymbol extends Symbol {

	private static final long serialVersionUID = 1L;
	
	private boolean exported;
	
	public JavaScriptSymbol(@Nullable Symbol parent, @Nullable String name, TokenPosition position, 
			boolean local, boolean exported) {
		super(parent, name, position, position, local);
		this.exported = exported;
	}
	
	public boolean isExported() {
		return exported;
	}

    public void setExported(boolean exported) {
        this.exported = exported;
    }

    @Override
	public boolean isPrimary() {
		return isExported();
	}

}
