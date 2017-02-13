package com.gitplex.jsymbol.util;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

/**
 * A skippable token stream is able to skip tokens in ANTLR streams to speed up ANTLR parsing
 * 
 * @author robin
 *
 */
public class SkippableTokenStream extends CommonTokenStream {

	public SkippableTokenStream(TokenSource tokenSource) {
		super(tokenSource);
	}

	public SkippableTokenStream(TokenSource tokenSource, int channel) {
		super(tokenSource, channel);
	}
	
	/**
	 * Skip tokens until encounter specified token type
	 * 
	 * @param expectedType
	 * 			expected token type
	 */
	public void skipUntil(int expectedType) {
		while(true) {
			int type = LA(1);
			if (type == expectedType || type == Token.EOF)
				break;
			consume();
		}
	}
	
	/**
	 * Skip tokens until encounter one of specified token types
	 * 
	 * @param expectedTypes
	 * 			expected token types
	 */
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
	
	/**
	 * Skip tokens until encounter balanced closing token. For instance, to speed up ANTLR declaration parsing from C 
	 * language, we modify ANTLR C grammar as below:
	 * <pre><code>
	 * functionDefinition
     *  		: declarationSpecifiers? declarator declarationList? '{' {getSkippableInput().skipUntilClosing(LeftBrace, RightBrace);} '}';
	 *</code></pre>
	 * 
	 * Here _getSkippableInput()_ is defined as a parser member returning instance instance of this class. Since we do 
	 * not need to parse method body to get declarations, we can skip method body parsing by calling this method.   
	 * 
	 * @param openType
	 * 			open token type
	 * @param closeType
	 * 			close token type
	 */
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
