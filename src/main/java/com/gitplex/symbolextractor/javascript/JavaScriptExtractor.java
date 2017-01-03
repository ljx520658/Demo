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
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.javascript.symbols.AssignedSymbol;
import com.gitplex.symbolextractor.javascript.symbols.ClassSymbol;
import com.gitplex.symbolextractor.javascript.symbols.DeclarationType;
import com.gitplex.symbolextractor.javascript.symbols.FunctionSymbol;
import com.gitplex.symbolextractor.javascript.symbols.JavaScriptSymbol;
import com.gitplex.symbolextractor.javascript.symbols.MethodAccessorType;
import com.gitplex.symbolextractor.javascript.symbols.MethodSymbol;
import com.gitplex.symbolextractor.javascript.symbols.PropertySymbol;
import com.gitplex.symbolextractor.javascript.symbols.VariableSymbol;
import com.google.common.collect.Lists;

public class JavaScriptExtractor extends AbstractSymbolExtractor {

	@Override
	public List<Symbol> extract(String source) throws ExtractException {
		List<Symbol> symbols = new ArrayList<>();
		JavaScriptParser parser = new JavaScriptParser(Charsets.UTF_8);
		collect(parser.parse(source), null, symbols);
		Map<Symbol, Set<String>> containedDeclarations = new HashMap<>();
		for (Symbol symbol: symbols) {
			if (!(symbol instanceof AssignedSymbol) && symbol.getIndexName() != null) {
				Symbol parent = symbol.getParent();
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

		for (Iterator<Symbol> it = symbols.iterator(); it.hasNext();) {
			Symbol symbol = it.next();
			if (symbol.getParent() != null 
					&& !(symbol instanceof AssignedSymbol) 
					&& !(symbol instanceof MethodSymbol) 
					&& !(symbol instanceof PropertySymbol)) {
				it.remove();
			}
		}
		
		for (Iterator<Symbol> it = symbols.iterator(); it.hasNext();) {
			Symbol symbol = it.next();
			if (symbol instanceof AssignedSymbol) {
				AssignedSymbol assignedSymbol = (AssignedSymbol) symbol;
				String rootObject = assignedSymbol.getRootObject();
				Symbol parent = symbol.getParent();
				while (parent != null) {
					Set<String> containedDeclarationsOfParent = containedDeclarations.get(parent);
					if (containedDeclarationsOfParent != null && containedDeclarationsOfParent.contains(rootObject)) {
						it.remove();
						break;
					}
					parent = parent.getParent();
				}
			}
		}

		Set<Symbol> hasChildren = new HashSet<>();
		for (Symbol symbol: symbols) {
			Symbol parent = symbol.getParent();
			if (parent != null)
				hasChildren.add(parent);
		}
		for (Iterator<Symbol> it = symbols.iterator(); it.hasNext();) {
			Symbol symbol = it.next();
			if (symbol instanceof AssignedSymbol && symbol.getIndexName() == null && !hasChildren.contains(symbol))
				it.remove();
		}
		
		List<Symbol> addSymbols = new ArrayList<>();
		Set<Symbol> symbolSet = new HashSet<>(symbols);
		for (Symbol symbol: symbols) {
			if (symbol instanceof AssignedSymbol) {
				Symbol parent = symbol.getParent();
				while (parent != null) {
					if (!symbolSet.contains(parent)) {
						symbolSet.add(parent);
						addSymbols.add(parent);
					}
					parent = parent.getParent();
				}
			}
		}
		symbols.addAll(addSymbols);
		
		List<Symbol> removeSymbols = new ArrayList<>();
		for (Symbol symbol: symbols) {
			if (!(symbol instanceof AssignedSymbol)) {
				Symbol parent = symbol.getParent();
				while (parent != null) {
					if (!symbolSet.contains(parent)) {
						removeSymbols.add(symbol);
						break;
					}
					parent = parent.getParent();
				}
			}
		}
		symbols.removeAll(removeSymbols);
		
		return symbols;
	}
	
	private void collect(Tree tree, Symbol parent, List<Symbol> symbols) {
		if (tree instanceof ScriptTree) {
			for (Tree item: ((ScriptTree)tree).items().items()) {
				collect(item, parent, symbols);
			}
		} else if (tree instanceof StatementTree) {
			collect((StatementTree)tree, parent, symbols);
		} else if (tree instanceof NamedExportDeclarationTree) {
			collect((NamedExportDeclarationTree)tree, parent, symbols);
		} else if (tree instanceof ImportDeclarationTree) {
			collect((ImportDeclarationTree)tree, parent, symbols);
		} 
	}
	
	private void collect(Iterable<MethodDeclarationTree> methodDeclarations, Symbol parent, List<Symbol> symbols) {
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
	
	private void collect(ImportDeclarationTree importDeclaration, Symbol parent, List<Symbol> symbols) {
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
				symbols.add(new VariableSymbol(parent, defaultImport.identifierToken(), DeclarationType.IMPORT));
			}
		}
	}
	
	private void collect(SpecifierTree specifier, Symbol parent, List<Symbol> symbols) {
		IdentifierTree identifier = specifier.localName();
		if (identifier != null) {
			symbols.add(new VariableSymbol(parent, identifier.identifierToken(), DeclarationType.IMPORT));
		} else if (specifier.name() instanceof IdentifierTree) {
			symbols.add(new VariableSymbol(parent, ((IdentifierTree)specifier.name()).identifierToken(), DeclarationType.IMPORT));
		}
	}
	
	private void collect(NamedExportDeclarationTree namedExportDeclaration, Symbol parent, List<Symbol> symbols) {
		Tree object = namedExportDeclaration.object();
		if (object instanceof ExportDefaultBinding) {
			ExportDefaultBinding exportDefaultBinding = (ExportDefaultBinding) object;
			IdentifierTree identifier = exportDefaultBinding.exportedDefaultIdentifier();
			if (!identifier.name().equals("default")) {
				symbols.add(new VariableSymbol(parent, identifier.identifierToken(), DeclarationType.EXPORT));
			}
		} if (object instanceof FunctionDeclarationTree) {
			collect((FunctionDeclarationTree)object, parent, symbols, DeclarationType.EXPORT);
		} else if (object instanceof VariableStatementTree) {
			collect(((VariableStatementTree)object).declaration(), parent, symbols, DeclarationType.EXPORT);
		} else if (object instanceof ExportClauseTree) {
			ExportClauseTree exportClause = (ExportClauseTree) object;
			for (SpecifierTree specifier: exportClause.exports().specifiers()) {
				if (specifier.localName() != null) {
					VariableSymbol symbol = new VariableSymbol(parent, specifier.localName().identifierToken(), DeclarationType.EXPORT);
					symbols.add(symbol);
				} else if (specifier.name() instanceof IdentifierTree) {
					VariableSymbol symbol = new VariableSymbol(parent, ((IdentifierTree)specifier.name()).identifierToken(), DeclarationType.EXPORT);
					symbols.add(symbol);
				}
			}
		} else if (object instanceof ClassTree) {
			ClassTree classTree = (ClassTree)object;
			parent = new ClassSymbol(parent, classTree.name()!=null?classTree.name().identifierToken():null, DeclarationType.EXPORT);
			symbols.add(parent);
			collect(classTree.methods(), parent, symbols);
		}
	}
	
	private void collect(FunctionDeclarationTree functionDeclaration, Symbol parent, 
			List<Symbol> symbols, DeclarationType type) {
		FunctionSymbol symbol = new FunctionSymbol(parent, functionDeclaration.name().identifierToken(), 
				describeParameters(functionDeclaration.parameterClause()), type);
		symbols.add(symbol);
		collect(functionDeclaration.body(), symbol, symbols);
	}
	
	private void collect(BlockTree body, Symbol parent, List<Symbol> symbols) {
		for (StatementTree statement: body.statements()) {
			collect(statement, parent, symbols);
		}
	}
	
	private void collect(StatementTree statement, Symbol parent, List<Symbol> symbols) {
		if (statement instanceof FunctionDeclarationTree) {
			collect((FunctionDeclarationTree)statement, parent, symbols, DeclarationType.NORMAL);
		} else if (statement instanceof VariableStatementTree) {
			collect(((VariableStatementTree)statement).declaration(), parent, symbols, DeclarationType.NORMAL);
		} else if (statement instanceof ExpressionStatementTree) {
			Tree expression = ((ExpressionStatementTree)statement).expression();
			if (expression instanceof ExpressionTree) {
				collect((ExpressionTree) expression, parent, symbols, new ArrayList<>(), DeclarationType.NORMAL);
			}
		} else if (statement instanceof BlockTree) {
			collect((BlockTree)statement, parent, symbols);
		} else if (statement instanceof ClassTree) {
			ClassTree classTree = (ClassTree) statement;
			parent = new ClassSymbol(parent, classTree.name()!=null?classTree.name().identifierToken():null, DeclarationType.NORMAL);
			symbols.add(parent);
			collect(classTree.methods(), parent, symbols);
		} else if (statement instanceof ReturnStatementTree) {
			ReturnStatementTree returnStatement = (ReturnStatementTree) statement;
			SyntaxToken token = returnStatement.returnKeyword();
			AssignedSymbol assignedSymbol = new AssignedSymbol(parent, null, 
					JavaScriptSymbol.getFrom(token), JavaScriptSymbol.getTo(token), "return", null);
			symbols.add(assignedSymbol);
			collect(returnStatement.expression(), parent, symbols, 
					Lists.newArrayList(assignedSymbol), DeclarationType.NORMAL);
		}
	}
	
	private void collect(VariableDeclarationTree variableDeclaration, Symbol parent, 
			List<Symbol> symbols, DeclarationType type) {
		for (BindingElementTree bindingElement: variableDeclaration.variables()) {
			collect(bindingElement, parent, symbols, type);
		}
	}
	
	private void collect(BindingElementTree bindingElement, Symbol parent, List<Symbol> symbols, DeclarationType type) {
		List<Symbol> assignedSymbols = new ArrayList<>();
		for (IdentifierTree identifier: bindingElement.bindingIdentifiers()) {
			VariableSymbol symbol = new VariableSymbol(parent, identifier.identifierToken(), type);
			assignedSymbols.add(symbol);
		}
		symbols.addAll(assignedSymbols);
		
		if (bindingElement instanceof InitializedBindingElementTree) {
			InitializedBindingElementTree initializedBindingElement = (InitializedBindingElementTree) bindingElement;
			collect((ExpressionTree)initializedBindingElement.right(), parent, symbols, assignedSymbols, type);
		}
	}
	
	private void collect(ExpressionTree expression, Symbol parent, List<Symbol> symbols, 
			List<Symbol> assignedSymbols, DeclarationType type) {
		if (expression instanceof AssignmentExpressionTree) {
			collect((AssignmentExpressionTree)expression, parent, symbols, assignedSymbols, type);
		} else if (expression instanceof ObjectLiteralTree) {
			collect((ObjectLiteralTree)expression, parent, symbols, assignedSymbols);
		} else if (expression instanceof ParenthesisedExpressionTree) {
			ParenthesisedExpressionTree parenthesisedExpression = (ParenthesisedExpressionTree) expression;
			collect(parenthesisedExpression.expression(), parent, symbols, assignedSymbols, type);
		} else if (expression instanceof CallExpressionTree) {
			CallExpressionTree callExpression = (CallExpressionTree) expression;
			collect(callExpression.callee(), parent, symbols, Lists.newArrayList(), DeclarationType.NORMAL);
			for (Tree parameter: callExpression.arguments().parameters()) {
				if (parameter instanceof ExpressionTree) {
					collect((ExpressionTree)parameter, parent, symbols, Lists.newArrayList(), DeclarationType.NORMAL);
				}
			}
		} else if (expression instanceof FunctionTree) {
			if (!assignedSymbols.isEmpty()) {
				for (Symbol assigned: assignedSymbols) {
					collect(((FunctionTree)expression).body(), assigned, symbols);
				}
			} else {
				if (expression instanceof FunctionExpressionTree) {
					FunctionExpressionTree functionExpression = (FunctionExpressionTree) expression;
					IdentifierTree identifier = functionExpression.name();
					SyntaxToken nameToken = identifier!=null?identifier.identifierToken():null;
					parent = new FunctionSymbol(parent, nameToken, 
							describeParameters(functionExpression.parameterClause()), 
							DeclarationType.NORMAL);
					symbols.add(parent);
				} else if (expression instanceof ArrowFunctionTree) {
					ArrowFunctionTree arrowFunction = (ArrowFunctionTree) expression;
					parent = new FunctionSymbol(parent, null, 
							describeParameters(arrowFunction.parameterClause()), 
							DeclarationType.NORMAL);
					symbols.add(parent);
				}
				collect(((FunctionTree)expression).body(), parent, symbols);
			}
		} else if (expression instanceof ClassTree) {
			ClassTree classTree = (ClassTree) expression;
			if (!assignedSymbols.isEmpty()) {
				for (Symbol assigned: assignedSymbols) {
					collect(classTree.methods(), assigned, symbols);
				}
			} else {
				parent = new ClassSymbol(parent, classTree.name()!=null?classTree.name().identifierToken():null, DeclarationType.NORMAL);
				symbols.add(parent);
				collect(classTree.methods(), parent, symbols);
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
	
	private void collect(ObjectLiteralTree objectLiteral, Symbol parent, List<Symbol> symbols, 
			List<Symbol> assignedSymbols) {
		for (Symbol assigned: assignedSymbols) {
			for (Tree property: objectLiteral.properties()) {
				if (property instanceof PairPropertyTree) {
					PairPropertyTree pairProperty = (PairPropertyTree) property;
					SyntaxToken nameToken = getNameToken(pairProperty.key());
					if (nameToken != null) {
						PropertySymbol propertySymbol = new PropertySymbol(assigned, nameToken);
						symbols.add(propertySymbol);
						collect(pairProperty.value(), parent, symbols, Lists.newArrayList(propertySymbol), DeclarationType.NORMAL);
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
						MethodSymbol methodSymbol = new MethodSymbol(assigned, nameToken, 
								accessorType, describeParameters(methodDeclaration.parameterClause()));
						symbols.add(methodSymbol);
						collect(methodDeclaration.body(), methodSymbol, symbols);
					}
				}
			}
		}
	}
	
	private void collect(AssignmentExpressionTree assignmentExpression, Symbol parent, List<Symbol> symbols, 
			List<Symbol> assignedSymbols, DeclarationType type) {
		if (assignmentExpression.is(Kind.ASSIGNMENT)) {
			ExpressionTree variable = assignmentExpression.variable();
			if (variable instanceof IdentifierTree) {
				IdentifierTree identifier = (IdentifierTree) assignmentExpression.variable();
				AssignedSymbol symbol = new AssignedSymbol(parent, identifier.identifierToken(), null);
				symbols.add(symbol);
				assignedSymbols.add(symbol);
				collect(assignmentExpression.expression(), parent, symbols, assignedSymbols, type);
			} else if (variable instanceof DotMemberExpressionTree) {
				DotMemberExpressionTree dotMemberExpression = (DotMemberExpressionTree) variable;
				AssignedSymbol symbol = new AssignedSymbol(parent, dotMemberExpression.property().identifierToken(), 
						StringUtils.deleteWhitespace(dotMemberExpression.object().toString()));
				symbols.add(symbol);
				assignedSymbols.add(symbol);
				collect(assignmentExpression.expression(), parent, symbols, assignedSymbols, type);
			}
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