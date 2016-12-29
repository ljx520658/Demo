package com.gitplex.symbolextractor;

import java.util.List;

public interface SymbolExtractor {
	
	List<Symbol> extract(String source) throws ExtractException;
	
}