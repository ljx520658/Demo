package com.gitplex.symbolextractor.java.symbols;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.gitplex.symbolextractor.Position;
import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.java.symbols.ui.FieldDefPanel;

public class FieldDef extends Symbol {

	private static final long serialVersionUID = 1L;

	private final String type;
	
	private final List<Modifier> modifiers;
	
	public FieldDef(TypeDef parent, String fieldName, Position from, Position to, 
			@Nullable String type, List<Modifier> modifiers) {
		super(parent, fieldName, from, to);
		
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
		return new FieldDefPanel(componentId, this, highlight);
	}

}
