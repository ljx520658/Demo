package com.gitplex.jsymbol.util;

import java.util.ArrayList;
import java.util.List;

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
	
	public Token skip() {
		Token token = LT(1);
		if (token.getType() != Token.EOF)
			consume();
		return token;
	}
	
	public Token skipType(int type) {
		Token token = skip();
		while(true) {
			if (token.getType() == type || token.getType() == Token.EOF)
				return token;
			token = skip();
		}
	}

	private boolean isType(Token token, int...types) {
		for (int type: types) {
			if (type == token.getType())
				return true;
		}
		return false;
	}
	
	public Token skipType(int...types) {
		Token token = skip();
		while(true) {
			if (isType(token, types) || token.getType() == Token.EOF)
				return token;
			token = skip();
		}
	}
	
	public Token skipClosed(int openType, int closeType) {
		int nestingLevel = 1;
		Token balanced = skipType(openType, closeType);
		while (true) {
			if (balanced.getType() == Token.EOF) {
				return balanced;
			} else if (isType(balanced, closeType)) {
				if (--nestingLevel == 0)
					return balanced;
			} else if (isType(balanced, openType)) {
				nestingLevel++;
			}
			balanced = skipType(openType, closeType);
		}
	}
	
	public List<Token> between(int startIndex, int endIndex) {
		List<Token> tokens = new ArrayList<>();
		for (int i=startIndex; i<=endIndex; i++) 
			tokens.add(get(i));
		return tokens;
	}
	
}
