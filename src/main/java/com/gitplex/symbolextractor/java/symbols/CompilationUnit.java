package com.gitplex.symbolextractor.java.symbols;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.Component;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.gitplex.symbolextractor.Position;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.java.symbols.ui.icon.IconLocator;
import com.gitplex.symbolextractor.util.HighlightableLabel;

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

	@Override
	public Component render(String componentId, Pair<Integer, Integer> highlight) {
		return new HighlightableLabel(componentId, packageName, highlight);
	}

	@Override
	public ResourceReference getIcon() {
		return new PackageResourceReference(IconLocator.class, "package_obj.png");
	}

}
