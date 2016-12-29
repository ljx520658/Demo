package com.gitplex.symbolextractor.java.symbols;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.gitplex.symbolextractor.Position;
import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.java.symbols.ui.TypeDefPanel;

public class TypeDef extends Symbol {

	private static final long serialVersionUID = 1L;

	public enum Kind {CLASS, INTERFACE, ANNOTATION, ENUM};

	private final Kind kind;
	
	private final String packageName;
	
	private final List<Modifier> modifiers;

	public TypeDef(@Nullable Symbol parent, @Nullable String packageName, 
			String className, Position from, Position to, Kind kind, List<Modifier> modifiers) {
		super(parent, className, from, to);

		this.packageName = packageName;
		this.kind = kind;
		this.modifiers = modifiers;
	}
	
	public Kind getKind() {
		return kind;
	}

	public List<Modifier> getModifiers() {
		return modifiers;
	}

	public String getPackageName() {
		return packageName;
	}

	@Override
	public String getScope() {
		if (getParent() != null) {
			String scope = getParent().getScope();
			if (scope != null)
				return scope + "." + getParent().getIndexName();
			else
				return getParent().getIndexName();
		} else {
			if (packageName != null)
				return packageName;
			else
				return null;
		}
	}

	@Override
	public boolean isPrimary() {
		return true;
	}

	@Override
	public Component render(String componentId, Range highlight) {
		return new TypeDefPanel(componentId, this, highlight);
	}

}
