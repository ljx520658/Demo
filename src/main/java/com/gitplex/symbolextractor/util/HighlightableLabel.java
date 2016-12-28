package com.gitplex.symbolextractor.util;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;

@SuppressWarnings("serial")
public class HighlightableLabel extends Label {

	public HighlightableLabel(String id, @Nullable String label, @Nullable Pair<Integer, Integer> highlight) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (label != null) {
					if (highlight != null) {
						String prefix = label.substring(0, highlight.getLeft());
						String middle = label.substring(highlight.getLeft(), highlight.getRight());
						String suffix = label.substring(highlight.getRight());
						return HtmlEscape.escape(prefix) 
								+ "<b>" 
								+ HtmlEscape.escape(middle) 
								+ "</b>" 
								+ HtmlEscape.escape(suffix);
					} else {
						return HtmlEscape.escape(label);
					}
				} else {
					return "";
				}
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setEscapeModelStrings(false);
	}

}
