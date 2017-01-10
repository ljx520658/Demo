package com.gitplex.jsymbol.java.symbols.ui;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.java.symbols.MethodDef;
import com.gitplex.jsymbol.util.HighlightableLabel;

@SuppressWarnings("serial")
public class MethodDefPanel extends Panel {

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
		
		add(new HighlightableLabel("name", methodDef.getName(), highlight));
		add(new Label("params", methodDef.getParams()));
		add(new Label("type", methodDef.getType()).setVisible(methodDef.getType()!=null));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new JavaSymbolResourceReference()));
	}
	
}
