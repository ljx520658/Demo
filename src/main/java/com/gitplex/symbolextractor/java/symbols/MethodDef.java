package com.gitplex.symbolextractor.java.symbols;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.gitplex.symbolextractor.Position;
import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.java.symbols.ui.MethodDefPanel;

public class MethodDef extends Symbol {

	private static final long serialVersionUID = 1L;

	private final String type; 
	
	private final String params;

	private final List<Modifier> modifiers;
	
	public MethodDef(TypeDef parent, String methodName, Position from, Position to, 
			@Nullable String type, @Nullable String params, List<Modifier> modifiers) {
		super(parent, methodName, from, to);
		
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
	
}
