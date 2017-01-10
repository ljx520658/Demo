package com.gitplex.jsymbol.javascript;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gitplex.jsymbol.DescribableExtractorTest;
import com.gitplex.jsymbol.SymbolExtractor;
import com.gitplex.jsymbol.javascript.symbols.ClassSymbol;
import com.gitplex.jsymbol.javascript.symbols.FunctionSymbol;
import com.gitplex.jsymbol.javascript.symbols.JavaScriptSymbol;
import com.gitplex.jsymbol.javascript.symbols.MethodAccessorType;
import com.gitplex.jsymbol.javascript.symbols.MethodSymbol;
import com.gitplex.jsymbol.javascript.symbols.PropertySymbol;
import com.gitplex.jsymbol.javascript.symbols.ReferenceSymbol;
import com.gitplex.jsymbol.javascript.symbols.VariableSymbol;
import com.google.common.base.Splitter;

public class JavaScriptExtractorTest extends DescribableExtractorTest<JavaScriptSymbol> {

	@Test
	public void test() {
		SymbolExtractor<JavaScriptSymbol> extractor = new JavaScriptExtractor();
		verify(readFile("object-literal.symbols"), extractor.extract(readFile("object-literal.source")));
		verify(readFile("variables.symbols"), extractor.extract(readFile("variables.source")));
		verify(readFile("module.symbols"), extractor.extract(readFile("module.source")));
		verify(readFile("class.symbols"), extractor.extract(readFile("class.source")));
		verify(readFile("jquery.symbols"), extractor.extract(readFile("jquery.source")));
		verify(readFile("commonjs.symbols"), extractor.extract(readFile("commonjs.source")));
		verify(readFile("vue.symbols"), extractor.extract(readFile("vue.source")));
	}

	@Override
	protected String describe(List<JavaScriptSymbol> context, JavaScriptSymbol symbol) {
		StringBuilder builder = new StringBuilder();
		if (symbol.isExported())
			builder.append("export ");
		if (symbol.isLocal())
		    builder.append("local ");
		if (symbol instanceof VariableSymbol) {
			VariableSymbol variable = (VariableSymbol) symbol;
			builder.append("var ").append(variable.getName());
		} else if (symbol instanceof ReferenceSymbol) {
			ReferenceSymbol referenceSymbol = (ReferenceSymbol) symbol;
			if (referenceSymbol.getObject() != null)
				builder.append(referenceSymbol.getObject()).append(".").append(referenceSymbol.getName());
			else
				builder.append(referenceSymbol.getName());
		} else if (symbol instanceof PropertySymbol) {
			PropertySymbol propertySymbol = (PropertySymbol) symbol;
			builder.append(propertySymbol.getName());
		} else if (symbol instanceof MethodSymbol) {
			MethodSymbol methodSymbol = (MethodSymbol) symbol;
			if (methodSymbol.getAccessorType() == MethodAccessorType.GET)
				builder.append("get ").append(methodSymbol.getName()).append(methodSymbol.getParams());
			else if (methodSymbol.getAccessorType() == MethodAccessorType.SET)
				builder.append("set ").append(methodSymbol.getName()).append(methodSymbol.getParams());
			else
				builder.append(methodSymbol.getName()).append(methodSymbol.getParams());
		} else if (symbol instanceof FunctionSymbol) {
			FunctionSymbol functionSymbol = (FunctionSymbol) symbol;
			if (functionSymbol.getName() != null)
				builder.append("function ").append(functionSymbol.getName()).append(functionSymbol.getParams());
			else
				builder.append("function").append(functionSymbol.getParams());
		} else if (symbol instanceof ClassSymbol) {
			ClassSymbol classSymbol = (ClassSymbol) symbol;
			builder.append("class ").append(classSymbol.getName());
		} else {
			throw new RuntimeException("Unrecognized symbol class: " + symbol.getClass());
		}
		List<JavaScriptSymbol> children = new ArrayList<>();
		for (JavaScriptSymbol each: context) {
			if (each.getParent() == symbol)
				children.add(each);
		}
		if (!children.isEmpty()) {
			builder.append(" {\n");
			for (JavaScriptSymbol child: children) {
				for (String line: Splitter.on("\n").omitEmptyStrings().split(describe(context, child))) {
					builder.append("  ").append(line).append("\n");
				}
			}
			builder.append("}");
		}
		builder.append("\n");
		return builder.toString();
	}
}
