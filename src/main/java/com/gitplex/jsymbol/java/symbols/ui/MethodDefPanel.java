package com.gitplex.jsymbol.java.symbols.ui;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.java.symbols.MethodSymbol;
import com.gitplex.jsymbol.util.HighlightableLabel;

@SuppressWarnings("serial")
public class MethodDefPanel extends Panel {

	private final MethodSymbol methodDef;
	
	private final Range highlight;
	
	public MethodDefPanel(String id, MethodSymbol methodDef, Range highlight) {
		super(id);
		this.methodDef = methodDef;
		this.highlight = highlight;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new HighlightableLabel("name", methodDef.getName(), highlight));
		
		if (methodDef.getMethodParams() != null)
			add(new Label("methodParams", methodDef.getMethodParams()));
		else
			add(new WebMarkupContainer("methodParams").setVisible(false));
		
		if (methodDef.getTypeParams() != null) 
			add(new Label("typeParams", methodDef.getTypeParams()));
		else
			add(new WebMarkupContainer("typeParams").setVisible(false));
		
		add(new Label("type", methodDef.getType()).setVisible(methodDef.getType()!=null));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new JavaSymbolResourceReference()));
	}
	
}
