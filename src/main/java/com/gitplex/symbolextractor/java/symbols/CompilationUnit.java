package com.gitplex.symbolextractor.java.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.TokenPosition;
import com.gitplex.symbolextractor.java.symbols.ui.CompilationUnitPanel;

public class CompilationUnit extends Symbol {
	
	private static final long serialVersionUID = 1L;
	
	private String packageName;

	public CompilationUnit(@Nullable String packageName, @Nullable TokenPosition position) {
		super(null, null, position);
		
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

	@Override
	public Component render(String componentId, Range highlight) {
		return new CompilationUnitPanel(componentId, this);
	}

}
