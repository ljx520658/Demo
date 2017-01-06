package com.gitplex.symbolextractor.web;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxChannel.Type;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.reflections.Reflections;

import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.SymbolExtractor;
import com.gitplex.symbolextractor.java.JavaExtractor;
import com.google.common.base.Throwables;

@SuppressWarnings("serial")
public class HomePage extends WebPage {
	
	private List<? extends Symbol> symbols = new ArrayList<>();
	
	private String error;
	
	public HomePage(final PageParameters parameters) {
		super(parameters);
    }

	@SuppressWarnings("rawtypes")
	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<String> extractorClassNames = new ArrayList<>();
		Reflections reflections = new Reflections(SymbolExtractor.class.getPackage().getName());
		for (Class<? extends SymbolExtractor> extractorClass: reflections.getSubTypesOf(SymbolExtractor.class)) {
			if (!Modifier.isAbstract(extractorClass.getModifiers())) {
				extractorClassNames.add(extractorClass.getName());
			}
		}
		Collections.sort(extractorClassNames);
		
		DropDownChoice<String> extractorChoice = new DropDownChoice<String>("extractor", Model.of(JavaExtractor.class.getName()), extractorClassNames);
		TextArea<String> sourceInput;
		add(sourceInput = new TextArea<String>("source", Model.of("")));
		extractorChoice.add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onChange(target, sourceInput.getModelObject(), extractorChoice.getModelObject());
			}
			
		});
		add(extractorChoice);
		
		sourceInput.add(new OnChangeAjaxBehavior() {
				
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setChannel(new AjaxChannel(AjaxChannel.DEFAULT_NAME, Type.DROP));
			}

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onChange(target, sourceInput.getModelObject(), extractorChoice.getModelObject());
			}
			
		});		
		
		add(newOutline());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void onChange(AjaxRequestTarget target, String sourceCode, String extractorClassName) {
		if (StringUtils.isNotBlank(sourceCode)) {
			try {
				symbols = ((SymbolExtractor)Class.forName(extractorClassName).newInstance()).extract(sourceCode);
				error = null;
			} catch (Exception e) {
				error = Throwables.getStackTraceAsString(e);
				symbols.clear();
			}
		} else {
			error = null;
			symbols.clear();
		}
		Component outline = newOutline();
		HomePage.this.replace(outline);
		target.add(outline);
	}
	
	private Component newOutline() {
		if (error != null) {
			return new MultiLineLabel("outline", error).setOutputMarkupId(true);
		} else {
			NestedTree<Symbol> outline = new NestedTree<Symbol>("outline", new ITreeProvider<Symbol>() {

				@Override
				public void detach() {
				}

				@Override
				public Iterator<? extends Symbol> getRoots() {
					return getChildSymbols(null).iterator();
				}

				@Override
				public boolean hasChildren(Symbol symbol) {
					return !getChildSymbols(symbol).isEmpty();
				}

				@Override
				public Iterator<? extends Symbol> getChildren(Symbol symbol) {
					return getChildSymbols(symbol).iterator();
				}

				@Override
				public IModel<Symbol> model(Symbol symbol) {
					return Model.of(symbol);
				}
				
			}) {

				@Override
				protected void onInitialize() {
					super.onInitialize();
					add(new HumanTheme());				
				}

				@Override
				protected Component newContentComponent(String id, IModel<Symbol> nodeModel) {
					Fragment fragment = new Fragment(id, "outlineNodeFrag", HomePage.this);
					Symbol symbol = nodeModel.getObject();
					fragment.add(symbol.renderIcon("icon"));
					fragment.add(symbol.render("symbol", null));
					
					return fragment;
				}
				
			};		
			
			for (Symbol root: getChildSymbols(null))
				outline.expand(root);
			
			outline.setOutputMarkupId(true);
			return outline;
		}
	}
	
	private List<Symbol> getChildSymbols(@Nullable Symbol parentSymbol) {
		List<Symbol> children = new ArrayList<>();
		for (Symbol symbol: symbols) {
			if (symbol.getParent() == parentSymbol)
				children.add(symbol);
		}
		return children;
	}
	
}
