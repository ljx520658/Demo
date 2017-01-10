package com.gitplex.jsymbol.javascript.symbols;

import javax.annotation.Nullable;

import com.gitplex.jsymbol.TokenPosition;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.Symbol;
import com.gitplex.jsymbol.javascript.symbols.ui.icon.IconLocator;
import com.gitplex.jsymbol.util.HighlightableLabel;
import com.gitplex.jsymbol.util.NoAntiCacheImage;

public class ClassSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;
	
	public ClassSymbol(@Nullable Symbol parent, @Nullable String name, TokenPosition position, boolean local,
                       boolean exported) {
		super(parent, name, position, local, exported);
	}

	@Override
	public boolean isPrimary() {
		return true;
	}

	@Override
	public Component render(String componentId, Range highlight) {
		if (getName() != null)
			return new HighlightableLabel(componentId, getName(), highlight);
		else
			return new WebMarkupContainer(componentId).setVisible(false);
	}

	@Override
	public Image renderIcon(String componentId) {
		Image icon;
		if (isExported()) {
            icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "exported_class.png"));
            icon.add(AttributeAppender.append("title", "exported class"));
        } else if (isLocal()) {
            icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "local_class.png"));
            icon.add(AttributeAppender.append("title", "local class"));
		} else {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "class.png"));
			icon.add(AttributeAppender.append("title", "class"));
		}
		return icon;
	}

}
