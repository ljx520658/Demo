package com.gitplex.jsymbol.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.Symbol;
import com.gitplex.jsymbol.TokenPosition;
import com.gitplex.jsymbol.javascript.symbols.ui.MethodSymbolPanel;
import com.gitplex.jsymbol.javascript.symbols.ui.icon.IconLocator;
import com.gitplex.jsymbol.util.NoAntiCacheImage;

public class MethodSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;
	
	private final MethodAccessorType accessorType;
	
	private final String params;
	
	public MethodSymbol(@Nullable Symbol parent, String name, TokenPosition position,
			MethodAccessorType accessorType, String params) {
		super(parent, name, position, false, false);
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
