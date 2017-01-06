package com.gitplex.symbolextractor;

import java.util.List;

public interface SymbolExtractor<T extends Symbol> {
	
	List<T> extract(String source) throws ExtractException;
	
	boolean accept(String fileName);
	
	int getVersion();
	
}