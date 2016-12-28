package com.gitplex.symbolextractor.java;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gitplex.symbolextractor.AbstractExtractorTest;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.java.symbols.CompilationUnit;
import com.gitplex.symbolextractor.java.symbols.FieldDef;
import com.gitplex.symbolextractor.java.symbols.MethodDef;
import com.gitplex.symbolextractor.java.symbols.Modifier;
import com.gitplex.symbolextractor.java.symbols.TypeDef;
import com.gitplex.symbolextractor.java.symbols.TypeDef.Kind;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class JavaExtractorTest extends AbstractExtractorTest {

	@Test
	public void testComposite() {
		assertSymbol(readFile("composite.symbols"), 
				new JavaExtractor().extract(readFile("composite.source")));
	}

	@Test
	public void testPackageInfo() {
		assertSymbol(readFile("package-info.symbols"), 
				new JavaExtractor().extract(readFile("package-info.source")));
	}
	
	@Test
	public void testLCount() {
		assertSymbol(readFile("LCount.symbols"), 
				new JavaExtractor().extract(readFile("LCount.source")));
	}
	
	@Test
	public void testResource() {
		assertSymbol(readFile("Resource.symbols"), 
				new JavaExtractor().extract(readFile("Resource.source")));
	}

	@Override
	protected String describe(List<Symbol> context, Symbol symbol) {
		if (symbol instanceof CompilationUnit) {
			CompilationUnit compilationUnit = (CompilationUnit) symbol;
			StringBuilder builder =  new StringBuilder();
			if (compilationUnit.getPackageName() != null)
				builder.append("package ").append(compilationUnit.getPackageName()).append(";\n\n");
			
			return builder.toString();
		} else if (symbol instanceof TypeDef) {
			TypeDef typeDef = (TypeDef) symbol;
			StringBuilder builder = new StringBuilder();
			for (Modifier modifier: typeDef.getModifiers()) 
				builder.append(modifier.name().toLowerCase()).append(" ");

			if (typeDef.getKind() == Kind.ANNOTATION)
				builder.append("@interface").append(" ");
			else
				builder.append(typeDef.getKind().toString().toLowerCase()).append(" ");
			builder.append(typeDef.getName()).append(" {\n\n");
			
			List<String> enumConstants = new ArrayList<>();
			for (Symbol each: context) {
				if (each.getParent() == symbol && (each instanceof FieldDef)) {
					FieldDef fieldDef = (FieldDef) each;
					if (fieldDef.getType() == null)  
						enumConstants.add(fieldDef.getName());
				}
			}
			if (!enumConstants.isEmpty())
				builder.append("  ").append(Joiner.on(", ").join(enumConstants)).append(";\n\n");
			else if (typeDef.getKind() == Kind.ENUM)
				builder.append("  ;\n\n");
			
			for (Symbol each: context) {
				if (each.getParent() == symbol && (each instanceof FieldDef)) {
					FieldDef fieldDef = (FieldDef) each;
					if (fieldDef.getType() != null)
						builder.append("  ").append(describe(context, fieldDef)).append("\n\n");
				}
			}
			
			for (Symbol each: context) { 
				if (each.getParent() == symbol && (each instanceof MethodDef)) {
					MethodDef methodDef = (MethodDef) each;
					builder.append("  ").append(describe(context, methodDef)).append("\n\n");
				}
			}

			for (Symbol each: context) { 
				if (each.getParent() == symbol && (each instanceof TypeDef)) {
					TypeDef subTypeDef = (TypeDef) each;
					for (String line: Splitter.on('\n').omitEmptyStrings().split(describe(context, subTypeDef)))
						builder.append("  ").append(line).append("\n\n");
				}
			}
			
			builder.append("}\n\n");
			
			return builder.toString();
		} else if (symbol instanceof FieldDef) {
			FieldDef fieldDef = (FieldDef) symbol;
			StringBuilder builder = new StringBuilder();
			for (Modifier modifier: fieldDef.getModifiers()) 
				builder.append(modifier.name().toLowerCase()).append(" ");
			if (fieldDef.getType() != null)
				builder.append(fieldDef.getType()).append(" ");
			builder.append(fieldDef.getName()).append(";");
			return builder.toString();
		} else if (symbol instanceof MethodDef) {
			MethodDef methodDef = (MethodDef) symbol;
			StringBuilder builder = new StringBuilder();
			for (Modifier modifier: methodDef.getModifiers()) 
				builder.append(modifier.name().toLowerCase()).append(" ");
			if (methodDef.getType() != null)
				builder.append(methodDef.getType()).append(" ");
			builder.append(methodDef.getName());
			if (methodDef.getParams() != null)
				builder.append("(").append(methodDef.getParams()).append(");");
			else
				builder.append("();");
			return builder.toString();
		} else {
			throw new RuntimeException("Unexpected symbol type: " + symbol.getClass());
		}
	}

}
