package com.gitplex.symbolextractor.javascript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;
import org.sonar.javascript.parser.JavaScriptParser;
import org.sonar.plugins.javascript.api.tree.ScriptTree;
import org.sonar.plugins.javascript.api.tree.Tree;
import org.sonar.plugins.javascript.api.tree.Tree.Kind;
import org.sonar.plugins.javascript.api.tree.declaration.AccessorMethodDeclarationTree;
import org.sonar.plugins.javascript.api.tree.declaration.BindingElementTree;
import org.sonar.plugins.javascript.api.tree.declaration.ExportClauseTree;
import org.sonar.plugins.javascript.api.tree.declaration.ExportDefaultBinding;
import org.sonar.plugins.javascript.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.javascript.api.tree.declaration.FunctionTree;
import org.sonar.plugins.javascript.api.tree.declaration.InitializedBindingElementTree;
import org.sonar.plugins.javascript.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.javascript.api.tree.declaration.NamedExportDeclarationTree;
import org.sonar.plugins.javascript.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.javascript.api.tree.declaration.SpecifierTree;
import org.sonar.plugins.javascript.api.tree.expression.ArrowFunctionTree;
import org.sonar.plugins.javascript.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.CallExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.ClassTree;
import org.sonar.plugins.javascript.api.tree.expression.DotMemberExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.ExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.IdentifierTree;
import org.sonar.plugins.javascript.api.tree.expression.LiteralTree;
import org.sonar.plugins.javascript.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.ObjectLiteralTree;
import org.sonar.plugins.javascript.api.tree.expression.PairPropertyTree;
import org.sonar.plugins.javascript.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.javascript.api.tree.statement.BlockTree;
import org.sonar.plugins.javascript.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.javascript.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.javascript.api.tree.statement.StatementTree;
import org.sonar.plugins.javascript.api.tree.statement.VariableDeclarationTree;
import org.sonar.plugins.javascript.api.tree.statement.VariableStatementTree;

import com.gitplex.symbolextractor.AbstractSymbolExtractor;
import com.gitplex.symbolextractor.ExtractException;
import com.gitplex.symbolextractor.javascript.symbols.ClassSymbol;
import com.gitplex.symbolextractor.javascript.symbols.FunctionSymbol;
import com.gitplex.symbolextractor.javascript.symbols.JavaScriptSymbol;
import com.gitplex.symbolextractor.javascript.symbols.MethodAccessorType;
import com.gitplex.symbolextractor.javascript.symbols.MethodSymbol;
import com.gitplex.symbolextractor.javascript.symbols.PropertySymbol;
import com.gitplex.symbolextractor.javascript.symbols.ReferenceSymbol;
import com.gitplex.symbolextractor.javascript.symbols.VariableSymbol;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.RecognitionException;

public class JavaScriptExtractor extends AbstractSymbolExtractor<JavaScriptSymbol> {

	@Override
	public List<JavaScriptSymbol> extract(String source) throws ExtractException {
		List<JavaScriptSymbol> symbols = new ArrayList<>();
		JavaScriptParser parser = new JavaScriptParser(Charsets.UTF_8);
		try {
			collect(parser.parse(source), null, symbols);
		} catch (RecognitionException e) {
			throw new ExtractException("Error parsing javascript", e);
		}
		Map<JavaScriptSymbol, Set<String>> containedDeclarations = new HashMap<>();
		for (JavaScriptSymbol symbol: symbols) {
			if (!(symbol instanceof ReferenceSymbol) && symbol.getIndexName() != null) {
				JavaScriptSymbol parent = (JavaScriptSymbol) symbol.getParent();
				if (parent != null) {
					Set<String> containedDeclarationsOfParent = containedDeclarations.get(parent);
					if (containedDeclarationsOfParent == null) {
						containedDeclarationsOfParent = new HashSet<>();
						containedDeclarations.put(parent, containedDeclarationsOfParent);
					}
					containedDeclarationsOfParent.add(symbol.getIndexName());
				}
			}
		}

		/*
		 * Remove all non-global variables, functions and classes. Method and property 
		 * symbols are attached to its parent symbols (classes and object literals), 
		 * and we will remove them later if their parents are removed. Referenced 
		 * symbols might be defining properties of global objects, and will not be 
		 * processed here 
		 */
		for (Iterator<JavaScriptSymbol> it = symbols.iterator(); it.hasNext();) {
			JavaScriptSymbol symbol = it.next();
			if (!symbol.isExported() && symbol.getParent() != null 
					&& (symbol instanceof ClassSymbol 
							|| symbol instanceof FunctionSymbol 
							|| symbol instanceof VariableSymbol)) {
				it.remove();
			}
		}
		
		/*
		 * Now we check if root object of reference symbols are local symbols. If yes, 
		 * we will remove them 
		 */
		for (Iterator<JavaScriptSymbol> it = symbols.iterator(); it.hasNext();) {
			JavaScriptSymbol symbol = it.next();
			if (!symbol.isExported() && symbol instanceof ReferenceSymbol) {
				ReferenceSymbol referenceSymbol = (ReferenceSymbol) symbol;
				String rootObject = referenceSymbol.getRootObject();
				JavaScriptSymbol parent = (JavaScriptSymbol) symbol.getParent();
				while (parent != null) {
					Set<String> containedDeclarationsOfParent = containedDeclarations.get(parent);
					if (containedDeclarationsOfParent != null && containedDeclarationsOfParent.contains(rootObject)) {
						it.remove();
						break;
					}
					parent = (JavaScriptSymbol) parent.getParent();
				}
			}
		}

		/*
		 * Now we continue to remove pseudo symbols without properties. Pseudo symbols 
		 * do not have index name, and are created to hold indexable properties, for 
		 * instance we will create a pseudo symbol for return statement of function to 
		 * hold returned object literals 
		 */
		Set<JavaScriptSymbol> hasChildren = new HashSet<>();
		for (JavaScriptSymbol symbol: symbols) {
			JavaScriptSymbol parent = (JavaScriptSymbol) symbol.getParent();
			if (parent != null)
				hasChildren.add(parent);
		}
		for (Iterator<JavaScriptSymbol> it = symbols.iterator(); it.hasNext();) {
			JavaScriptSymbol symbol = it.next();
			if (symbol instanceof ReferenceSymbol && symbol.getIndexName() == null && !hasChildren.contains(symbol))
				it.remove();
		}
		
		/*
		 * For remaining reference symbols, we add back their parents (local classes, 
		 * functions or variables) if they've been removed in the first stage to make
		 * the outline understandable 
		 */
		List<JavaScriptSymbol> addSymbols = new ArrayList<>();
		Set<JavaScriptSymbol> symbolSet = new HashSet<>(symbols);
		for (JavaScriptSymbol symbol: symbols) {
			if (symbol instanceof ReferenceSymbol) {
				JavaScriptSymbol parent = (JavaScriptSymbol) symbol.getParent();
				while (parent != null) {
					if (!symbolSet.contains(parent)) {
						symbolSet.add(parent);
						addSymbols.add(parent);
					}
					parent = (JavaScriptSymbol) parent.getParent();
				}
			}
		}
		symbols.addAll(addSymbols);
		
		/*
		 * The final step is to remove those non-reference symbols whose parents 
		 * have been removed 
		 */
		List<JavaScriptSymbol> removeSymbols = new ArrayList<>();
		for (JavaScriptSymbol symbol: symbols) {
			if (!(symbol instanceof ReferenceSymbol)) {
				JavaScriptSymbol parent = (JavaScriptSymbol) symbol.getParent();
				while (parent != null) {
					if (!symbolSet.contains(parent)) {
						removeSymbols.add(symbol);
						break;
					}
					parent = (JavaScriptSymbol) parent.getParent();
				}
			}
		}
		symbols.removeAll(removeSymbols);
		
		return symbols;
	}
	
	private void collect(Tree tree, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
		if (tree instanceof ScriptTree) {
			ScriptTree script = (ScriptTree) tree;
			if (script.items() != null && script.items().items() != null) {
				for (Tree item: script.items().items()) {
					collect(item, parent, symbols);
				}
			}
		} else if (tree instanceof StatementTree) {
			collect((StatementTree)tree, parent, symbols);
		} else if (tree instanceof NamedExportDeclarationTree) {
			collect((NamedExportDeclarationTree)tree, parent, symbols);
		} 
	}
	
	private void collect(Iterable<MethodDeclarationTree> methodDeclarations, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
		for (MethodDeclarationTree methodDeclaration: methodDeclarations) {
			Tree name = methodDeclaration.name();
			SyntaxToken nameToken = getNameToken(name);
			if (nameToken != null) {
				MethodAccessorType accessorType = MethodAccessorType.NORMAL;
				if (methodDeclaration instanceof AccessorMethodDeclarationTree) {
					AccessorMethodDeclarationTree accessorMethodDeclaration = (AccessorMethodDeclarationTree) methodDeclaration;
					if (accessorMethodDeclaration.accessorToken().text().equals("get"))
						accessorType = MethodAccessorType.GET;
					else
						accessorType = MethodAccessorType.SET;
				}
				MethodSymbol methodSymbol = new MethodSymbol(parent, nameToken, 
						accessorType, describeParameters(methodDeclaration.parameterClause()));
				symbols.add(methodSymbol);
				collect(methodDeclaration.body(), methodSymbol, symbols);
			}
		}
	}
	
	private String describeParameters(Tree parameters) {
		StringBuilder builder = new StringBuilder("(");
		if (parameters instanceof ParameterListTree) {
			ParameterListTree parameterList = (ParameterListTree) parameters;
			for (int i=0; i<parameterList.parameters().size(); i++) {
				builder.append(parameterList.parameters().get(i));
				if (i != parameterList.parameters().size()-1)
					builder.append(", ");
			}
		} else if (parameters instanceof IdentifierTree) {
			IdentifierTree identifier = (IdentifierTree) parameters;
			builder.append(identifier.name());
		}
		builder.append(")");
		return builder.toString();
	}
	
	/*
	 * process ES6 export statements
	 */
	private void collect(NamedExportDeclarationTree namedExportDeclaration, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
		Tree object = namedExportDeclaration.object();
		if (object instanceof ExportDefaultBinding) {
			ExportDefaultBinding exportDefaultBinding = (ExportDefaultBinding) object;
			IdentifierTree identifier = exportDefaultBinding.exportedDefaultIdentifier();
			if (!identifier.name().equals("default")) {
				symbols.add(new VariableSymbol(parent, identifier.identifierToken(), true));
			}
		} if (object instanceof FunctionDeclarationTree) {
			collect((FunctionDeclarationTree)object, parent, symbols, true);
		} else if (object instanceof VariableStatementTree) {
			collect(((VariableStatementTree)object).declaration(), parent, symbols, true);
		} else if (object instanceof ExportClauseTree) {
			ExportClauseTree exportClause = (ExportClauseTree) object;
			for (SpecifierTree specifier: exportClause.exports().specifiers()) {
				if (specifier.localName() != null) {
					VariableSymbol symbol = new VariableSymbol(parent, specifier.localName().identifierToken(), true);
					symbols.add(symbol);
				} else if (specifier.name() instanceof IdentifierTree) {
					VariableSymbol symbol = new VariableSymbol(parent, ((IdentifierTree)specifier.name()).identifierToken(), true);
					symbols.add(symbol);
				}
			}
		} else if (object instanceof ClassTree) {
			ClassTree classTree = (ClassTree)object;
			parent = new ClassSymbol(parent, classTree.name()!=null?classTree.name().identifierToken():null, true);
			symbols.add(parent);
			collect(classTree.methods(), parent, symbols);
		}
	}
	
	private void collect(FunctionDeclarationTree functionDeclaration, JavaScriptSymbol parent, 
			List<JavaScriptSymbol> symbols, boolean exported) {
		FunctionSymbol symbol = new FunctionSymbol(parent, functionDeclaration.name().identifierToken(), 
				describeParameters(functionDeclaration.parameterClause()), exported);
		symbols.add(symbol);
		collect(functionDeclaration.body(), symbol, symbols);
	}
	
	private void collect(BlockTree body, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
		for (StatementTree statement: body.statements()) {
			collect(statement, parent, symbols);
		}
	}
	
	private void collect(StatementTree statement, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
		if (statement instanceof FunctionDeclarationTree) {
			collect((FunctionDeclarationTree)statement, parent, symbols, false);
		} else if (statement instanceof VariableStatementTree) {
			collect(((VariableStatementTree)statement).declaration(), parent, symbols, false);
		} else if (statement instanceof ExpressionStatementTree) {
			Tree expression = ((ExpressionStatementTree)statement).expression();
			if (expression instanceof ExpressionTree) {
				collect((ExpressionTree) expression, parent, symbols, new ArrayList<>(), false);
			}
		} else if (statement instanceof BlockTree) {
			collect((BlockTree)statement, parent, symbols);
		} else if (statement instanceof ClassTree) {
			ClassTree classTree = (ClassTree) statement;
			parent = new ClassSymbol(parent, classTree.name()!=null?classTree.name().identifierToken():null, false);
			symbols.add(parent);
			collect(classTree.methods(), parent, symbols);
		} else if (statement instanceof ReturnStatementTree) {
			/*
			 * create a pseudo return reference symbol to hold properties 
			 * discovered in return statement. These properties might be 
			 * referenced by other files, so we need to index them
			 */
			ReturnStatementTree returnStatement = (ReturnStatementTree) statement;
			SyntaxToken token = returnStatement.returnKeyword();
			ReferenceSymbol referenceSymbol = new ReferenceSymbol(parent, null, 
					JavaScriptSymbol.getPosition(token), "return", null, false);
			symbols.add(referenceSymbol);
			collect(returnStatement.expression(), parent, symbols, 
					Lists.newArrayList(referenceSymbol), false);
		}
	}
	
	private void collect(VariableDeclarationTree variableDeclaration, JavaScriptSymbol parent, 
			List<JavaScriptSymbol> symbols, boolean exported) {
		for (BindingElementTree bindingElement: variableDeclaration.variables()) {
			collect(bindingElement, parent, symbols, exported);
		}
	}
	
	/*
	 * BindingElementTree represents variable binding such as "var a" or "var [a,b]"
	 */
	private void collect(BindingElementTree bindingElement, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols, boolean exported) {
		List<JavaScriptSymbol> assignedSymbols = new ArrayList<>();
		for (IdentifierTree identifier: bindingElement.bindingIdentifiers()) {
			VariableSymbol symbol = new VariableSymbol(parent, identifier.identifierToken(), exported);
			assignedSymbols.add(symbol);
		}
		symbols.addAll(assignedSymbols);

		/*
		 *  variable is initialized, so we continue to check if there is something defining
		 *  properties of the variables worthing to be indexed
		 */
		if (bindingElement instanceof InitializedBindingElementTree) {
			InitializedBindingElementTree initializedBindingElement = (InitializedBindingElementTree) bindingElement;
			collect((ExpressionTree)initializedBindingElement.right(), parent, symbols, assignedSymbols, exported);
		}
	}
	
	private void collect(ExpressionTree expression, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols, 
			List<JavaScriptSymbol> assignedSymbols, boolean exported) {
		if (expression instanceof AssignmentExpressionTree) {
			collect((AssignmentExpressionTree)expression, parent, symbols, assignedSymbols, exported);
		} else if (expression instanceof ObjectLiteralTree) {
			collect((ObjectLiteralTree)expression, symbols, parent, assignedSymbols);
		} else if (expression instanceof ParenthesisedExpressionTree) {
			ParenthesisedExpressionTree parenthesisedExpression = (ParenthesisedExpressionTree) expression;
			collect(parenthesisedExpression.expression(), parent, symbols, assignedSymbols, exported);
		} else if (expression instanceof NewExpressionTree) { // new SomeClass(...)
			NewExpressionTree newExpression = (NewExpressionTree) expression;
			collect(newExpression.expression(), parent, symbols, Lists.newArrayList(), false);
			if (newExpression.arguments() != null) {
				for (Tree parameter: newExpression.arguments().parameters()) {
					if (parameter instanceof ExpressionTree) {
						collect((ExpressionTree)parameter, parent, symbols, Lists.newArrayList(), false);
					} 
				}
			}
		} else if (expression instanceof CallExpressionTree) { // call a function
			CallExpressionTree callExpression = (CallExpressionTree) expression;
			boolean processed = false;
			
			// CommonJS require statement
			if (!processed && callExpression.callee() instanceof IdentifierTree) {  
				IdentifierTree callingFunction = (IdentifierTree) callExpression.callee();
				if (callingFunction.name().equals("require")) {
					symbols.removeAll(assignedSymbols);
					processed = true;
				} 
			} 
			
			// Vue.js component registration
			if (!processed 
					&& callExpression.callee() instanceof DotMemberExpressionTree 
					&& StringUtils.deleteWhitespace(callExpression.callee().toString()).equals("Vue.component") 
					&& callExpression.arguments().parameters().size()>=2
					&& callExpression.arguments().parameters().get(0) instanceof LiteralTree
					&& ((LiteralTree) callExpression.arguments().parameters().get(0)).is(Kind.STRING_LITERAL)) {
				LiteralTree vueComponent = (LiteralTree) callExpression.arguments().parameters().get(0);
				ReferenceSymbol componentSymbol = new ReferenceSymbol(parent, vueComponent.token(), null, true);
				symbols.add(componentSymbol);
				assignedSymbols.add(componentSymbol);
				
				if (callExpression.arguments().parameters().get(1) instanceof ExpressionTree) {
					collect((ExpressionTree)callExpression.arguments().parameters().get(1), 
							parent, symbols, assignedSymbols, false);
				}
				processed = true;
			} 
			if (!processed 
					&& callExpression.callee() instanceof DotMemberExpressionTree 
					&& StringUtils.deleteWhitespace(callExpression.callee().toString()).equals("Vue.extend") 
					&& !callExpression.arguments().parameters().isEmpty()) {
				for (JavaScriptSymbol assignedSymbol: assignedSymbols)
					assignedSymbol.setExported(true);
				if (callExpression.arguments().parameters().get(0) instanceof ExpressionTree) {
					collect((ExpressionTree)callExpression.arguments().parameters().get(0), 
							parent, symbols, assignedSymbols, false);
				}
				processed = true;
			} 
			
			if (!processed) {
				collect(callExpression.callee(), parent, symbols, Lists.newArrayList(), false);
				for (Tree parameter: callExpression.arguments().parameters()) {
					if (parameter instanceof ExpressionTree) {
						collect((ExpressionTree)parameter, parent, symbols, Lists.newArrayList(), false);
					} 
				}
			}
		} else if (expression instanceof FunctionTree) { // an inline function declaration
			if (!assignedSymbols.isEmpty()) {
				for (JavaScriptSymbol assigned: assignedSymbols) {
					collect(((FunctionTree)expression).body(), assigned, symbols);
				}
			} else {
				if (expression instanceof FunctionExpressionTree) {
					FunctionExpressionTree functionExpression = (FunctionExpressionTree) expression;
					IdentifierTree identifier = functionExpression.name();
					SyntaxToken nameToken = identifier!=null?identifier.identifierToken():null;
					parent = new FunctionSymbol(parent, nameToken, 
							describeParameters(functionExpression.parameterClause()), false);
					symbols.add(parent);
				} else if (expression instanceof ArrowFunctionTree) {
					ArrowFunctionTree arrowFunction = (ArrowFunctionTree) expression;
					parent = new FunctionSymbol(parent, null, 
							describeParameters(arrowFunction.parameterClause()), false);
					symbols.add(parent);
				}
				collect(((FunctionTree)expression).body(), parent, symbols);
			}
		} else if (expression instanceof ClassTree) {
			ClassTree classTree = (ClassTree) expression;
			if (!assignedSymbols.isEmpty()) {
				for (JavaScriptSymbol assigned: assignedSymbols) {
					collect(classTree.methods(), assigned, symbols);
				}
			} else {
				parent = new ClassSymbol(parent, classTree.name()!=null?classTree.name().identifierToken():null, false);
				symbols.add(parent);
				collect(classTree.methods(), parent, symbols);
			}
		} else if (expression instanceof IdentifierTree || expression instanceof DotMemberExpressionTree) {
			/*
			 * capture CommonJS exported symbols of below form:
			 * exports = someVar;
			 * module.exports = someVar;  
			 */
			boolean exports = false;
			for (JavaScriptSymbol assignedSymbol: assignedSymbols) {
				if (assignedSymbol instanceof ReferenceSymbol) {
					ReferenceSymbol referenceSymbol = (ReferenceSymbol) assignedSymbol;
					if (referenceSymbol.getIndexName() != null) {
						if (referenceSymbol.getObject()!=null && (referenceSymbol.getObject() + "." + referenceSymbol.getIndexName()).equals("module.exports")
								|| referenceSymbol.getObject()==null && referenceSymbol.getIndexName().equals("exports")) {
							symbols.remove(referenceSymbol);
							exports = true;
						}
					}
				}
			}
			if (exports) {
				ReferenceSymbol referenceSymbol;
				if (expression instanceof IdentifierTree) {
					IdentifierTree identifier = (IdentifierTree) expression;
					referenceSymbol = new ReferenceSymbol(parent, identifier.identifierToken(), null, true);
				} else {
					DotMemberExpressionTree dotMemberExpression = (DotMemberExpressionTree) expression;
					referenceSymbol = new ReferenceSymbol(parent, dotMemberExpression.property().identifierToken(), 
							StringUtils.deleteWhitespace(dotMemberExpression.object().toString()), true);
				}
				symbols.add(referenceSymbol);
			}
		}
	}

	@Nullable
	private SyntaxToken getNameToken(Tree nameTree) {
		if (nameTree instanceof IdentifierTree) {
			IdentifierTree identifier = (IdentifierTree) nameTree;
			return identifier.identifierToken();
		} else if (nameTree instanceof LiteralTree) {
			LiteralTree literal = (LiteralTree) nameTree;
			if (literal.is(Kind.STRING_LITERAL)) {
				return literal.token();
			}
		}
		return null;
	}
	
	private void collect(ObjectLiteralTree objectLiteral, List<JavaScriptSymbol> symbols, JavaScriptSymbol parent, 
			List<JavaScriptSymbol> assignedSymbols) {
		List<JavaScriptSymbol> associatedSymbols = new ArrayList<>(assignedSymbols);
		if (associatedSymbols.isEmpty())
			associatedSymbols.add(parent);
		for (JavaScriptSymbol associatedSymbol: associatedSymbols) {
			for (Tree property: objectLiteral.properties()) {
				if (property instanceof PairPropertyTree) {
					PairPropertyTree pairProperty = (PairPropertyTree) property;
					SyntaxToken nameToken = getNameToken(pairProperty.key());
					if (nameToken != null) {
						PropertySymbol propertySymbol = new PropertySymbol(associatedSymbol, nameToken);
						symbols.add(propertySymbol);
						collect(pairProperty.value(), parent, symbols, Lists.newArrayList(propertySymbol), false);
					}
				} else if (property instanceof MethodDeclarationTree) {
					MethodAccessorType accessorType = MethodAccessorType.NORMAL;
					if (property instanceof AccessorMethodDeclarationTree) {
						AccessorMethodDeclarationTree accessorMethodDeclaration = (AccessorMethodDeclarationTree) property;
						if (accessorMethodDeclaration.accessorToken().text().equals("get"))
							accessorType = MethodAccessorType.GET;
						else
							accessorType = MethodAccessorType.SET;
					}
					MethodDeclarationTree methodDeclaration = (MethodDeclarationTree) property;
					Tree name = methodDeclaration.name();
					SyntaxToken nameToken = getNameToken(name);
					if (nameToken != null) {
						MethodSymbol methodSymbol = new MethodSymbol(associatedSymbol, nameToken, 
								accessorType, describeParameters(methodDeclaration.parameterClause()));
						symbols.add(methodSymbol);
						collect(methodDeclaration.body(), methodSymbol, symbols);
					}
				}
			}
		}
	}
	
	private void collect(AssignmentExpressionTree assignmentExpression, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols, 
			List<JavaScriptSymbol> assignedSymbols, boolean exported) {
		if (assignmentExpression.is(Kind.ASSIGNMENT)) {
			ExpressionTree variable = assignmentExpression.variable();
			if (variable instanceof IdentifierTree) {
				IdentifierTree identifier = (IdentifierTree) assignmentExpression.variable();
				ReferenceSymbol symbol = new ReferenceSymbol(parent, identifier.identifierToken(), null, false);
				symbols.add(symbol);
				assignedSymbols.add(symbol);
				collect(assignmentExpression.expression(), parent, symbols, assignedSymbols, exported);
			} else if (variable instanceof DotMemberExpressionTree) {
				DotMemberExpressionTree dotMemberExpression = (DotMemberExpressionTree) variable;
				ReferenceSymbol symbol = new ReferenceSymbol(parent, dotMemberExpression.property().identifierToken(), 
						StringUtils.deleteWhitespace(dotMemberExpression.object().toString()), false);
				symbols.add(symbol);
				assignedSymbols.add(symbol);
				collect(assignmentExpression.expression(), parent, symbols, assignedSymbols, exported);
			}
		}
	}
	
	@Override
	public int getVersion() {
		return 3;
	}

	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "js", "jsx");
	}
	
}