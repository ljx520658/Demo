package com.gitplex.symbolextractor.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.javascript.symbols.ui.MethodSymbolPanel;
import com.gitplex.symbolextractor.javascript.symbols.ui.icon.IconLocator;
import com.gitplex.symbolextractor.util.NoAntiCacheImage;

public class MethodSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;
	
	private final MethodAccessorType accessorType;
	
	private final String params;
	
	public MethodSymbol(@Nullable Symbol parent, SyntaxToken token, 
			MethodAccessorType accessorType, String params) {
		super(parent, token, false);
		this.accessorType = accessorType;
		this.params = params;
	}

	public MethodAccessorType getAccessorType() {
		return accessorType;
	}

	public String getParams() {
		return params;
	}

	@Override
	public Component render(String componentId, Range highlight) {
		return new MethodSymbolPanel(componentId, this, highlight);
	}

	@Override
	public Image renderIcon(String componentId) {
		Image icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "method.png"));
		icon.add(AttributeAppender.append("title", "method"));
		return icon;
	}

}
