package com.gitplex.symbolextractor;

@SuppressWarnings("serial")
public class ExtractException extends RuntimeException {
	
	public ExtractException(String message) {
		super(message);
	}
	
	public ExtractException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
