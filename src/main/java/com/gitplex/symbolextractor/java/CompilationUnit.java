package com.gitplex.symbolextractor.java;

import javax.annotation.Nullable;

import com.gitplex.symbolextractor.Position;
import com.gitplex.symbolextractor.Symbol;

public class CompilationUnit extends Symbol {
	
	private static final long serialVersionUID = 1L;
	
	private String packageName;

	public CompilationUnit(@Nullable String packageName, Position from, Position to) {
		super(null, null, from, to);
		
		this.packageName = packageName;
	}
	
	@Nullable
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public String getScope() {
		return packageName;
	}

	@Override
	public boolean isPrimary() {
		return false;
	}

}
