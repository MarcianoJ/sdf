package org.metaborg.sdf2table.tests.lr_table;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;
import org.metaborg.sdf2table.io.ParseTableGenerator;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableGenType;

public class LRTableTest {
	String resourcesPath;
	
	public LRTableTest() {
		resourcesPath = "src/test/resources/";
	}
	
	@Test
    public void LR0CompileTest() throws Exception {
		String grammarName = "helloworld6";
		File[] files = getFiles(grammarName);
		
		assert(files[0].exists() && !files[0].isDirectory());
		
		ParseTableGenerator ptGen = new ParseTableGenerator(files[0], files[1],
				files[2], files[3], new ArrayList<String>(), ParseTableGenType.LR, 0);
		
		ptGen.outputTable(false, true, true);	
	}
	
	@Test
    public void LR0StatesTest() throws Exception {
		String grammarName = "helloworld6";
		File[] files = getFiles(grammarName);
		
		assert(files[0].exists() && !files[0].isDirectory());
		
		ParseTableGenerator ptGen = new ParseTableGenerator(files[0], files[1],
				files[2], files[3], new ArrayList<String>(), ParseTableGenType.LR, 0);
		
		ptGen.createParseTable(false, true, true);
		
		int states = ptGen.getParseTable().totalStates();
		System.out.println(ptGen.getParseTable().stateLabels());
		assert(states == 16);
	}
	
	@Test
	public void LR1CompileTest() throws Exception {
		String grammarName = "helloworld6";
		File[] files = getFiles(grammarName);
		
		assert(files[0].exists() && !files[0].isDirectory());
		
		ParseTableGenerator ptGen = new ParseTableGenerator(files[0], files[1],
				files[2], files[3], new ArrayList<String>(), ParseTableGenType.LR, 1);
		
		ptGen.outputTable(false, true, true);
	}
	
	@Test
	public void LR1StatesTest() throws Exception {
		String grammarName = "helloworld6";
		File[] files = getFiles(grammarName);
		
		assert(files[0].exists() && !files[0].isDirectory());
		
		ParseTableGenerator ptGen = new ParseTableGenerator(files[0], files[1],
				files[2], files[3], new ArrayList<String>(), ParseTableGenType.LR, 1);
		
		ptGen.outputTable(false, true, true);
		
		int states = ptGen.getParseTable().totalStates();
		
		System.out.println(ptGen.getParseTable().stateLabels());
		System.out.println(states);
		System.out.println(ptGen.getParseTable().augmentedKernelMap().keySet().size());
		assert(states == 23);
	}
	
	public File[] getFiles(String grammarName) {
		File[] files = new File[4];
		resourcesPath = "src/test/resources/";
		files[0] = new File(resourcesPath + "grammars/" + grammarName + ".aterm");
		files[1] = new File(resourcesPath + "parsetables/" + grammarName + ".tbl");
		files[2] = new File(resourcesPath + "persisted_objects/" + grammarName + ".obj");
		files[3] = new File(resourcesPath + grammarName + ".xx");
		
		return files;
	}
}