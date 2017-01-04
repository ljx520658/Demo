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
import com.gitplex.symbolextractor.java.symbols.ui.icon.IconLocator;
import com.gitplex.symbolextractor.util.HighlightableLabel;

public class TypeDef extends Symbol {

	private static final long serialVersionUID = 1L;

	public enum Kind {CLASS, INTERFACE, ANNOTATION, ENUM};

	private final Kind kind;
	
	private final String packageName;
	
	private final List<Modifier> modifiers;

	public TypeDef(@Nullable Symbol parent, @Nullable String packageName, 
			String className, TokenPosition position, Kind kind, List<Modifier> modifiers) {
		super(parent, className, position);

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
		return new HighlightableLabel(componentId, getIndexName(), highlight);
	}

	@Override
	public Image renderIcon(String componentId) {
		Image icon;
		if (kind == Kind.ENUM) {
			icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "enum_obj.png"));
			icon.add(AttributeAppender.append("title", "enum"));
		} else if (kind == Kind.INTERFACE) {
			icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "int_obj.png"));
			icon.add(AttributeAppender.append("title", "interface"));
		} else if (kind == Kind.ANNOTATION) {
			icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "annotation_obj.png"));
			icon.add(AttributeAppender.append("title", "annotation"));
		} else if (kind == Kind.CLASS) {
			if (modifiers.contains(Modifier.PRIVATE)) {
				icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "innerclass_private_obj.png"));
				icon.add(AttributeAppender.append("title", "private inner class"));
			}  else if (modifiers.contains(Modifier.PROTECTED)) {
				icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "innerclass_protected_obj.png"));
				icon.add(AttributeAppender.append("title", "protected inner class"));
			} else if (modifiers.contains(Modifier.PUBLIC)) {
				icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "class_obj.png"));
				icon.add(AttributeAppender.append("title", "public class"));
			} else {
				icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "class_default_obj.png"));
				icon.add(AttributeAppender.append("title", "class"));
			}
		} else {
			throw new RuntimeException("Unrecognized type: " + kind);
		}
		return icon;
	}

}
