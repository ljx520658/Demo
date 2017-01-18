package com.gitplex.jsymbol.java;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.sonar.java.ast.parser.JavaParser;
import org.sonar.plugins.java.api.tree.ArrayTypeTree;
import org.sonar.plugins.java.api.tree.BlockTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.EmptyStatementTree;
import org.sonar.plugins.java.api.tree.EnumConstantTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Modifier;
import org.sonar.plugins.java.api.tree.ModifierKeywordTree;
import org.sonar.plugins.java.api.tree.ModifierTree;
import org.sonar.plugins.java.api.tree.ModifiersTree;
import org.sonar.plugins.java.api.tree.PackageDeclarationTree;
import org.sonar.plugins.java.api.tree.ParameterizedTypeTree;
import org.sonar.plugins.java.api.tree.PrimitiveTypeTree;
import org.sonar.plugins.java.api.tree.SyntaxToken;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TypeParameterTree;
import org.sonar.plugins.java.api.tree.TypeParameters;
import org.sonar.plugins.java.api.tree.TypeTree;
import org.sonar.plugins.java.api.tree.VariableTree;
import org.sonar.plugins.java.api.tree.WildcardTree;

import com.gitplex.jsymbol.AbstractSymbolExtractor;
import com.gitplex.jsymbol.ExtractException;
import com.gitplex.jsymbol.TokenPosition;
import com.gitplex.jsymbol.java.symbols.CompilationUnit;
import com.gitplex.jsymbol.java.symbols.FieldDef;
import com.gitplex.jsymbol.java.symbols.JavaSymbol;
import com.gitplex.jsymbol.java.symbols.MethodDef;
import com.gitplex.jsymbol.java.symbols.TypeDef;
import com.gitplex.jsymbol.java.symbols.TypeDef.Kind;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.sonar.sslr.api.RecognitionException;

public class JavaExtractor extends AbstractSymbolExtractor<JavaSymbol> {

	@Override
	public List<JavaSymbol> extract(String source) {
		List<JavaSymbol> symbols = new ArrayList<>();
		Tree tree;
		try {
			tree = JavaParser.createParser(Charsets.UTF_8).parse(source);
		} catch (RecognitionException e) {
			throw new ExtractException("Error parsing java", e);
		}

		if (tree instanceof CompilationUnitTree) {
			extractFromCompilationUnitTree((CompilationUnitTree) tree, symbols);
		}
		
		return symbols;
	}

	private TokenPosition getPosition(SyntaxToken token) {
		return new TokenPosition(token.line()-1, token.column(), token.line()-1, token.column()+token.text().length());
	}
	
	private TokenPosition getPosition(SyntaxToken from, SyntaxToken to) {
		return new TokenPosition(from.line()-1, from.column(), to.line()-1, to.column()+to.text().length());
	}
	
	private void extractFromCompilationUnitTree(CompilationUnitTree compilationUnit, List<JavaSymbol> symbols) {
		JavaSymbol parent = null;
		PackageDeclarationTree packageDeclaration = compilationUnit.packageDeclaration();
		if (packageDeclaration != null) {
			ExpressionTree expression = packageDeclaration.packageName();
			TokenPosition position = getPosition(expression.firstToken(), expression.lastToken());
			TokenPosition scope = getPosition(packageDeclaration.firstToken(), packageDeclaration.lastToken());
			String packageName = getFullQualifiedName(expression);
			parent = new CompilationUnit(packageName, position, scope);
			symbols.add(parent);
		}
		for (Tree tree: compilationUnit.types()) {
			if (tree instanceof ClassTree) {
				extractFromClassTree((ClassTree) tree, parent, symbols);
			}
		}
	}
	
	private void extractFromClassTree(ClassTree classTree, JavaSymbol parent, List<JavaSymbol> symbols) {
		String typeName = classTree.simpleName().name();
		TokenPosition position = getPosition(classTree.simpleName().identifierToken());
		TokenPosition scope = getPosition(classTree.firstToken(), classTree.lastToken());
		List<Modifier> modifiers = getModifiers(classTree.modifiers());
		TypeDef.Kind kind;
		switch (classTree.kind()) {
		case CLASS: 
			kind = Kind.CLASS;
			break;
		case INTERFACE: 
			kind = Kind.INTERFACE;
			break;
		case ANNOTATION_TYPE:
			kind = Kind.ANNOTATION;
			break;
		case ENUM:
			kind = Kind.ENUM;
			break;
		default:
			throw new ExtractException("Unexpected kind: " + classTree.kind());
		}
		List<String> superClassNames = new ArrayList<>();
		TypeTree typeTree = classTree.superClass();
		if (typeTree != null)
			superClassNames.add(getSimpleTypeName(typeTree));
		for (TypeTree each: classTree.superInterfaces()) {
			superClassNames.add(getSimpleTypeName(each));
		}
		
		String typeParameters = describeTypeParameters(classTree.typeParameters());
		TypeDef typeDef = new TypeDef(parent, typeName, position, scope, kind, typeParameters, modifiers, 
				superClassNames);
		symbols.add(typeDef);
		
		for (Tree tree: classTree.members()) {
			if (tree instanceof EmptyStatementTree || tree instanceof BlockTree) {
				continue;
			} else if (tree instanceof ClassTree) {
				extractFromClassTree((ClassTree) tree, typeDef, symbols);
			} else if (tree instanceof EnumConstantTree) {
				extractFromEnumConstantTree((EnumConstantTree) tree, typeDef, symbols);
			} else if (tree instanceof VariableTree) {
				extractFromVariableTree((VariableTree) tree, typeDef, symbols);
			} else if (tree instanceof MethodTree) {
				extractFromMethodTree((MethodTree) tree, typeDef, symbols);
			} else {
				throw new ExtractException("Unexpected member type: " + tree.getClass());
			}
		}
	}
	
	private void extractFromVariableTree(VariableTree variableTree, TypeDef parent, List<JavaSymbol> symbols) {
		String fieldName = variableTree.simpleName().name();
		TokenPosition position = getPosition(variableTree.simpleName().identifierToken());
		TokenPosition scope = getPosition(variableTree.firstToken(), variableTree.lastToken());
		List<Modifier> modifiers = getModifiers(variableTree.modifiers());
		String typeInfo = describeTypeTree(variableTree.type());
		FieldDef fieldDef = new FieldDef(parent, fieldName, position, scope, typeInfo, modifiers);
		symbols.add(fieldDef);
	}
	
	private void extractFromMethodTree(MethodTree methodTree, TypeDef parent, List<JavaSymbol> symbols) {
		String methodName = methodTree.simpleName().name();
		TokenPosition position = getPosition(methodTree.simpleName().identifierToken());
		TokenPosition scope = getPosition(methodTree.firstToken(), methodTree.lastToken());
		List<Modifier> modifiers = getModifiers(methodTree.modifiers());
		String typeInfo = describeTypeTree(methodTree.returnType());
		String methodParamInfo = describeMethodParameters(methodTree.parameters());
		String typeParamInfo = describeTypeParameters(methodTree.typeParameters());
		MethodDef methodDef = new MethodDef(parent, methodName, position, scope, typeInfo, methodParamInfo, 
				typeParamInfo, modifiers);
		symbols.add(methodDef);
	}
	
	private @Nullable String describeTypeParameters(TypeParameters typeParameters) {
		if (typeParameters == null || typeParameters.isEmpty()) {
			return null;
		} else {
			List<String> list = new ArrayList<>();
			for (TypeParameterTree typeParameterTree: typeParameters) {
				if (typeParameterTree.extendToken() == null) {
					list.add(typeParameterTree.identifier().name());
				} else {
					List<String> bounds = new ArrayList<>();
					for (Tree boundTree: typeParameterTree.bounds()) {
						if (boundTree instanceof IdentifierTree)
							bounds.add(((IdentifierTree)boundTree).name());
						else if (boundTree instanceof TypeTree) 
							bounds.add(describeTypeTree((TypeTree) boundTree));
						else
							throw new ExtractException("Unexpected bound tree type: " + boundTree.getClass());
					}
					list.add(typeParameterTree.identifier().name() + " extends " + Joiner.on("&").join(bounds));
				}
			}
			return "<" + Joiner.on(", ").join(list) + ">";
		}
	}
	
	private @Nullable String describeTypeTree(TypeTree typeTree) {
		if (typeTree instanceof IdentifierTree || typeTree instanceof MemberSelectExpressionTree) {
			return getFullQualifiedName((ExpressionTree) typeTree);
		} else if (typeTree instanceof ParameterizedTypeTree) {
			ParameterizedTypeTree parameterizedTypeTree = (ParameterizedTypeTree) typeTree;
			List<String> typeArguments = new ArrayList<>();
			for (Tree typeArgument: parameterizedTypeTree.typeArguments()) {
				if (typeArgument instanceof TypeTree) {
					typeArguments.add(describeTypeTree((TypeTree) typeArgument));
				} else if (typeArgument instanceof WildcardTree) {
					WildcardTree wildcardTree = (WildcardTree) typeArgument;
					if (wildcardTree.kind() == Tree.Kind.UNBOUNDED_WILDCARD) {
						typeArguments.add("?");
					} else if (wildcardTree.kind() == Tree.Kind.EXTENDS_WILDCARD) {
						typeArguments.add("? extends " + describeTypeTree(wildcardTree.bound()));
					} else if (wildcardTree.kind() == Tree.Kind.SUPER_WILDCARD) {
						typeArguments.add("? super " + describeTypeTree(wildcardTree.bound()));
					}
				}
			}
			String unparameterizedTypeInfo = describeTypeTree(parameterizedTypeTree.type());
			return unparameterizedTypeInfo + "<" + Joiner.on(", ").join(typeArguments) + ">";
		} else if (typeTree instanceof ArrayTypeTree) {
			ArrayTypeTree arrayTypeTree = (ArrayTypeTree) typeTree;
			if (arrayTypeTree.ellipsisToken() != null) {
				return describeTypeTree(arrayTypeTree.type()) + "...";
			} else {
				return describeTypeTree(arrayTypeTree.type()) + "[]";
			}
		} else if (typeTree instanceof PrimitiveTypeTree) {
			PrimitiveTypeTree primitiveTypeTree = (PrimitiveTypeTree) typeTree;
			return primitiveTypeTree.keyword().text();
		} else if (typeTree == null) {
			return null;
		} else {
			throw new ExtractException("Unexpected type tree: " + typeTree.getClass());
		}
	}
	
	private @Nullable String describeMethodParameters(List<VariableTree> methodParameters) {
		if (methodParameters.isEmpty()) {
			return null;
		} else {
			List<String> params = new ArrayList<>();
			for (VariableTree variableTree: methodParameters) {
				params.add(describeTypeTree(variableTree.type()));
			}
			return Joiner.on(", ").join(params);
		}
	}
	
	private void extractFromEnumConstantTree(EnumConstantTree enumConstantTree, TypeDef parent, 
			List<JavaSymbol> symbols) {
		List<Modifier> modifiers = getModifiers(enumConstantTree.modifiers());
		String fieldName = enumConstantTree.simpleName().name();
		TokenPosition position = getPosition(enumConstantTree.simpleName().identifierToken());
		TokenPosition scope = getPosition(enumConstantTree.firstToken(), enumConstantTree.lastToken());
		FieldDef fieldDef = new FieldDef(parent, fieldName, position, scope, null, modifiers);
		symbols.add(fieldDef);
	}
	
	private String getSimpleTypeName(TypeTree typeTree) {
		if (typeTree instanceof IdentifierTree) {
			IdentifierTree identifierTree = (IdentifierTree) typeTree;
			return identifierTree.name();
		} else if (typeTree instanceof MemberSelectExpressionTree) {
			MemberSelectExpressionTree memberSelectExpressionTree = (MemberSelectExpressionTree) typeTree;
			return memberSelectExpressionTree.identifier().name();
		} else if (typeTree instanceof ParameterizedTypeTree) {
			ParameterizedTypeTree parameterizedTypeTree = (ParameterizedTypeTree) typeTree;
			return getSimpleTypeName(parameterizedTypeTree.type());
		} else {
			throw new ExtractException("Unexpected type tree: " + typeTree.getClass());
		}
	}
	
	private List<Modifier> getModifiers(ModifiersTree modifiersTree) {
		List<Modifier> modifiers = new ArrayList<>();
		for (ModifierTree modifierTree: modifiersTree) {
			if (modifierTree instanceof ModifierKeywordTree) {
				ModifierKeywordTree modifierKeywordTree = (ModifierKeywordTree) modifierTree;
				modifiers.add(modifierKeywordTree.modifier());
			}
		}
		return modifiers;
	}
	
	private String getFullQualifiedName(ExpressionTree expression) {
		if (expression instanceof IdentifierTree) {
			IdentifierTree identifier = (IdentifierTree) expression;
			return identifier.name();
		} else if (expression instanceof MemberSelectExpressionTree) {
			MemberSelectExpressionTree memberSelectExpression = (MemberSelectExpressionTree) expression;
			return getFullQualifiedName(memberSelectExpression.expression()) + 
					"." + memberSelectExpression.identifier().name();
		} else {
			throw new ExtractException("Unexpected expression type: " + expression.getClass());
		}
	}
	
	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "java");
	}
	
}

