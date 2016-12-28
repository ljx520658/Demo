package com.gitplex.symbolextractor.helper;

import static org.junit.Assert.fail;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.Assert;
import org.junit.Test;

import com.gitplex.symbolextractor.ExtractException;
import com.gitplex.symbolextractor.helper.TokenFilter;
import com.gitplex.symbolextractor.helper.TokenStream;
import com.gitplex.symbolextractor.java.JavaLexer;

public class TokenStreamTest {

	@Test
	public void testBalanced() {
		String text = "public class MyClass {"
				+ "  public void sayHello() {System.out.println(\"hello\");}"
				+ "}";
		
		TokenStream extractStream = new TokenStream(
				new JavaLexer(new ANTLRInputStream(text)), TokenFilter.DEFAULT_CHANNEL);
		
		extractStream.nextType(JavaLexer.LBRACE);
		Assert.assertEquals("}", extractStream.nextClosed(JavaLexer.LBRACE, JavaLexer.RBRACE).getText());
	}

	@Test
	public void testNonBalanced() {
		String text = "public class MyClass {"
				+ "  public void sayHello() {System.out.println(\"hello\");}";
		
		TokenStream extractStream = new TokenStream(
				new JavaLexer(new ANTLRInputStream(text)), TokenFilter.DEFAULT_CHANNEL);
		extractStream.nextType(JavaLexer.LBRACE);
		try {
			extractStream.nextClosed(JavaLexer.LBRACE, JavaLexer.RBRACE);
			fail();
		} catch (ExtractException e) {
		}
	}
	
}
