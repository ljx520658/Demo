package com.gitplex.jsymbol;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;

/**
 * Represents a symbol extracted from source file. Implementation should preserve enough 
 * information in the symbol (such as method parameters, return type, method modifier) 
 * for UI to render it appropriately. Various Java symbols in Java extractor package 
 * can be used as an example
 * 
 * @author robin
 *
 */
public abstract class Symbol implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Symbol parent;
	
	private String name;
	
	private TokenPosition position;

	private boolean local;
	
	/**
	 * Construct a symbol
	 * 
	 * @param parent
	 * 			parent symbol, use <tt>null</tt> if there is no parent 
	 * @param name
	 * 			name of the symbol. Use <tt>null</tt> if the symbol does not have a name
	 * @param position
	 * 			position of the symbol in source file
     * @param local
     * 			whether or not this is a local symbol which can not be accessed from other files
     */
	public Symbol(@Nullable Symbol parent, @Nullable String name, TokenPosition position, boolean local) {
		this.parent = parent;
		this.name = name;
		this.position = position;
		this.local = local;
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
	
	public abstract Image renderIcon(String componentId);
	
	public TokenPosition getPosition() {
		return position;
	}

    /**
     * whether or not this is a local symbol which can not be accessed from other files
     * @return
     *          whether or not the symbol is local
     */
    public boolean isLocal() {
        return local;
    }

    public void setParent(Symbol parent) {
        this.parent = parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(TokenPosition position) {
        this.position = position;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public int score() {
		int relevance = 1;
		Symbol parent = this.parent;
		while (parent != null) {
			if (parent.getName() != null)
				relevance++;
			parent = parent.parent;
		}
		return relevance*(name !=null? name.length():1);
	}
	
	/**
	 * Get scope of the symbol. Scope is used to show to the user under which scope the 
	 * symbol is. For instance, A method &quot;sayHello()&quot; defined in Java class 
	 * &quot;com.example.HelloWorld&quot; will return the scope as 
	 * &quot;com.example.HelloWorld&quot;. Return <tt>null</tt> if the symbol does not 
	 * have a scope
	 * @return
     *          get scope of the symbol
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

	/**
	 * Render the symbol in web UI
	 * 
	 * @param componentId
	 * 			wicket component id corresponding to this symbol
	 * @param highlight
	 * 			range of the name to be highlighted if not <tt>null</tt>. For instance, 
	 * 			when user searches for a symbol, this range will be used to highlight
	 * 			the matched search in the symbol name
	 * 			
	 * @return
	 * 			a wicket component to be displayed in web UI for the symbol
	 */
	public abstract Component render(String componentId, @Nullable Range highlight);
	
}
