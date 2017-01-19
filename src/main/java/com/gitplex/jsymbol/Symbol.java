package com.gitplex.jsymbol;

import java.io.Serializable;
import java.util.List;

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
	
	private TokenPosition scope;

	private boolean local;
	
	private List<String> superSymbolNames;
	
	/**
	 * Construct a symbol
	 * 
	 * @param parent
	 * 			parent symbol, use <tt>null</tt> if there is no parent 
	 * @param name
	 * 			name of the symbol. Use <tt>null</tt> if the symbol does not have a name
	 * @param position
	 * 			position of the symbol name in source file. Use <tt>null</tt> if position is unknown
	 * @param scope
	 * 			scope of the symbol in source file, for instance a method scope covers the method body. 
	 * 			Use <tt>null</tt> if scope is unknown 
     * @param local
     * 			whether or not this is a local symbol which can not be accessed from other files
     * @param superSymbolNames
     * 			names of symbols this symbol is extended from
     */
	public Symbol(@Nullable Symbol parent, @Nullable String name, @Nullable TokenPosition position, 
			@Nullable TokenPosition scope, boolean local, List<String> superSymbolNames) {
		this.parent = parent;
		this.name = name;
		this.position = position;
		this.scope = scope;
		this.local = local;
		this.superSymbolNames = superSymbolNames;
	}
	
	@Nullable
	public Symbol getParent() {
		return parent;
	}
	
	public Symbol getOutlineParent() {
		Symbol parent = getParent();
		while (parent != null && parent.isPassthroughInOutline()) {
			parent = parent.getParent();
		}
		return parent;
	}
	
	public boolean isPassthroughInOutline() {
		return false;
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
	
	@Nullable
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
    
    public boolean isEffectivelyLocal() {
    	Symbol current = this;
    	do {
    		if (current.isLocal())
    			return true;
    		current = current.getParent();
    	} while (current != null);
    	
    	return false;
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

    @Nullable
    public TokenPosition getScope() {
		return scope;
	}

	public void setScope(TokenPosition scope) {
		this.scope = scope;
	}

	public List<String> getSuperSymbolNames() {
		return superSymbolNames;
	}

	public void setSuperSymbolNames(List<String> superSymbolNames) {
		this.superSymbolNames = superSymbolNames;
	}

	/**
	 * Get namespace of the symbol
	 * 
	 * @return
     *          namespace of the symbol, or <tt>null</tt> if the symbol is in global namespace
	 */
	@Nullable
	public String getNamespace() {
		String scope;
		if (getParent() != null) {
			String parentNamespace = getParent().getNamespace();
			String parentName = getParent().getName();
			if (parentName == null)
				parentName = "{}";
			if (parentNamespace != null)
				scope = parentNamespace + "." + parentName;
			else
				scope = parentName;
		} else {
			scope = null;
		}
		return scope;
	}
	
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

	public boolean isSearchable() {
		return true;
	}
	
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
