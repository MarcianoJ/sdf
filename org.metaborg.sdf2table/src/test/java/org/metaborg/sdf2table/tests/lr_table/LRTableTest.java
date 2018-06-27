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
		
		ArrayList<String> dependencyPaths = new ArrayList<String>();
		
		ParseTableGenerator ptGen = new ParseTableGenerator(files[0], files[1],
				files[2], files[3], dependencyPaths, ParseTableGenType.LR, 1);
		
		ptGen.outputTable(false, true, true);
		
		int states = ptGen.getParseTable().totalStates();
		
		System.out.println(ptGen.getParseTable().stateLabels());
		System.out.println(states);
		System.out.println(ptGen.getParseTable().augmentedKernelMap().keySet().size());
		assert(states == 23);
	}
	
//	@Test
//	public void LR1CompileWithGrammarImportTest() throws Exception {
//		String grammarName = "jasmin";
//		File[] files = getFiles(grammarName);
//		
//		assert(files[0].exists() && !files[0].isDirectory());
//		
//		ArrayList<String> dependencyPaths = new ArrayList<String>();
//		dependencyPaths.add(resourcesPath + "grammars/" + grammarName);
//		dependencyPaths.add(resourcesPath + "grammars/common");
//		
//		ParseTableGenerator ptGen = new ParseTableGenerator(files[0], files[1],
//				files[2], files[3], dependencyPaths, ParseTableGenType.LR, 1);
//		
//		ptGen.outputTable(false, true, true);
//		
//		int states = ptGen.getParseTable().totalStates();
//		
//		System.out.println(ptGen.getParseTable().stateLabels());
//		System.out.println(states);
//		System.out.println(ptGen.getParseTable().augmentedKernelMap().keySet().size());
//	}
	
	public File[] getFiles(String grammarName) {
		new File(resourcesPath + "grammars/" + grammarName + "/normalized").mkdirs();
		new File(resourcesPath + "generated/parsetables").mkdirs();
		new File(resourcesPath + "generated/persisted_objects").mkdirs();
		
		File[] files = new File[4];
		files[0] = new File(resourcesPath + "grammars/" + grammarName + "/normalized/" + grammarName + "-norm.aterm");
		files[1] = new File(resourcesPath + "generated/parsetables/" + grammarName + ".tbl");
		files[2] = new File(resourcesPath + "generated/persisted_objects/" + grammarName + ".obj");
		files[3] = new File(resourcesPath + "generated/" + grammarName + ".xx");
		
		return files;
	}
}