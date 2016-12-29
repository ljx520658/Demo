package com.gitplex.symbolextractor.java.symbols.ui;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.java.symbols.FieldDef;
import com.gitplex.symbolextractor.java.symbols.Modifier;
import com.gitplex.symbolextractor.java.symbols.ui.icon.IconLocator;
import com.gitplex.symbolextractor.util.HighlightableLabel;

@SuppressWarnings("serial")
public class FieldDefPanel extends JavaSymbolPanel {

	private final FieldDef fieldDef;
	
	private final Range highlight;
	
	public FieldDefPanel(String id, FieldDef fieldDef, Range highlight) {
		super(id);
		this.fieldDef = fieldDef;
		this.highlight = highlight;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String icon;
		if (fieldDef.getModifiers().contains(Modifier.PRIVATE))
			icon = "field_private_obj.png";
		else if (fieldDef.getModifiers().contains(Modifier.PROTECTED))
			icon = "field_protected_obj.png";
		else if (fieldDef.getModifiers().contains(Modifier.PUBLIC))
			icon = "field_public_obj.png";
		else
			icon = "field_default_obj.png";
		add(new Image("icon", new PackageResourceReference(IconLocator.class, icon)));
		
		add(new HighlightableLabel("name", fieldDef.getIndexName(), highlight));
		
		add(new Label("type", fieldDef.getType()).setVisible(fieldDef.getType()!=null));
	}

}
