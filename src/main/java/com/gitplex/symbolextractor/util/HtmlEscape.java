package com.gitplex.symbolextractor.util;

import org.apache.commons.lang3.StringEscapeUtils;

public class HtmlEscape {

	public static String escape(String text) {
		String escapedText = "";
		for (int i=0; i<text.length(); i++) {
			char ch = text.charAt(i);
			if (ch == ' ' || ch == '\t' || !Character.isWhitespace(ch))
				escapedText += ch;
		}
		return StringEscapeUtils.escapeHtml4(escapedText);
	}

}
