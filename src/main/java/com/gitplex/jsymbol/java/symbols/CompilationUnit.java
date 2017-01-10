package com.gitplex.jsymbol.java.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.Symbol;
import com.gitplex.jsymbol.TokenPosition;
import com.gitplex.jsymbol.java.symbols.ui.icon.IconLocator;
import com.gitplex.jsymbol.util.NoAntiCacheImage;

public class CompilationUnit extends Symbol {
	
	private static final long serialVersionUID = 1L;
	
	private String packageName;

	public CompilationUnit(@Nullable String packageName, @Nullable TokenPosition position) {
		super(null, null, position, true);
		
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
		return new Label(componentId, packageName);
	}

	@Override
	public Image renderIcon(String componentId) {
		Image icon = new NoAntiCacheImage("icon", new PackageResourceReference(IconLocator.class, "package_obj.png"));
		icon.add(AttributeAppender.append("title", "package"));
		return icon;
	}

}
