package com.gitplex.symbolextractor.java;

import java.util.List;

import javax.annotation.Nullable;

import com.gitplex.symbolextractor.Position;
import com.gitplex.symbolextractor.Symbol;

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

}
