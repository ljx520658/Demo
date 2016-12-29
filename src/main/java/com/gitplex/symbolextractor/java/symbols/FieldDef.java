package com.gitplex.symbolextractor.java.symbols;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.Component;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.gitplex.symbolextractor.Position;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.java.symbols.ui.FieldDefPanel;
import com.gitplex.symbolextractor.java.symbols.ui.icon.IconLocator;

public class FieldDef extends Symbol {

	private static final long serialVersionUID = 1L;

	private final String type;
	
	private final List<Modifier> modifiers;
	
	public FieldDef(TypeDef parent, String name, Position from, Position to, 
			@Nullable String type, List<Modifier> modifiers) {
		super(parent, name, from, to);
		
		this.type = type;
		this.modifiers = modifiers;
	}
	
	/**
	 * Get type of this field.
	 * 
	 * @return 
	 * 			type of this field, or <tt>null</tt> for enum constant
	 */
	@Nullable
	public String getType() {
		return type;
	}

	public List<Modifier> getModifiers() {
		return modifiers;
	}

	@Override
	public String getScope() {
		String scope = getParent().getScope();
		if (scope != null)
			return scope + "." + getParent().getName();
		else
			return getParent().getName();
	}

	@Override
	public boolean isPrimary() {
		return false;
	}

	@Override
	public Component render(String componentId, Pair<Integer, Integer> highlight) {
		return new FieldDefPanel(componentId, this, highlight);
	}

	@Override
	public ResourceReference getIcon() {
		String icon;
		if (modifiers.contains(Modifier.PRIVATE))
			icon = "field_private_obj.png";
		else if (modifiers.contains(Modifier.PROTECTED))
			icon = "field_protected_obj.png";
		else if (modifiers.contains(Modifier.PUBLIC))
			icon = "field_public_obj.png";
		else
			icon = "field_default_obj.png";
		return new PackageResourceReference(IconLocator.class, icon);
	}

}
