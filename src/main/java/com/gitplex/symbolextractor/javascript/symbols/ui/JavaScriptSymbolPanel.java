package com.gitplex.symbolextractor.javascript.symbols.ui;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;

public class JavaScriptSymbolPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public JavaScriptSymbolPanel(String id) {
		super(id);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new JavaScriptSymbolResourceReference()));
	}

}
