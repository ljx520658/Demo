package com.gitplex.symbolextractor.java.symbols.ui;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.java.symbols.Modifier;
import com.gitplex.symbolextractor.java.symbols.TypeDef;
import com.gitplex.symbolextractor.java.symbols.ui.icon.IconLocator;
import com.gitplex.symbolextractor.util.HighlightableLabel;

@SuppressWarnings("serial")
public class TypeDefPanel extends JavaSymbolPanel {

	private final TypeDef typeDef;
	
	private final Range highlight;
	
	public TypeDefPanel(String id, TypeDef typeDef, Range highlight) {
		super(id);
		this.typeDef = typeDef;
		this.highlight = highlight;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String icon;
		switch (typeDef.getKind()) {
		case ENUM:
			icon = "enum_obj.png";
			break;
		case INTERFACE:
			icon = "int_obj.png";
			break;
		case ANNOTATION:
			icon = "annotation_obj.png";
			break;
		case CLASS:
			if (typeDef.getModifiers().contains(Modifier.PRIVATE))
				icon = "innerclass_private_obj.png";
			else if (typeDef.getModifiers().contains(Modifier.PROTECTED))
				icon = "innerclass_protected_obj.png";
			else if (typeDef.getModifiers().contains(Modifier.PUBLIC))
				icon = "class_obj.png";
			else
				icon = "class_default_obj.png";
			break;
		default:
			throw new IllegalStateException();
		}

		add(new Image("icon", new PackageResourceReference(IconLocator.class, icon)));
		add(new HighlightableLabel("name", typeDef.getIndexName(), highlight));
	}

}
