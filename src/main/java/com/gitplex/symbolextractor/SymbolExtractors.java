package com.gitplex.symbolextractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.reflections.Reflections;

import com.google.common.base.Joiner;

import javassist.Modifier;

public class SymbolExtractors {

	private static final Set<SymbolExtractor> extractorSet;
	
	static {
		extractorSet = new HashSet<>();
		Reflections reflections = new Reflections(SymbolExtractors.class.getPackage().getName());
		for (Class<? extends SymbolExtractor> extractorClass: reflections.getSubTypesOf(SymbolExtractor.class)) {
			if (!Modifier.isAbstract(extractorClass.getModifiers())) {
				try {
					extractorSet.add(extractorClass.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	/**
	 * Get symbol extractor of specified file
	 * 
	 * @return
	 * 			symbol extractor of specified file, or <tt>null</tt> if not found
	 */
	@Nullable
	public SymbolExtractor of(String fileName) {
		for (SymbolExtractor extractor: extractorSet) {
			if (extractor.accept(fileName))
				return extractor;
		}
		return null;
	}

	public String getVersions() {
		List<String> versions = new ArrayList<>();
		
		for (SymbolExtractor extractor: extractorSet) 
			versions.add(extractor.getClass().getName() + ":" + extractor.getVersion());
		
		Collections.sort(versions);
		return Joiner.on(",").join(versions);
	}
	
}
