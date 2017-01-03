package com.gitplex.symbolextractor.util;

import com.gitplex.symbolextractor.ExtractException;

@SuppressWarnings("serial")
public class UnexpectedTokenException extends ExtractException {

	public UnexpectedTokenException(Token token) {
		super("Unexpected token: " + token);
	}

}