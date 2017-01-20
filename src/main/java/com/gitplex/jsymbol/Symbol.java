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

	@Nullable
	public abstract Symbol getParent();
	
	/**
	 * Get name of this symbol.
	 * 
	 * @return
	 * 			name of this symbol for indexing, or <tt>null</tt> if this symbol 
	 * 			does not need to be indexed
	 */
	@Nullable
	public abstract String getName();
	
	public boolean isPassthroughInOutline() {
		return false;
	}

	@Nullable
	public abstract TokenPosition getPosition();

    @Nullable
    public abstract TokenPosition getScope();
    
    /**
     * whether or not this symbol is local to its namespace
     * @return
     *          whether or not the symbol is local
     */
    public abstract boolean isLocal();
    
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
	
	public abstract Image renderIcon(String componentId);
	
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
	
	public Symbol getOutlineParent() {
		Symbol parent = getParent();
		while (parent != null && parent.isPassthroughInOutline()) {
			parent = parent.getParent();
		}
		return parent;
	}
	
    public boolean isLocalInHierarchy() {
    	Symbol current = this;
    	do {
    		if (current.isLocal())
    			return true;
    		current = current.getParent();
    	} while (current != null);
    	
    	return false;
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
	
}
