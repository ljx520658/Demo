package com.gitplex.symbolextractor;

import java.io.Serializable;

public class Position implements Serializable {

	private static final long serialVersionUID = 1L;

	public static Position NONE = new Position(-1, -1);
	
	private final int line;
	
	private final int ch;
	
	public Position(int line, int ch) {
		this.line = line;
		this.ch = ch;
	}

	public int getLine() {
		return line;
	}

	public int getCh() {
		return ch;
	}
	
}
