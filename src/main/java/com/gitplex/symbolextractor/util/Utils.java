package com.gitplex.symbolextractor.util;

import org.apache.commons.lang3.StringEscapeUtils;

public class Utils {
	
	public static int[] getOrdinals(Enum<?> array[]) {
		int[] intArray = new int[array.length];
		for (int i=0; i<array.length; i++)
			intArray[i] = array[i].ordinal();
		return intArray;
	}
	
	public static String escapeHtml(String text) {
		String escapedText = "";
		for (int i=0; i<text.length(); i++) {
			char ch = text.charAt(i);
			if (ch == ' ' || ch == '\t' || !Character.isWhitespace(ch))
				escapedText += ch;
		}
		return StringEscapeUtils.escapeHtml4(escapedText);
	}
	
}
