package com.gitplex.symbolextractor.util;

public interface TypeMapper {
	
	Enum<?> getType(com.gitplex.jsyntax.Token jsyntaxToken);
	
}
