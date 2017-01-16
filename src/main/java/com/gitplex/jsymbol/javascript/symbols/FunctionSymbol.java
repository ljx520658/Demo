package com.gitplex.jsymbol.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.Symbol;
import com.gitplex.jsymbol.TokenPosition;
import com.gitplex.jsymbol.javascript.symbols.ui.FunctionSymbolPanel;
import com.gitplex.jsymbol.javascript.symbols.ui.icon.IconLocator;
import com.gitplex.jsymbol.util.NoAntiCacheImage;

public class FunctionSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;
	
	private final String params;
	
	public FunctionSymbol(@Nullable Symbol parent, @Nullable String name, TokenPosition position, boolean local,
                          boolean exported, String params) {
		super(parent, name, position, local, exported);
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
        } else if (isLocal()) {
            icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "local_function.png"));
            icon.add(AttributeAppender.append("title", "local function"));
		} else {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "function.png"));
			icon.add(AttributeAppender.append("title", "function"));
		}
		return icon;
	}
	
}
