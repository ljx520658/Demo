package com.gitplex.jsymbol.java.symbols.ui;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.java.symbols.TypeDef;
import com.gitplex.jsymbol.util.HighlightableLabel;

@SuppressWarnings("serial")
public class TypeDefPanel extends Panel {

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
		
		add(new HighlightableLabel("name", typeDef.getName(), highlight));

		if (typeDef.getTypeParams() != null) 
			add(new Label("typeParams", typeDef.getTypeParams()));
		else
			add(new WebMarkupContainer("typeParams").setVisible(false));
	}

}