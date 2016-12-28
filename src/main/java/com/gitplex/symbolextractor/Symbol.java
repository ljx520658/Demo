package com.gitplex.symbolextractor;

import java.io.Serializable;

import javax.annotation.Nullable;

/**
 * Represents a symbol extracted from source file
 * 
 * @author robin
 *
 */
public abstract class Symbol implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final Symbol parent;
	
	private final String name;
	
	private final Position from;
	
	private final Position to;
	
	/**
	 * Construct a symbol
	 * 
	 * @param parent
	 * 			parent symbol, use <tt>null</tt> if there is no parent 
	 * @param name
	 * 			name of the symbol to be indexed for search and cross referencing. 
	 * 			Use <tt>null</tt> if the symbol is only for outline purpose, and 
	 * 			does not need to be indexed for search and cross referenced. For 
	 * 			instance a Java package should be displayed in outline, but does 
	 * 			not need to be indexed
	 * @param pos
	 * 			position of the symbol in source file
	 */
	public Symbol(@Nullable Symbol parent, @Nullable String name, Position from, Position to) {
		this.parent = parent;
		this.name = name;
		this.from = from;
		this.to = to;
	}
	
	public Symbol getParent() {
		return parent;
	}

	/**
	 * Get name of this symbol.
	 * 
	 * @return
	 * 			name of this symbol for indexing, or <tt>null</tt> if this symbol 
	 * 			does not need to be indexed
	 */
	@Nullable
	public String getName() {
		return name;
	}

	public Position getFrom() {
		return from;
	}

	public Position getTo() {
		return to;
	}

	public int score() {
		int relevance = 1;
		Symbol parent = this.parent;
		while (parent != null) {
			if (parent.getName() != null)
				relevance++;
			parent = parent.parent;
		}
		return relevance*name.length();
	}
	
	/**
	 * Get scope of the symbol. Scope is used to show to the user under which scope the 
	 * symbol is. For instance, A method &quot;sayHello()&quot; defined in Java class 
	 * &quot;com.example.HelloWorld&quot; will return the scope as 
	 * &quot;com.example.HelloWorld&quot;. Return <tt>null</tt> if the symbol does not 
	 * have a scope
	 * @return
	 */
	@Nullable
	public abstract String getScope();

	/**
	 * Whether or not this symbol is a primary symbol. Primary symbol will be searched 
	 * before non-primary symbols. For instance, a Java symbol search will match 
	 * against type symbols (class, enumeration, interface, annotation etc.) first 
	 * before matching other symbols such as fields and methods.
	 * 
	 * @return
	 * 			whether or not this symbol is primary
	 */
	public abstract boolean isPrimary();

}
