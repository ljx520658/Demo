package com.gitplex.symbolextractor.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.TokenPosition;
import com.gitplex.symbolextractor.javascript.symbols.ui.AssignedSymbolPanel;
import com.gitplex.symbolextractor.javascript.symbols.ui.icon.IconLocator;
import com.gitplex.symbolextractor.util.NoAntiCacheImage;

public class AssignedSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;

	private final String displayName;
	
	private final String object;
	
	public AssignedSymbol(@Nullable Symbol parent, @Nullable String indexName, 
			TokenPosition position, String displayName, @Nullable String object) {
		super(parent, indexName, position);
		this.displayName = displayName;
		this.object = object;
	}

	public AssignedSymbol(@Nullable Symbol parent, SyntaxToken token, @Nullable String object) {
		super(parent, token);
		this.displayName = removeQuotes(token.text());
		this.object = object;
	}
	
	@Nullable
	public String getObject() {
		return object;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public boolean isPrimary() {
		return false;
	}
	
	@Override
	public Component render(String componentId, Range highlight) {
		return new AssignedSymbolPanel(componentId, this, highlight);
	}

	public String getRootObject() {
		if (object != null) {
			return StringUtils.substringBefore(StringUtils.substringBefore(object, "."), "[");
		} else {
			return super.getIndexName();
		}
	}

	@Override
	public Image renderIcon(String componentId) {
		Image icon;
		if (object != null) {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "property.png"));
			icon.add(AttributeAppender.append("title", "property"));
		} else {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "object.png"));
			icon.add(AttributeAppender.append("title", "object"));
		}
		return icon;
	}

}
