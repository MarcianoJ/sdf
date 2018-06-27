package org.metaborg.sdf2table.tests.lr_table;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.metaborg.parsetable.characterclasses.ICharacterClass;
import org.metaborg.sdf2table.io.ParseTableGenerator;
import org.metaborg.sdf2table.parsetable.LRItem;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableGenType;
import org.metaborg.sdf2table.parsetable.State;

public class NaiveTableTest {
	String basePath;
	String grammarName;
	
	public NaiveTableTest() {
		basePath = "src/test/resources/";
		
		grammarName = "helloworld6";
	}
	
	@Test
	public void NaiveLRTest() throws Exception {
		String normGrammarPath = "grammars/";
    	String parseTablePath = "parsetables/";
    	String persistedObjectPath = "persisted_objects/";
    	
    	//String basePath = new File(JSGLR2Test.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + "/classes/";
    	
    	
    	new File(basePath + "grammars/" + grammarName + "/normalized").mkdirs();
    	new File(basePath + "generated/" + parseTablePath).mkdirs();
		new File(basePath + "generated/" + persistedObjectPath).mkdirs();
		
		ArrayList<String> dependencyPaths = new ArrayList<String>();
		dependencyPaths.add(basePath + normGrammarPath + grammarName);
		dependencyPaths.add(basePath + normGrammarPath + "common");
		
    	File[] files = new File[4];
		files[0] = new File(basePath + normGrammarPath + grammarName + "/normalized/" + grammarName + "-norm.aterm");
		files[1] = new File(basePath + "generated/" + parseTablePath + grammarName + ".tbl");
		files[2] = new File(basePath + "generated/" + persistedObjectPath + grammarName + ".obj");
		files[3] = new File(basePath + "generated/" + grammarName + ".xx");
		
		ParseTableGenerator ptGen = new ParseTableGenerator(files[0], files[1],
				files[2], files[3], dependencyPaths, ParseTableGenType.SLR, 1, false);
		
		ptGen.createParseTable(false, true, true);
		ParseTable parseTable = ptGen.getParseTable();
		
		
		ParseTableGenerator ptGenNaive = new ParseTableGenerator(files[0], files[1],
				files[2], files[3], dependencyPaths, ParseTableGenType.SLR, 1, true);
		
		ptGenNaive.createParseTable(false, true, true);
		ParseTable parseTableNaive = ptGenNaive.getParseTable();
		
		int states = parseTable.totalStates();
		int statesNaive = parseTableNaive.totalStates();
		
		assert(states == statesNaive);
		
		Map<Map<LRItem, List<ICharacterClass>>, State> augKernelMapping = parseTable.augmentedKernelMap();
		Map<Map<LRItem, List<ICharacterClass>>, State> augKernelMappingNaive = parseTableNaive.augmentedKernelMap();
		
		for(Map<LRItem, List<ICharacterClass>> key : augKernelMapping.keySet()) {
			State state = augKernelMapping.get(key);
			State stateNaive = augKernelMappingNaive.get(key);
			System.out.println(key);
			System.out.println(((State) parseTableNaive.getState(state.getLabel())).getKernel().iterator().next());
			System.out.println(state.getLabel());
			if(stateNaive != null)
				System.out.println(stateNaive.getLabel());
			System.out.println();
			assert(state.equals(stateNaive));
		}
		
		for(Map<LRItem, List<ICharacterClass>> key : augKernelMapping.keySet()) {
			System.out.println(key);
		}
		
		for(Map<LRItem, List<ICharacterClass>> key : augKernelMappingNaive.keySet()) {
			System.out.println(key);
			
			System.out.println(augKernelMappingNaive.get(key));
			System.out.println();
			
		}
		
	}
}