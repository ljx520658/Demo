package com.gitplex.symbolextractor.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.javascript.symbols.ui.FunctionSymbolPanel;
import com.gitplex.symbolextractor.javascript.symbols.ui.icon.IconLocator;
import com.gitplex.symbolextractor.util.NoAntiCacheImage;

public class FunctionSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;
	
	private final String params;
	
	public FunctionSymbol(@Nullable Symbol parent, @Nullable SyntaxToken token, String params, boolean exported) {
		super(parent, token, exported);
		this.params = params;
	}

	public String getParams() {
		return params;
	}

	@Override
	public Component render(String componentId, Range highlight) {
		return new FunctionSymbolPanel(componentId, this, highlight);
	}

	@Override
	public Image renderIcon(String componentId) {
		Image icon;
		if (isExported()) { 
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "exported_function.png"));
			icon.add(AttributeAppender.append("title", "exported function"));
		} else {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "function.png"));
			icon.add(AttributeAppender.append("title", "function"));
		}
		return icon;
	}
	
}
