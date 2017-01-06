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
import com.gitplex.symbolextractor.javascript.symbols.ui.ReferenceSymbolPanel;
import com.gitplex.symbolextractor.javascript.symbols.ui.icon.IconLocator;
import com.gitplex.symbolextractor.util.NoAntiCacheImage;

/**
 * Reference symbol is of below form:
 * <ul>
 * <li>someobj.someprop = somevalue;
 * <li>someobj = somevalue;
 * </ul
 * 
 * @author robin
 *
 */
public class ReferenceSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;

	private final String displayName;

	/*
	 * This field represents left part of the form <object...>.<property>, and 
	 * is null if left part does not exist
	 */
	private final String object;
	
	public ReferenceSymbol(@Nullable Symbol parent, @Nullable String indexName, 
			TokenPosition position, String displayName, @Nullable String object, boolean exported) {
		super(parent, indexName, position, exported);
		this.displayName = displayName;
		this.object = object;
	}

	public ReferenceSymbol(@Nullable Symbol parent, SyntaxToken token, @Nullable String object, boolean exported) {
		super(parent, token, exported);
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
	public Component render(String componentId, Range highlight) {
		return new ReferenceSymbolPanel(componentId, this, highlight);
	}

	/**
	 * Get the root object, for instance, the root object of reference "obj1.obj2.prop"
	 * is "obj1". The root object will be used to compare with declared local variables 
	 * to determine if it is really a local reference, in which case we should discard
	 * 
	 * @return
	 */
	public String getRootObject() {
		if (object != null) {
			// reference might be of the form "obj[0].prop", here we only returns "obj"
			return StringUtils.substringBefore(StringUtils.substringBefore(object, "."), "[");
		} else {
			return super.getIndexName();
		}
	}

	@Override
	public Image renderIcon(String componentId) {
		String iconFile, tooltip;
		if (isExported()) {
			if (object != null) {
				iconFile = "exported_property.png";
				tooltip = "exported property";
			} else {
				iconFile = "exported_object.png";
				tooltip = "exported object";
			}
		} else {
			if (object != null) {
				iconFile = "property.png";
				tooltip = "property";
			} else {
				iconFile = "object.png";
				tooltip = "object";
			}
		}
		Image icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, iconFile));
		icon.add(AttributeAppender.append("title", tooltip));
		return icon;
	}

}
