package com.gitplex.jsymbol.javascript;

import java.util.List;

import org.junit.Test;

import com.gitplex.jsymbol.DescribableExtractorTest;
import com.gitplex.jsymbol.SymbolExtractor;
import com.gitplex.jsymbol.javascript.symbols.ClassSymbol;
import com.gitplex.jsymbol.javascript.symbols.FunctionSymbol;
import com.gitplex.jsymbol.javascript.symbols.JavaScriptSymbol;
import com.gitplex.jsymbol.javascript.symbols.MethodAccess;
import com.gitplex.jsymbol.javascript.symbols.MethodSymbol;
import com.gitplex.jsymbol.javascript.symbols.ModuleAccess;
import com.gitplex.jsymbol.javascript.symbols.ObjectSymbol;

public class JavaScriptExtractorTest extends DescribableExtractorTest<JavaScriptSymbol> {

	@Test
	public void test() {
		SymbolExtractor<JavaScriptSymbol> extractor = new JavaScriptExtractor();
		verify(readFile("object-literal.outline"), extractor.extract(readFile("object-literal.source")));
		verify(readFile("variables.outline"), extractor.extract(readFile("variables.source")));
		verify(readFile("module.outline"), extractor.extract(readFile("module.source")));
		verify(readFile("class.outline"), extractor.extract(readFile("class.source")));
		verify(readFile("jquery.outline"), extractor.extract(readFile("jquery.source")));
		verify(readFile("commonjs.outline"), extractor.extract(readFile("commonjs.source")));
		verify(readFile("vue.outline"), extractor.extract(readFile("vue.source")));
	}

	@Override
	protected String describe(List<JavaScriptSymbol> context, JavaScriptSymbol symbol) {
		StringBuilder builder = new StringBuilder();
		if (symbol.getModuleAccess() == ModuleAccess.EXPORT)
			builder.append("export ");
		if (symbol.isLocal())
		    builder.append("local ");
		if (symbol instanceof MethodSymbol) {
			MethodSymbol methodSymbol = (MethodSymbol) symbol;
			if (methodSymbol.getMethodAccess() == MethodAccess.GET)
				builder.append("get ").append(methodSymbol.getName()).append(methodSymbol.getParameters());
			else if (methodSymbol.getMethodAccess() == MethodAccess.SET)
				builder.append("set ").append(methodSymbol.getName()).append(methodSymbol.getParameters());
			else
				builder.append(methodSymbol.getName()).append(methodSymbol.getParameters());
		} else if (symbol instanceof FunctionSymbol) {
			FunctionSymbol functionSymbol = (FunctionSymbol) symbol;
			if (functionSymbol.getName() != null)
				builder.append("function ").append(functionSymbol.getName()).append(functionSymbol.getParameters());
			else
				builder.append("function").append(functionSymbol.getParameters());
		} else if (symbol instanceof ClassSymbol) {
			ClassSymbol classSymbol = (ClassSymbol) symbol;
			builder.append("class ").append(classSymbol.getName());
		} else {
			ObjectSymbol object = (ObjectSymbol) symbol;
			builder.append(object.getName());
		}
		appendChildren(builder, context, symbol);
		return builder.toString();
	}
}
