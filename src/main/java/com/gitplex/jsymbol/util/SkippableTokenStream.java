package com.gitplex.jsymbol.util;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

public class SkippableTokenStream extends CommonTokenStream {

	public SkippableTokenStream(TokenSource tokenSource) {
		super(tokenSource);
	}

	public SkippableTokenStream(TokenSource tokenSource, int channel) {
		super(tokenSource, channel);
	}
	
	public void skipUntil(int expectedType) {
		while(true) {
			int type = LA(1);
			if (type == expectedType || type == Token.EOF)
				break;
			consume();
		}
	}
	
	public void skipUntil(int...expectedTypes) {
		while(true) {
			int tokenType = LA(1);
			
			if (tokenType == Token.EOF)
				break;
			
			boolean found = false;
			for (int type: expectedTypes) {
				if (type == tokenType) {
					found = true;
					break;
				}
			}
			if (found)
				break;
			consume();
		}
	}
	
	public void skipUntilClosing(int openType, int closeType) {
		int nestingLevel = 1;
		while (true) {
			skipUntil(openType, closeType);
			int tokenType = LA(1);
			if (tokenType == Token.EOF) {
				break;
			} else if (tokenType == closeType) {
				if (--nestingLevel == 0)
					break;
				else
					consume();
			} else if (tokenType == openType) {
				nestingLevel++;
				consume();
			}
		}
	}
	
}
