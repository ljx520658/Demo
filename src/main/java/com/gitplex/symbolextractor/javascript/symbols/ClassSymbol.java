package com.gitplex.symbolextractor.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.javascript.symbols.ui.icon.IconLocator;
import com.gitplex.symbolextractor.util.HighlightableLabel;
import com.gitplex.symbolextractor.util.NoAntiCacheImage;

public class ClassSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;
	
	private final DeclarationType declarationType;
	
	public ClassSymbol(@Nullable Symbol parent, @Nullable SyntaxToken token, DeclarationType declarationType) {
		super(parent, token);
		this.declarationType = declarationType;
	}

	public DeclarationType getDeclarationType() {
		return declarationType;
	}

	@Override
	public boolean isPrimary() {
		return true;
	}

	@Override
	public Component render(String componentId, Range highlight) {
		if (getIndexName() != null)
			return new HighlightableLabel(componentId, getIndexName(), highlight);
		else
			return new WebMarkupContainer(componentId).setVisible(false);
	}

	@Override
	public Image renderIcon(String componentId) {
		Image icon;
		if (declarationType == DeclarationType.EXPORT) { 
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "exported_class.png"));
			icon.add(AttributeAppender.append("title", "exported class"));
		} else if (declarationType == DeclarationType.IMPORT) {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "imported_class.png"));
			icon.add(AttributeAppender.append("title", "imported class"));
		} else {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "class.png"));
			icon.add(AttributeAppender.append("title", "class"));
		}
		return icon;
	}

}
