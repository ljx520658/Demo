package com.gitplex.symbolextractor.java.symbols;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.TokenPosition;
import com.gitplex.symbolextractor.java.symbols.ui.MethodDefPanel;
import com.gitplex.symbolextractor.java.symbols.ui.icon.IconLocator;

public class MethodDef extends Symbol {

	private static final long serialVersionUID = 1L;

	private final String type; 
	
	private final String params;

	private final List<Modifier> modifiers;
	
	public MethodDef(TypeDef parent, String methodName, TokenPosition position, 
			@Nullable String type, @Nullable String params, List<Modifier> modifiers) {
		super(parent, methodName, position);
		
		this.type = type;
		this.params = params;
		this.modifiers = modifiers;
	}

	/**
	 * Get type of this method. 
	 * 
	 * @return
	 * 			type of this method, or <tt>null</tt> for constructor
	 */
	@Nullable
	public String getType() {
		return type;
	}

	/**
	 * Get params of this method.
	 * 
	 * @return
	 * 			params of this method, or <tt>null</tt> if no params
	 */
	@Nullable
	public String getParams() {
		return params;
	}

	public List<Modifier> getModifiers() {
		return modifiers;
	}

	@Override
	public String getScope() {
		String scope = getParent().getScope();
		if (scope != null)
			return scope + "." + getParent().getIndexName();
		else
			return getParent().getIndexName();
	}

	@Override
	public boolean isPrimary() {
		return false;
	}

	@Override
	public Component render(String componentId, Range highlight) {
		return new MethodDefPanel(componentId, this, highlight);
	}

	@Override
	public Image renderIcon(String componentId) {
		Image icon;
		if (modifiers.contains(Modifier.PRIVATE)) {
			icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "methpri_obj.png"));
			icon.add(AttributeAppender.append("title", "private method"));
		}  else if (modifiers.contains(Modifier.PROTECTED)) {
			icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "methpro_obj.png"));
			icon.add(AttributeAppender.append("title", "protected method"));
		} else if (modifiers.contains(Modifier.PUBLIC)) {
			icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "methpub_obj.png"));
			icon.add(AttributeAppender.append("title", "public method"));
		} else {
			icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "methdef_obj.png"));
			icon.add(AttributeAppender.append("title", "method"));
		}
		return icon;
	}
	
}
