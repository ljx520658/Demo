package com.gitplex.symbolextractor;

import java.io.File;

import org.apache.commons.io.Charsets;
import org.sonar.javascript.parser.JavaScriptParser;
import org.sonar.plugins.javascript.api.tree.Tree;

public class Test {

	@org.junit.Test
	public void test() {
		JavaScriptParser parser = new JavaScriptParser(Charsets.UTF_8);
		Tree tree = parser.parse(new File("w:\\temp\\test.js"));
		System.out.println(tree.getClass());
	}

}
