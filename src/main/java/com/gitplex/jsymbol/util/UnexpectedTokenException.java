package com.gitplex.jsymbol.util;

import com.gitplex.jsymbol.ExtractException;

@SuppressWarnings("serial")
public class UnexpectedTokenException extends ExtractException {

	public UnexpectedTokenException(Token token) {
		super("Unexpected token: " + token);
	}

}
