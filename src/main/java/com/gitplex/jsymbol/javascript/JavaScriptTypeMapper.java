package com.gitplex.jsymbol.javascript;

import java.util.HashMap;
import java.util.Map;

import com.gitplex.jsyntax.Token;
import com.gitplex.jsymbol.util.TypeMapper;

public class JavaScriptTypeMapper implements TypeMapper {

	private static final Map<String, Enum<?>> KEYWORDS  = new HashMap<>();
	
	public enum Type { 
		IF, WHILE, WITH, ELSE, DO, TRY, FINALLY, RETURN, BREAK, CONTINUE, NEW, DELETE, THROW, 
		DEBUGGER, VAR, CONST, LET, FUNCTION, CATCH, FOR, SWITCH, CASE, DEFAULT, IN, TYPEOF, 
		INSTANCEOF, TRUE, FALSE, NULL, UNDEFINED, NAN, INFINITY, THIS, CLASS, SUPER, YIELD, EXPORT, 
		IMPORT, EXTENDS, AWAIT, ASYNC, IMPLEMENTS, NAMESPACE, MODULE, ENUM, TYPE, 
		PUBLIC, PRIVATE, PROTECTED, ABSTRACT, AS}
	
	static {
		KEYWORDS.put("if", Type.IF);
		KEYWORDS.put("while", Type.WHILE);
		KEYWORDS.put("with", Type.WITH);
		KEYWORDS.put("else", Type.ELSE);
		KEYWORDS.put("do", Type.DO);
		KEYWORDS.put("try", Type.TRY);
		KEYWORDS.put("finally", Type.FINALLY);
		KEYWORDS.put("return", Type.RETURN);
		KEYWORDS.put("break", Type.BREAK);
		KEYWORDS.put("continue", Type.CONTINUE);
		KEYWORDS.put("new", Type.NEW);
		KEYWORDS.put("delete", Type.DELETE);
		KEYWORDS.put("throw", Type.THROW);
		KEYWORDS.put("debugger", Type.DEBUGGER);
		KEYWORDS.put("var", Type.VAR);
		KEYWORDS.put("const", Type.CONST);
		KEYWORDS.put("let", Type.LET);
		KEYWORDS.put("function", Type.FUNCTION);
		KEYWORDS.put("catch", Type.CATCH);
		KEYWORDS.put("for", Type.FOR);
		KEYWORDS.put("switch", Type.SWITCH);
		KEYWORDS.put("case", Type.CASE);
		KEYWORDS.put("default", Type.DEFAULT);
		KEYWORDS.put("in", Type.IN);
		KEYWORDS.put("typeof", Type.TYPEOF);
		KEYWORDS.put("instanceof", Type.INSTANCEOF);
		KEYWORDS.put("true", Type.TRUE);
		KEYWORDS.put("false", Type.FALSE);
		KEYWORDS.put("null", Type.NULL);
		KEYWORDS.put("undefined", Type.UNDEFINED);
		KEYWORDS.put("NaN", Type.NAN);
		KEYWORDS.put("Infinity", Type.INFINITY);
		KEYWORDS.put("this", Type.THIS);
		KEYWORDS.put("class", Type.CLASS);
		KEYWORDS.put("super", Type.SUPER);
		KEYWORDS.put("yield", Type.YIELD);
		KEYWORDS.put("export", Type.EXPORT);
		KEYWORDS.put("import", Type.IMPORT);
		KEYWORDS.put("extends", Type.EXTENDS);
		KEYWORDS.put("await", Type.AWAIT);
		KEYWORDS.put("async", Type.ASYNC);
		
		KEYWORDS.put("implements", Type.IMPLEMENTS);
        KEYWORDS.put("namespace", Type.NAMESPACE);
        KEYWORDS.put("module", Type.MODULE);
        KEYWORDS.put("enum", Type.ENUM);
        KEYWORDS.put("type", Type.TYPE);

        KEYWORDS.put("public", Type.PUBLIC);
        KEYWORDS.put("private", Type.PRIVATE);
        KEYWORDS.put("protected", Type.PROTECTED);
        KEYWORDS.put("abstract", Type.ABSTRACT);

        KEYWORDS.put("as", Type.AS);
	}
	
	@Override
	public Enum<?> getType(Token jsyntaxToken) {
		throw new UnsupportedOperationException();
	}

}