package com.gitplex.symbolextractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.reflections.Reflections;

import com.google.common.base.Joiner;

import javassist.Modifier;

public class SymbolExtractorRegistry {

	private static final List<SymbolExtractor> extractors;
	
	static {
		extractors = new ArrayList<>();
		Reflections reflections = new Reflections(SymbolExtractorRegistry.class.getPackage().getName());
		for (Class<? extends SymbolExtractor> extractorClass: reflections.getSubTypesOf(SymbolExtractor.class)) {
			if (!Modifier.isAbstract(extractorClass.getModifiers())) {
				try {
					extractors.add(extractorClass.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		extractors.sort((o1, o2)->o1.getClass().getName().compareTo(o2.getClass().getName()));
	}
	
	/**
	 * Get symbol extractor of specified file
	 * 
	 * @return
	 * 			symbol extractor of specified file, or <tt>null</tt> if not found
	 */
	@Nullable
	public static SymbolExtractor getExtractor(String fileName) {
		for (SymbolExtractor extractor: extractors) {
			if (extractor.accept(fileName))
				return extractor;
		}
		return null;
	}

	public static String getVersion() {
		List<String> versions = new ArrayList<>();
		
		for (SymbolExtractor extractor: extractors) 
			versions.add(extractor.getClass().getName() + ":" + extractor.getVersion());
		
		Collections.sort(versions);
		return Joiner.on(",").join(versions);
	}
	
}
