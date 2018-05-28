package org.metaborg.sdf2table.tests.lr_table;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;
import org.metaborg.sdf2table.io.ParseTableGenerator;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableGenType;

public class LALRTableTest {
	String resourcesPath;
	
	public LALRTableTest() {
		resourcesPath = "src/test/resources/";
	}
	
	@Test
    public void LALRCompileTest() throws Exception {
		String grammarName = "helloworld5";
		File[] files = getFiles(grammarName);
		
		assert(files[0].exists() && !files[0].isDirectory());
		
		ParseTableGenerator ptGen = new ParseTableGenerator(files[0], files[1],
				files[2], files[3], new ArrayList<String>(), ParseTableGenType.LALR, 1);
		
		ptGen.outputTable(false, true, true);	
	}
	
	@Test
    public void LALRStatesTest() throws Exception {
		String grammarName = "helloworld5";
		File[] files = getFiles(grammarName);
		
		assert(files[0].exists() && !files[0].isDirectory());
		
		ParseTableGenerator ptGen = new ParseTableGenerator(files[0], files[1],
				files[2], files[3], new ArrayList<String>(), ParseTableGenType.LALR, 1);
		
		ptGen.outputTable(false, true, true);
		
		int states = ptGen.getParseTable().totalStates();
		System.out.println(ptGen.getParseTable().stateLabels());
		System.out.println(states);
		System.out.println(ptGen.getParseTable().augmentedKernelMap().keySet().size());
		assert(states == 16);
	}
	
	public File[] getFiles(String grammarName) {
		File[] files = new File[4];
		resourcesPath = "src/test/resources/";
		files[0] = new File(resourcesPath + grammarName + "_in.aterm");
		files[1] = new File(resourcesPath + grammarName + "_out.aterm");
		files[2] = new File(resourcesPath + grammarName + ".tbl");
		files[3] = new File(resourcesPath + grammarName + ".yy");
		
		return files;
	}
}