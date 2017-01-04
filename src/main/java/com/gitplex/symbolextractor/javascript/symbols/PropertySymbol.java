package com.gitplex.symbolextractor.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.javascript.symbols.ui.icon.IconLocator;
import com.gitplex.symbolextractor.util.HighlightableLabel;
import com.gitplex.symbolextractor.util.NoAntiCacheImage;

public class PropertySymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;
	
	public PropertySymbol(@Nullable Symbol parent, SyntaxToken token) {
		super(parent, token);
	}

	@Override
	public boolean isPrimary() {
		return false;
	}

	@Override
	public Component render(String componentId, Range highlight) {
		return new HighlightableLabel(componentId, getIndexName(), highlight);
	}

	@Override
	public Image renderIcon(String componentId) {
		Image icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "property.png"));
		icon.add(AttributeAppender.append("title", "property"));
		return icon;
	}

}
