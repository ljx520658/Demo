package com.gitplex.jsymbol.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.Symbol;
import com.gitplex.jsymbol.TokenPosition;
import com.gitplex.jsymbol.javascript.symbols.ui.icon.IconLocator;
import com.gitplex.jsymbol.util.HighlightableLabel;
import com.gitplex.jsymbol.util.NoAntiCacheImage;

public class VariableSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;
	
	public VariableSymbol(@Nullable Symbol parent, String name, TokenPosition position, boolean local, boolean exported) {
		super(parent, name, position, local, exported);
	}

	@Override
	public Component render(String componentId, Range highlight) {
		return new HighlightableLabel(componentId, getName(), highlight);
	}

	@Override
	public Image renderIcon(String componentId) {
		Image icon;
		if (isExported()) {
            icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "exported_variable.png"));
            icon.add(AttributeAppender.append("title", "exported variable"));
        } else if (isLocal()) {
            icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "local_variable.png"));
            icon.add(AttributeAppender.append("title", "local variable"));
		} else {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "variable.png"));
			icon.add(AttributeAppender.append("title", "variable"));
		}
		return icon;
	}

}
