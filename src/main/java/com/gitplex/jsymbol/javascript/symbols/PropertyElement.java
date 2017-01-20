package com.gitplex.jsymbol.javascript.symbols;

import java.io.Serializable;

import com.gitplex.jsymbol.TokenPosition;

public class PropertyElement implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final TokenPosition position;
	
	public PropertyElement(String name, TokenPosition position) {
		this.name = name;
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public TokenPosition getPosition() {
		return position;
	}
	
}