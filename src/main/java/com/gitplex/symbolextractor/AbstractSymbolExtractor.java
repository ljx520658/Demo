package com.gitplex.symbolextractor;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractSymbolExtractor implements SymbolExtractor {

	protected boolean acceptExtensions(String fileName, String...exts) {
		String fileExt = StringUtils.substringAfterLast(fileName, ".");
		for (String ext: exts) {
			if (ext.equalsIgnoreCase(fileExt))
				return true;
		}
		return false;
	}

	protected boolean acceptPattern(String fileName, Pattern pattern) {
		return pattern.matcher(fileName).matches();
	}
	
}
