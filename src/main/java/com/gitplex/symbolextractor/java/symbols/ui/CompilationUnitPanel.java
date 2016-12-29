package com.gitplex.symbolextractor.java.symbols.ui;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.gitplex.symbolextractor.java.symbols.CompilationUnit;
import com.gitplex.symbolextractor.java.symbols.ui.icon.IconLocator;

@SuppressWarnings("serial")
public class CompilationUnitPanel extends JavaSymbolPanel {

	private final CompilationUnit compilationUnit;
	
	public CompilationUnitPanel(String id, CompilationUnit compilationUnit) {
		super(id);
		this.compilationUnit = compilationUnit;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Image("icon", new PackageResourceReference(IconLocator.class, "package_obj.png")));
		add(new Label("name", compilationUnit.getPackageName()));
	}

}
