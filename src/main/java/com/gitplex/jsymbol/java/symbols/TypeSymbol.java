package com.gitplex.jsymbol.java.symbols;

import java.util.EnumSet;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.github.javaparser.ast.Modifier;
import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.Symbol;
import com.gitplex.jsymbol.TokenPosition;
import com.gitplex.jsymbol.java.symbols.ui.TypeDefPanel;
import com.gitplex.jsymbol.java.symbols.ui.icon.IconLocator;
import com.gitplex.jsymbol.util.NoAntiCacheImage;

public class TypeSymbol extends JavaSymbol {

	private static final long serialVersionUID = 1L;

	public enum Kind {CLASS, INTERFACE, ANNOTATION, ENUM};

	private final Kind kind;
	
	private final String typeParams;
	
	private final EnumSet<Modifier> modifiers;

	public TypeSymbol(@Nullable Symbol parent, String typeName, TokenPosition position, TokenPosition scope,
			Kind kind, @Nullable String typeParams, EnumSet<Modifier> modifiers) {
		super(parent, typeName, position, scope, modifiers.contains(Modifier.PRIVATE));

		this.kind = kind;
		this.typeParams = typeParams;
		this.modifiers = modifiers;
	}
	
	public Kind getKind() {
		return kind;
	}

	public String getTypeParams() {
		return typeParams;
	}

	public EnumSet<Modifier> getModifiers() {
		return modifiers;
	}

	@Override
	public boolean isPrimary() {
		return true;
	}

	@Override
	public Component render(String componentId, Range highlight) {
		return new TypeDefPanel(componentId, this, highlight);
	}

	@Override
	public Image renderIcon(String componentId) {
		Image icon;
		if (kind == Kind.ENUM) {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "enum_obj.png"));
			icon.add(AttributeAppender.append("title", "enum"));
		} else if (kind == Kind.INTERFACE) {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "int_obj.png"));
			icon.add(AttributeAppender.append("title", "interface"));
		} else if (kind == Kind.ANNOTATION) {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "annotation_obj.png"));
			icon.add(AttributeAppender.append("title", "annotation"));
		} else if (kind == Kind.CLASS) {
			if (modifiers.contains(Modifier.PRIVATE)) {
				icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "innerclass_private_obj.png"));
				icon.add(AttributeAppender.append("title", "private inner class"));
			}  else if (modifiers.contains(Modifier.PROTECTED)) {
				icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "innerclass_protected_obj.png"));
				icon.add(AttributeAppender.append("title", "protected inner class"));
			} else if (modifiers.contains(Modifier.PUBLIC)) {
				icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "class_obj.png"));
				icon.add(AttributeAppender.append("title", "public class"));
			} else {
				icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "class_default_obj.png"));
				icon.add(AttributeAppender.append("title", "class"));
			}
		} else {
			throw new RuntimeException("Unrecognized type: " + kind);
		}
		return icon;
	}

}
