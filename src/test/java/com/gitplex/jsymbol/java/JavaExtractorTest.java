package com.gitplex.jsymbol.java;

import java.util.List;

import org.junit.Test;
import org.sonar.plugins.java.api.tree.Modifier;

import com.gitplex.jsymbol.DescribableExtractorTest;
import com.gitplex.jsymbol.java.symbols.CompilationUnit;
import com.gitplex.jsymbol.java.symbols.FieldDef;
import com.gitplex.jsymbol.java.symbols.JavaSymbol;
import com.gitplex.jsymbol.java.symbols.MethodDef;
import com.gitplex.jsymbol.java.symbols.TypeDef;
import com.gitplex.jsymbol.java.symbols.TypeDef.Kind;
import com.google.common.base.Joiner;

public class JavaExtractorTest extends DescribableExtractorTest<JavaSymbol> {

	@Test
	public void test() {
		verify(readFile("test.outline"), new JavaExtractor().extract(readFile("test.source")));
		verify(readFile("composite.outline"), new JavaExtractor().extract(readFile("composite.source")));
		verify(readFile("lcount.outline"), new JavaExtractor().extract(readFile("lcount.source")));
		verify(readFile("resource.outline"), new JavaExtractor().extract(readFile("resource.source")));
	}

	@Override
	protected String describe(List<JavaSymbol> context, JavaSymbol symbol) {
		StringBuilder builder = new StringBuilder();
		if (symbol instanceof CompilationUnit) {
			CompilationUnit compilationUnit = (CompilationUnit) symbol;
			if (compilationUnit.getName() != null)
				builder.append("package ").append(compilationUnit.getName());
		} else if (symbol instanceof TypeDef) {
			TypeDef typeDef = (TypeDef) symbol;
			for (Modifier modifier: typeDef.getModifiers()) 
				builder.append(modifier.name().toLowerCase()).append(" ");

			if (typeDef.getKind() == Kind.ANNOTATION)
				builder.append("@interface").append(" ");
			else
				builder.append(typeDef.getKind().toString().toLowerCase()).append(" ");
			builder.append(typeDef.getName());
			if (typeDef.getTypeParams() != null)
				builder.append(typeDef.getTypeParams());
			if (!typeDef.getSuperSymbolNames().isEmpty()) {
				builder.append(" extends ");
				builder.append(Joiner.on(",").join(typeDef.getSuperSymbolNames()));
			}
		} else if (symbol instanceof FieldDef) {
			FieldDef fieldDef = (FieldDef) symbol;
			for (Modifier modifier: fieldDef.getModifiers()) 
				builder.append(modifier.name().toLowerCase()).append(" ");
			if (fieldDef.getType() != null)
				builder.append(fieldDef.getType()).append(" ");
			builder.append(fieldDef.getName());
		} else if (symbol instanceof MethodDef) {
			MethodDef methodDef = (MethodDef) symbol;
			for (Modifier modifier: methodDef.getModifiers()) 
				builder.append(modifier.name().toLowerCase()).append(" ");
			if (methodDef.getTypeParams() != null)
				builder.append(methodDef.getTypeParams()).append(" ");
			if (methodDef.getType() != null)
				builder.append(methodDef.getType()).append(" ");
			builder.append(methodDef.getName());
			if (methodDef.getMethodParams() != null)
				builder.append("(").append(methodDef.getMethodParams()).append(")");
			else
				builder.append("()");
		} else {
			throw new RuntimeException("Unexpected symbol type: " + symbol.getClass());
		}
		
		appendChildren(builder, context, symbol);
		return builder.toString();
	}

}
