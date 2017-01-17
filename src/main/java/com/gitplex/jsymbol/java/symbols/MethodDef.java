package com.gitplex.jsymbol.java.symbols;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.sonar.plugins.java.api.tree.Modifier;

import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.TokenPosition;
import com.gitplex.jsymbol.java.symbols.ui.MethodDefPanel;
import com.gitplex.jsymbol.java.symbols.ui.icon.IconLocator;
import com.gitplex.jsymbol.util.NoAntiCacheImage;

public class MethodDef extends JavaSymbol {

	private static final long serialVersionUID = 1L;

	private final String type; 
	
	private final String methodParams;
	
	private final String typeParams;

	private final List<Modifier> modifiers;
	
	public MethodDef(TypeDef parent, String methodName, TokenPosition position, TokenPosition scope,
			@Nullable String type, @Nullable String methodParams, @Nullable String typeParams, 
			List<Modifier> modifiers) {
		super(parent, methodName, position, scope, modifiers.contains(Modifier.PRIVATE), new ArrayList<>());
		
		this.type = type;
		this.methodParams = methodParams;
		this.typeParams = typeParams;
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
	 * Get method params
	 * 
	 * @return
	 * 			method params, or <tt>null</tt> if no method params
	 */
	@Nullable
	public String getMethodParams() {
		return methodParams;
	}

	/**
	 * Get type params 
	 * 
	 * @return
	 * 			type params, or <tt>null</tt> if no type params
	 */
	@Nullable
	public String getTypeParams() {
		return typeParams;
	}

	public List<Modifier> getModifiers() {
		return modifiers;
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
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "methpri_obj.png"));
			icon.add(AttributeAppender.append("title", "private method"));
		}  else if (modifiers.contains(Modifier.PROTECTED)) {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "methpro_obj.png"));
			icon.add(AttributeAppender.append("title", "protected method"));
		} else if (modifiers.contains(Modifier.PUBLIC)) {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "methpub_obj.png"));
			icon.add(AttributeAppender.append("title", "public method"));
		} else {
			icon = new NoAntiCacheImage(componentId, new PackageResourceReference(IconLocator.class, "methdef_obj.png"));
			icon.add(AttributeAppender.append("title", "method"));
		}
		return icon;
	}
	
}
