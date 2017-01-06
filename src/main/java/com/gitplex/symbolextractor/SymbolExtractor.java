package com.gitplex.symbolextractor;

import java.util.List;

/**
 * Extracts symbols from source code to serve below purposes:
 * <ul>
 * <li> Index symbol definition names for definition search
 * <li> Display outlines of source files
 * 
 * So local structures such as local variables inside a method should not be extracted, 
 * otherwise it will clutter search result of symbol definitions with same name, and 
 * also clutter the outline   
 * 
 * @author robin
 *
 * @param <T>
 */
public interface SymbolExtractor<T extends Symbol> {
	
	List<T> extract(String source) throws ExtractException;
	
	boolean accept(String fileName);
	
	int getVersion();
	
}