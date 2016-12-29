package com.gitplex.symbolextractor.java.symbols.ui;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.java.symbols.MethodDef;
import com.gitplex.symbolextractor.java.symbols.Modifier;
import com.gitplex.symbolextractor.java.symbols.ui.icon.IconLocator;
import com.gitplex.symbolextractor.util.HighlightableLabel;

@SuppressWarnings("serial")
public class MethodDefPanel extends JavaSymbolPanel {

	private final MethodDef methodDef;
	
	private final Range highlight;
	
	public MethodDefPanel(String id, MethodDef methodDef, Range highlight) {
		super(id);
		this.methodDef = methodDef;
		this.highlight = highlight;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String icon;
		if (methodDef.getModifiers().contains(Modifier.PRIVATE))
			icon = "methpri_obj.png";
		else if (methodDef.getModifiers().contains(Modifier.PROTECTED))
			icon = "methpro_obj.png";
		else if (methodDef.getModifiers().contains(Modifier.PUBLIC))
			icon = "methpub_obj.png";
		else
			icon = "methdef_obj.png";
		add(new Image("icon", new PackageResourceReference(IconLocator.class, icon)));
		
		add(new HighlightableLabel("name", methodDef.getIndexName(), highlight));
		add(new Label("params", methodDef.getParams()));
		add(new Label("type", methodDef.getType()).setVisible(methodDef.getType()!=null));
	}

}
