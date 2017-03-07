package com.gitplex.jsymbol;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import com.gitplex.jsymbol.cpp.CppExtractor;

public class Test {

	@org.junit.Test
	public void test() throws IOException {	
		CppExtractor extractor = new CppExtractor();
		Collection<File> files = FileUtils.listFiles(new File("w:\\temp\\tensorflow-master"), new String[]{"cc"}, true);
		int count = 0;
		System.out.println(files.size());
		for (File file: files) {
			String fileContent = FileUtils.readFileToString(file);
			extractor.extract("test.cc", fileContent).size();
			count++;
			System.out.println(count);
		}
	}

}
