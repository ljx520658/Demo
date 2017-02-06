package com.gitplex.jsymbol;

import java.io.IOException;

public class Test {

	@org.junit.Test
	public void test() throws IOException {	
		/*
		Collection<File> files = FileUtils.listFiles(new File("w:\\temp\\linux"), new IOFileFilter() {

			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(".c"); 
			}

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".c");
			}
			
		}, new IOFileFilter() {

			@Override
			public boolean accept(File file) {
				return true;
			}

			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
			
		});

		long time = System.currentTimeMillis();
		int count = 0;
		for (File file: files) {
			String content = FileUtils.readFileToString(file, Charsets.UTF_8);
			CDeclarationLexer lexer = new CDeclarationLexer(new ANTLRInputStream(content));
			lexer.removeErrorListeners();
			CommonTokenStream stream = new SkippableTokenStream(lexer);
			CDeclarationParser parser = new CDeclarationParser(stream);
			parser.removeErrorListeners();
			parser.compilationUnit();
			if (count++ % 100 == 0)
				System.out.println(count);
		}
		System.out.println(System.currentTimeMillis()-time);
		*/
	}

}
