package com.gitplex.symbolextractor.javascript.symbols;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.javascript.symbols.ui.icon.IconLocator;
import com.gitplex.symbolextractor.util.HighlightableLabel;

public class VariableSymbol extends JavaScriptSymbol {

	private static final long serialVersionUID = 1L;
	
	private final DeclarationType declarationType;
	
	public VariableSymbol(@Nullable Symbol parent, SyntaxToken token, DeclarationType declarationType) {
		super(parent, token);
		this.declarationType = declarationType;
	}

	@Override
	public boolean isPrimary() {
		return true;
	}

	public DeclarationType getDeclarationType() {
		return declarationType;
	}

	@Override
	public Component render(String componentId, Range highlight) {
		return new HighlightableLabel(componentId, getIndexName(), highlight);
	}

	@Override
	public Image renderIcon(String componentId) {
		Image icon;
		if (declarationType == DeclarationType.EXPORT) { 
			icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "exported_variable.png"));
			icon.add(AttributeAppender.append("title", "exported variable"));
		} else if (declarationType == DeclarationType.IMPORT) {
			icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "imported_variable.png"));
			icon.add(AttributeAppender.append("title", "imported variable"));
		} else {
			icon = new Image(componentId, new PackageResourceReference(IconLocator.class, "variable.png"));
			icon.add(AttributeAppender.append("title", "variable"));
		}
		return icon;
	}

}
