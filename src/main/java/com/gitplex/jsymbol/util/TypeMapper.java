package com.gitplex.jsymbol.util;

public interface TypeMapper {
	
	Enum<?> getType(com.gitplex.jsyntax.Token jsyntaxToken);
	
}
