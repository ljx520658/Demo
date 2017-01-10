package com.gitplex.jsymbol.javascript;

import com.gitplex.jsymbol.AbstractSymbolExtractor;
import com.gitplex.jsymbol.ExtractException;
import com.gitplex.jsymbol.Symbol;
import com.gitplex.jsymbol.TokenPosition;
import com.gitplex.jsymbol.javascript.symbols.*;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.RecognitionException;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;
import org.sonar.javascript.parser.JavaScriptParser;
import org.sonar.plugins.javascript.api.tree.ScriptTree;
import org.sonar.plugins.javascript.api.tree.Tree;
import org.sonar.plugins.javascript.api.tree.Tree.Kind;
import org.sonar.plugins.javascript.api.tree.declaration.*;
import org.sonar.plugins.javascript.api.tree.expression.*;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.javascript.api.tree.statement.*;

import javax.annotation.Nullable;
import java.util.*;

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
		} else if (tree instanceof ImportDeclarationTree) {
            collect((ImportDeclarationTree)tree, parent, symbols);
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
				MethodSymbol methodSymbol = new MethodSymbol(parent, getName(nameToken), getPosition(nameToken),
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
	 * process ES6 import statement
	 */
	private void collect(ImportDeclarationTree importDeclaration, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
		if (importDeclaration.importClause() instanceof ImportClauseTree) {
			ImportClauseTree importClause = (ImportClauseTree) importDeclaration.importClause();
			if (importClause.namedImport() instanceof SpecifierListTree) {
				SpecifierListTree specifierList = (SpecifierListTree) importClause.namedImport();
				for (SpecifierTree specifier: specifierList.specifiers()) {
					collect(specifier, parent, symbols);
				}
			} else if (importClause.namedImport() instanceof SpecifierTree) {
				collect((SpecifierTree)importClause.namedImport(), parent, symbols);
			}
			IdentifierTree defaultImport = importClause.defaultImport();
			if (defaultImport != null) {
			    SyntaxToken token = defaultImport.identifierToken();
				symbols.add(new VariableSymbol(parent, getName(token), getPosition(token), true,false));
			}
		}
	}

    private void collect(SpecifierTree specifier, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
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
	private void collect(NamedExportDeclarationTree namedExportDeclaration, JavaScriptSymbol parent, List<JavaScriptSymbol> symbols) {
		Tree object = namedExportDeclaration.object();
		if (object instanceof ExportDefaultBinding) {
			ExportDefaultBinding exportDefaultBinding = (ExportDefaultBinding) object;
			IdentifierTree identifier = exportDefaultBinding.exportedDefaultIdentifier();
			if (!identifier.name().equals("default")) {
				symbols.add(new VariableSymbol(parent, getName(identifier.identifierToken()),
                        getPosition(identifier.identifierToken()), parent!=null, true));
			}
		} if (object instanceof FunctionDeclarationTree) {
			collect((FunctionDeclarationTree)object, parent, symbols, true);
		} else if (object instanceof VariableStatementTree) {
			collect(((VariableStatementTree)object).declaration(), parent, symbols, true);
		} else if (object instanceof ExportClauseTree) {
			ExportClauseTree exportClause = (ExportClauseTree) object;
			for (SpecifierTree specifier: exportClause.exports().specifiers()) {
				if (specifier.localName() != null) {
					VariableSymbol symbol = new VariableSymbol(parent, getName(specifier.localName().identifierToken()),
                            getPosition(specifier.localName().identifierToken()), parent!=null, true);
					symbols.add(symbol);
				} else if (specifier.name() instanceof IdentifierTree) {
				    SyntaxToken token = ((IdentifierTree)specifier.name()).identifierToken();
					VariableSymbol symbol = new VariableSymbol(parent, getName(token), getPosition(token),
                            parent!=null, true);
					symbols.add(symbol);
				}
			}
		} else if (object instanceof ClassTree) {
			ClassTree classTree = (ClassTree)object;
			parent = new ClassSymbol(parent, getName(classTree.name()),
                    getPosition(classTree.name(), classTree.classToken()), false, true);
			symbols.add(parent);
			collect(classTree.methods(), parent, symbols);
		}
	}
	
	private void collect(FunctionDeclarationTree functionDeclaration, JavaScriptSymbol parent, 
			List<JavaScriptSymbol> symbols, boolean exported) {
		FunctionSymbol symbol = new FunctionSymbol(parent, getName(functionDeclaration.name()),
                getPosition(functionDeclaration.name(), functionDeclaration.functionKeyword()), parent!=null,
				exported, describeParameters(functionDeclaration.parameterClause()));
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
			parent = new ClassSymbol(parent, getName(classTree.name()),
                    getPosition(classTree.name(), classTree.classToken()), parent!=null, false);
			symbols.add(parent);
			collect(classTree.methods(), parent, symbols);
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
			VariableSymbol symbol = new VariableSymbol(parent, getName(identifier.identifierToken()),
                    getPosition(identifier.identifierToken()), parent!=null, exported);
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
				ReferenceSymbol componentSymbol = new ReferenceSymbol(parent, getName(vueComponent.token()),
                        getPosition(vueComponent.token()), true, null);
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
				collect(((FunctionTree)expression).body(), parent, symbols);
			}
		} else if (expression instanceof ClassTree) {
			ClassTree classTree = (ClassTree) expression;
			if (!assignedSymbols.isEmpty()) {
				for (JavaScriptSymbol assigned: assignedSymbols) {
					collect(classTree.methods(), assigned, symbols);
				}
			} else {
				parent = new ClassSymbol(parent, getName(classTree.name()),
                        getPosition(classTree.name(), classTree.classToken()), parent!=null, false);
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
					if (referenceSymbol.getName() != null) {
						if (referenceSymbol.getObject()!=null && (referenceSymbol.getObject() + "." + referenceSymbol.getName()).equals("module.exports")
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
					referenceSymbol = new ReferenceSymbol(parent, getName(token), getPosition(token),
                            true, null);
				} else {
					DotMemberExpressionTree dotMemberExpression = (DotMemberExpressionTree) expression;
					SyntaxToken token = dotMemberExpression.property().identifierToken();
					referenceSymbol = new ReferenceSymbol(parent, getName(token), getPosition(token), true,
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
						PropertySymbol propertySymbol = new PropertySymbol(associatedSymbol, getName(nameToken),
                                getPosition(nameToken));
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
						MethodSymbol methodSymbol = new MethodSymbol(associatedSymbol, getName(nameToken),
                                getPosition(nameToken), accessorType,
                                describeParameters(methodDeclaration.parameterClause()));
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
				SyntaxToken token = identifier.identifierToken();
				ReferenceSymbol symbol = new ReferenceSymbol(parent, getName(token), getPosition(token),
                        false, null);
				symbols.add(symbol);
				assignedSymbols.add(symbol);
				collect(assignmentExpression.expression(), parent, symbols, assignedSymbols, exported);
			} else if (variable instanceof DotMemberExpressionTree) {
				DotMemberExpressionTree dotMemberExpression = (DotMemberExpressionTree) variable;
				SyntaxToken token = dotMemberExpression.property().identifierToken();
				ReferenceSymbol symbol = new ReferenceSymbol(parent, getName(token), getPosition(token), false,
						StringUtils.deleteWhitespace(dotMemberExpression.object().toString()));
				symbols.add(symbol);
				assignedSymbols.add(symbol);
				collect(assignmentExpression.expression(), parent, symbols, assignedSymbols, exported);
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