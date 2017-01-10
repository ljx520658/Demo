package com.gitplex.jsymbol.javascript.symbols;

import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.Symbol;
import com.gitplex.jsymbol.TokenPosition;
import com.gitplex.jsymbol.javascript.symbols.ui.ReferenceSymbolPanel;
import com.gitplex.jsymbol.javascript.symbols.ui.icon.IconLocator;
import com.gitplex.jsymbol.util.NoAntiCacheImage;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import javax.annotation.Nullable;

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

	/*
	 * This field represents left part of the form <object...>.<property>, and 
	 * is null if left part does not exist
	 */
	private final String object;
	
	public ReferenceSymbol(@Nullable Symbol parent, String name,
			TokenPosition position, boolean export, @Nullable String object) {
		super(parent, name, position, false, export);
		this.object = object;
	}

	@Nullable
	public String getObject() {
		return object;
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
     *          get the root object
	 */
	public String getRootObject() {
		if (object != null) {
			// reference might be of the form "obj[0].prop", here we only returns "obj"
			return StringUtils.substringBefore(StringUtils.substringBefore(object, "."), "[");
		} else {
			return super.getName();
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
