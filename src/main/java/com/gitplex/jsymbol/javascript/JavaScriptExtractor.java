package com.gitplex.jsymbol.javascript;

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
import org.sonar.plugins.javascript.api.tree.declaration.ImportClauseTree;
import org.sonar.plugins.javascript.api.tree.declaration.ImportDeclarationTree;
import org.sonar.plugins.javascript.api.tree.declaration.InitializedBindingElementTree;
import org.sonar.plugins.javascript.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.javascript.api.tree.declaration.NamedExportDeclarationTree;
import org.sonar.plugins.javascript.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.javascript.api.tree.declaration.SpecifierListTree;
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
import org.sonar.plugins.javascript.api.tree.statement.StatementTree;
import org.sonar.plugins.javascript.api.tree.statement.VariableDeclarationTree;
import org.sonar.plugins.javascript.api.tree.statement.VariableStatementTree;

import com.gitplex.jsymbol.AbstractSymbolExtractor;
import com.gitplex.jsymbol.ExtractException;
import com.gitplex.jsymbol.TokenPosition;
import com.gitplex.jsymbol.javascript.symbols.ClassSymbol;
import com.gitplex.jsymbol.javascript.symbols.FunctionSymbol;
import com.gitplex.jsymbol.javascript.symbols.JavaScriptSymbol;
import com.gitplex.jsymbol.javascript.symbols.MethodAccessorType;
import com.gitplex.jsymbol.javascript.symbols.MethodSymbol;
import com.gitplex.jsymbol.javascript.symbols.PropertySymbol;
import com.gitplex.jsymbol.javascript.symbols.ReferenceSymbol;
import com.gitplex.jsymbol.javascript.symbols.VariableSymbol;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.RecognitionException;

public class JavaScriptExtractor extends AbstractSymbolExtractor<JavaScriptSymbol> {

	@Override
	public List<JavaScriptSymbol> extract(String source) throws ExtractException {
		List<JavaScriptSymbol> symbols = new ArrayList<>();
		try {
			processTree(new JavaScriptParser(Charsets.UTF_8).parse(source), null, symbols);
		} catch (RecognitionException e) {
			throw new ExtractException("Error parsing javascript", e);
		}
		Map<JavaScriptSymbol, Set<String>> containedDeclarations = new HashMap<>();
		for (JavaScriptSymbol symbol: symbols) {
			if (!(symbol instanceof ReferenceSymbol) && symbol.getName() != null) {
				JavaScriptSymbol parent = (JavaScriptSymbol) symbol.getParent();
				if (parent != null) {
					Set<String> containedDeclarationsOfParent = containedDeclarations.get(parent);
					if (containedDeclarationsOfParent == null) {
						containedDeclarationsOfParent = new HashSet<>();
						containedDeclarations.put(parent, containedDeclarationsOfParent);
					}
					containedDeclarationsOfParent.add(symbol.getName());
				}
			}
			if (symbol.isExported())
				symbol.setParent(null);
		}

		/*
		 * Remove local variables to clean up outline
		 */
		for (Iterator<JavaScriptSymbol> it = symbols.iterator(); it.hasNext();) {
			JavaScriptSymbol symbol = it.next();
			if (!symbol.isExported() && symbol.getParent() != null && symbol instanceof VariableSymbol) {
				it.remove();
			}
		}
		
		/*
		 * Remove local reference symbols to further clean up outline
		 */
		for (Iterator<JavaScriptSymbol> it = symbols.iterator(); it.hasNext();) {
			JavaScriptSymbol symbol = it.next();
			if (symbol instanceof ReferenceSymbol) {
				ReferenceSymbol referenceSymbol = (ReferenceSymbol) symbol;
				String rootObject = referenceSymbol.getRootObject();
				boolean local = false;
				JavaScriptSymbol parent = (JavaScriptSymbol) symbol.getParent();
				while (parent != null) {
					Set<String> containedDeclarationsOfParent = containedDeclarations.get(parent);
					if (containedDeclarationsOfParent != null && containedDeclarationsOfParent.contains(rootObject)) {
						local = true;
						break;
					}
					parent = (JavaScriptSymbol) parent.getParent();
				}
				if (local) {
					symbol.setLocal(true);
					it.remove();
				} else {
					String object = referenceSymbol.getObject();
					if (object == null || !referenceSymbol.getRootObject().equals("this"))
						referenceSymbol.setParent(null);
				}
			}
		}

		/*
		 * For remaining symbols, we add back their parents in case they've been removed 
		 * previously 
		 */
		List<JavaScriptSymbol> addSymbols = new ArrayList<>();
		Set<JavaScriptSymbol> symbolSet = new HashSet<>(symbols);
		for (JavaScriptSymbol symbol: symbols) {
			JavaScriptSymbol parent = (JavaScriptSymbol) symbol.getParent();
			while (parent != null) {
				if (!symbolSet.contains(parent)) {
					symbolSet.add(parent);
					addSymbols.add(parent);
				}
				parent = (JavaScriptSymbol) parent.getParent();
			}
		}
		symbols.addAll(addSymbols);
		
		return symbols;
	}
	
	private void processTree(Tree tree, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
		if (tree instanceof ScriptTree) {
			ScriptTree script = (ScriptTree) tree;
			if (script.items() != null && script.items().items() != null) {
				for (Tree item: script.items().items()) {
					processTree(item, parent, symbols);
				}
			}
		} else if (tree instanceof StatementTree) {
			processStatement((StatementTree)tree, parent, symbols);
		} else if (tree instanceof NamedExportDeclarationTree) {
			processNamedExportDeclaration((NamedExportDeclarationTree)tree, parent, symbols);
		} else if (tree instanceof ImportDeclarationTree) {
            processImportDeclaration((ImportDeclarationTree)tree, parent, symbols);
        }
    }
	
	private void processMethosDeclarations(Iterable<MethodDeclarationTree> methodDeclarations, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
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
				MethodSymbol methodSymbol = new MethodSymbol(parent, getName(nameToken), getPosition(nameToken),
						accessorType, describeParameters(methodDeclaration.parameterClause()));
				symbols.add(methodSymbol);
				processBlock(methodDeclaration.body(), methodSymbol, symbols);
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
	 * process ES6 import statement
	 */
	private void processImportDeclaration(ImportDeclarationTree importDeclaration, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
		if (importDeclaration.importClause() instanceof ImportClauseTree) {
			ImportClauseTree importClause = (ImportClauseTree) importDeclaration.importClause();
			if (importClause.namedImport() instanceof SpecifierListTree) {
				SpecifierListTree specifierList = (SpecifierListTree) importClause.namedImport();
				for (SpecifierTree specifier: specifierList.specifiers()) {
					proessSpecifierTree(specifier, parent, symbols);
				}
			} else if (importClause.namedImport() instanceof SpecifierTree) {
				proessSpecifierTree((SpecifierTree)importClause.namedImport(), parent, symbols);
			}
			IdentifierTree defaultImport = importClause.defaultImport();
			if (defaultImport != null) {
			    SyntaxToken token = defaultImport.identifierToken();
				symbols.add(new VariableSymbol(parent, getName(token), getPosition(token), true, false));
			}
		}
	}

    private void proessSpecifierTree(SpecifierTree specifier, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
	    IdentifierTree identifier = specifier.localName();
        if (identifier != null) {
            SyntaxToken token = identifier.identifierToken();
            symbols.add(new VariableSymbol(parent, getName(token), getPosition(token), true, false));
        } else if (specifier.name() instanceof IdentifierTree) {
            SyntaxToken token = ((IdentifierTree)specifier.name()).identifierToken();
            symbols.add(new VariableSymbol(parent, getName(token), getPosition(token), true, false));
        }
    }

    /*
	 * process ES6 export statements
	 */
	private void processNamedExportDeclaration(NamedExportDeclarationTree namedExportDeclaration, 
			JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
		Tree object = namedExportDeclaration.object();
		if (object instanceof ExportDefaultBinding) {
			ExportDefaultBinding exportDefaultBinding = (ExportDefaultBinding) object;
			IdentifierTree identifier = exportDefaultBinding.exportedDefaultIdentifier();
			if (!identifier.name().equals("default")) {
				symbols.add(new VariableSymbol(null, getName(identifier.identifierToken()),
                        getPosition(identifier.identifierToken()), false, true));
			}
		} if (object instanceof FunctionDeclarationTree) {
			processFunctionDeclaration((FunctionDeclarationTree)object, parent, symbols, true);
		} else if (object instanceof VariableStatementTree) {
			processVariableDeclaration(((VariableStatementTree)object).declaration(), parent, symbols, true);
		} else if (object instanceof ExportClauseTree) {
			ExportClauseTree exportClause = (ExportClauseTree) object;
			for (SpecifierTree specifier: exportClause.exports().specifiers()) {
				if (specifier.localName() != null) {
					VariableSymbol symbol = new VariableSymbol(null, getName(specifier.localName().identifierToken()),
                            getPosition(specifier.localName().identifierToken()), false, true);
					symbols.add(symbol);
				} else if (specifier.name() instanceof IdentifierTree) {
				    SyntaxToken token = ((IdentifierTree)specifier.name()).identifierToken();
					VariableSymbol symbol = new VariableSymbol(parent, getName(token), getPosition(token), false, true);
					symbols.add(symbol);
				}
			}
		} else if (object instanceof ClassTree) {
			ClassTree classTree = (ClassTree)object;
			parent = new ClassSymbol(null, getName(classTree.name()),
                    getPosition(classTree.name(), classTree.classToken()), false, true);
			symbols.add(parent);
			processMethosDeclarations(classTree.methods(), parent, symbols);
		}
	}
	
	private void processFunctionDeclaration(FunctionDeclarationTree functionDeclaration, JavaScriptSymbol parent, 
			List<JavaScriptSymbol> symbols, boolean exported) {
		FunctionSymbol symbol = new FunctionSymbol(exported?null:parent, getName(functionDeclaration.name()),
                getPosition(functionDeclaration.name(), functionDeclaration.functionKeyword()), 
                exported?false:parent!=null, exported, describeParameters(functionDeclaration.parameterClause()));
		symbols.add(symbol);
		processBlock(functionDeclaration.body(), symbol, symbols);
	}
	
	private void processBlock(BlockTree body, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
		for (StatementTree statement: body.statements()) {
			processStatement(statement, parent, symbols);
		}
	}
	
	private void processStatement(StatementTree statement, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
		if (statement instanceof FunctionDeclarationTree) {
			processFunctionDeclaration((FunctionDeclarationTree)statement, parent, symbols, false);
		} else if (statement instanceof VariableStatementTree) {
			processVariableDeclaration(((VariableStatementTree)statement).declaration(), parent, symbols, false);
		} else if (statement instanceof ExpressionStatementTree) {
			Tree expression = ((ExpressionStatementTree)statement).expression();
			if (expression instanceof ExpressionTree) {
				processExpression((ExpressionTree) expression, parent, symbols, new ArrayList<>(), false);
			}
		} else if (statement instanceof BlockTree) {
			processBlock((BlockTree)statement, parent, symbols);
		} else if (statement instanceof ClassTree) {
			ClassTree classTree = (ClassTree) statement;
			parent = new ClassSymbol(parent, getName(classTree.name()),
                    getPosition(classTree.name(), classTree.classToken()), parent!=null, false);
			symbols.add(parent);
			processMethosDeclarations(classTree.methods(), parent, symbols);
		}
	}
	
	private void processVariableDeclaration(VariableDeclarationTree variableDeclaration, JavaScriptSymbol parent, 
			List<JavaScriptSymbol> symbols, boolean exported) {
		for (BindingElementTree bindingElement: variableDeclaration.variables()) {
			processBindingElement(bindingElement, parent, symbols, exported);
		}
	}
	
	/*
	 * BindingElementTree represents variable binding such as "var a" or "var [a,b]"
	 */
	private void processBindingElement(BindingElementTree bindingElement, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols, boolean exported) {
		List<JavaScriptSymbol> assignedSymbols = new ArrayList<>();
		for (IdentifierTree identifier: bindingElement.bindingIdentifiers()) {
			VariableSymbol symbol = new VariableSymbol(exported?null:parent, getName(identifier.identifierToken()),
                    getPosition(identifier.identifierToken()), exported?false:parent!=null, exported);
			assignedSymbols.add(symbol);
		}
		symbols.addAll(assignedSymbols);

		/*
		 *  variable is initialized, so we continue to check if there is something defining
		 *  properties of the variables worthing to be indexed
		 */
		if (bindingElement instanceof InitializedBindingElementTree) {
			InitializedBindingElementTree initializedBindingElement = (InitializedBindingElementTree) bindingElement;
			processExpression((ExpressionTree)initializedBindingElement.right(), parent, symbols, assignedSymbols, exported);
		}
	}
	
	private void processExpression(ExpressionTree expression, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols, 
			List<JavaScriptSymbol> assignedSymbols, boolean exported) {
		if (expression instanceof AssignmentExpressionTree) {
			processAssignmentExpression((AssignmentExpressionTree)expression, parent, symbols, assignedSymbols, exported);
		} else if (expression instanceof ObjectLiteralTree) {
			processObjectLiteral((ObjectLiteralTree)expression, symbols, parent, assignedSymbols);
		} else if (expression instanceof ParenthesisedExpressionTree) {
			ParenthesisedExpressionTree parenthesisedExpression = (ParenthesisedExpressionTree) expression;
			processExpression(parenthesisedExpression.expression(), parent, symbols, assignedSymbols, exported);
		} else if (expression instanceof NewExpressionTree) { // new SomeClass(...)
			NewExpressionTree newExpression = (NewExpressionTree) expression;
			processExpression(newExpression.expression(), parent, symbols, Lists.newArrayList(), false);
			if (newExpression.arguments() != null) {
				for (Tree parameter: newExpression.arguments().parameters()) {
					if (parameter instanceof ExpressionTree) {
						processExpression((ExpressionTree)parameter, parent, symbols, Lists.newArrayList(), false);
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
				    for (JavaScriptSymbol assignedSymbol: assignedSymbols) {
                        assignedSymbol.setLocal(true);
                        assignedSymbol.setExported(false);
                    }
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
				ReferenceSymbol componentSymbol = new ReferenceSymbol(null, getName(vueComponent.token()),
                        getPosition(vueComponent.token()), true, null);
				symbols.add(componentSymbol);
				assignedSymbols.add(componentSymbol);
				
				if (callExpression.arguments().parameters().get(1) instanceof ExpressionTree) {
					processExpression((ExpressionTree)callExpression.arguments().parameters().get(1), 
							parent, symbols, assignedSymbols, false);
				}
				processed = true;
			} 
			if (!processed 
					&& callExpression.callee() instanceof DotMemberExpressionTree 
					&& StringUtils.deleteWhitespace(callExpression.callee().toString()).equals("Vue.extend") 
					&& !callExpression.arguments().parameters().isEmpty()) {
				if (callExpression.arguments().parameters().get(0) instanceof ExpressionTree) {
					processExpression((ExpressionTree)callExpression.arguments().parameters().get(0), 
							parent, symbols, assignedSymbols, false);
				}
				processed = true;
			} 
			
			if (!processed) {
				processExpression(callExpression.callee(), parent, symbols, Lists.newArrayList(), false);
				for (Tree parameter: callExpression.arguments().parameters()) {
					if (parameter instanceof ExpressionTree) {
						processExpression((ExpressionTree)parameter, parent, symbols, Lists.newArrayList(), false);
					} 
				}
			}
		} else if (expression instanceof FunctionTree) { // an inline function declaration
			if (!assignedSymbols.isEmpty()) {
				for (JavaScriptSymbol assigned: assignedSymbols) {
					processTree(((FunctionTree)expression).body(), assigned, symbols);
				}
			} else {
				if (expression instanceof FunctionExpressionTree) {
					FunctionExpressionTree functionExpression = (FunctionExpressionTree) expression;
					IdentifierTree identifier = functionExpression.name();
					parent = new FunctionSymbol(parent, getName(identifier),
                            getPosition(identifier, functionExpression.functionKeyword()), parent!=null, false,
							describeParameters(functionExpression.parameterClause()));
					symbols.add(parent);
				} else if (expression instanceof ArrowFunctionTree) {
					ArrowFunctionTree arrowFunction = (ArrowFunctionTree) expression;
					parent = new FunctionSymbol(parent, null, getPosition(arrowFunction.doubleArrow()),
                            parent!=null, false, describeParameters(arrowFunction.parameterClause()));
					symbols.add(parent);
				}
				processTree(((FunctionTree)expression).body(), parent, symbols);
			}
		} else if (expression instanceof ClassTree) {
			ClassTree classTree = (ClassTree) expression;
			if (!assignedSymbols.isEmpty()) {
				for (JavaScriptSymbol assigned: assignedSymbols) {
					processMethosDeclarations(classTree.methods(), assigned, symbols);
				}
			} else {
				parent = new ClassSymbol(parent, getName(classTree.name()),
                        getPosition(classTree.name(), classTree.classToken()), parent!=null, false);
				symbols.add(parent);
				processMethosDeclarations(classTree.methods(), parent, symbols);
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
					if (referenceSymbol.getName() != null) {
						if (referenceSymbol.getObject()!=null 
									&& (referenceSymbol.getObject() + "." + referenceSymbol.getName()).equals("module.exports")
								|| referenceSymbol.getObject()==null && referenceSymbol.getName().equals("exports")) {
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
					SyntaxToken token = identifier.identifierToken();
					referenceSymbol = new ReferenceSymbol(null, getName(token), getPosition(token), true, null);
				} else {
					DotMemberExpressionTree dotMemberExpression = (DotMemberExpressionTree) expression;
					SyntaxToken token = dotMemberExpression.property().identifierToken();
					referenceSymbol = new ReferenceSymbol(null, getName(token), getPosition(token), true,
							StringUtils.deleteWhitespace(dotMemberExpression.object().toString()));
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
	
	private void processObjectLiteral(ObjectLiteralTree objectLiteral, List<JavaScriptSymbol> symbols, 
			JavaScriptSymbol parent, List<JavaScriptSymbol> assignedSymbols) {
		List<JavaScriptSymbol> associatedSymbols = new ArrayList<>(assignedSymbols);
		if (associatedSymbols.isEmpty())
			associatedSymbols.add(parent);
		for (JavaScriptSymbol associatedSymbol: associatedSymbols) {
			for (Tree property: objectLiteral.properties()) {
				if (property instanceof PairPropertyTree) {
					PairPropertyTree pairProperty = (PairPropertyTree) property;
					SyntaxToken nameToken = getNameToken(pairProperty.key());
					if (nameToken != null) {
						PropertySymbol propertySymbol = new PropertySymbol(associatedSymbol, getName(nameToken),
                                getPosition(nameToken));
						symbols.add(propertySymbol);
						processExpression(pairProperty.value(), parent, symbols, Lists.newArrayList(propertySymbol), false);
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
						MethodSymbol methodSymbol = new MethodSymbol(associatedSymbol, getName(nameToken),
                                getPosition(nameToken), accessorType,
                                describeParameters(methodDeclaration.parameterClause()));
						symbols.add(methodSymbol);
						processBlock(methodDeclaration.body(), methodSymbol, symbols);
					}
				}
			}
		}
	}
	
	private void processAssignmentExpression(AssignmentExpressionTree assignmentExpression, JavaScriptSymbol parent, 
			List<JavaScriptSymbol> symbols, List<JavaScriptSymbol> assignedSymbols, boolean exported) {
		if (assignmentExpression.is(Kind.ASSIGNMENT)) {
			ExpressionTree variable = assignmentExpression.variable();
			if (variable instanceof IdentifierTree) {
				IdentifierTree identifier = (IdentifierTree) assignmentExpression.variable();
				SyntaxToken token = identifier.identifierToken();
				ReferenceSymbol symbol = new ReferenceSymbol(exported?null:parent, getName(token), getPosition(token), exported, null);
				symbols.add(symbol);
				assignedSymbols.add(symbol);
				processExpression(assignmentExpression.expression(), parent, symbols, assignedSymbols, exported);
			} else if (variable instanceof DotMemberExpressionTree) {
				DotMemberExpressionTree dotMemberExpression = (DotMemberExpressionTree) variable;
				SyntaxToken token = dotMemberExpression.property().identifierToken();
				String object = StringUtils.deleteWhitespace(dotMemberExpression.object().toString()); 
				ReferenceSymbol symbol = new ReferenceSymbol(exported?null:parent, getName(token), 
						getPosition(token), exported, object);
				symbols.add(symbol);
				assignedSymbols.add(symbol);
				processExpression(assignmentExpression.expression(), parent, symbols, assignedSymbols, exported);
			}
		}
	}

    private TokenPosition getPosition(SyntaxToken token) {
        return new TokenPosition(token.line()-1, token.column(), token.endLine()-1, token.endColumn());
    }

    private TokenPosition getPosition(@Nullable SyntaxToken token, SyntaxToken thenToken) {
	    return token!=null?getPosition(token):getPosition(thenToken);
    }

    private TokenPosition getPosition(@Nullable IdentifierTree tree, SyntaxToken thenToken) {
        return tree!=null?getPosition(tree.identifierToken(), thenToken):getPosition((SyntaxToken)null, thenToken);
    }

    private String getName(@Nullable SyntaxToken token) {
        return token!=null?removeQuotes(token.text()):null;
    }

    private String getName(@Nullable IdentifierTree tree) {
        return tree!=null?getName(tree.identifierToken()):null;
    }

    private String removeQuotes(String name) {
        return StringUtils.stripEnd(StringUtils.stripStart(name, "'\""), "'\"");
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