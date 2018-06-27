package org.metaborg.sdf2table.tests.lr_table;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.junit.Test;
import org.metaborg.characterclasses.CharacterClassFactory;
import org.metaborg.characterclasses.ICharacterClassFactory;
import org.metaborg.parsetable.IParseTable;
import org.metaborg.sdf2table.io.ParseTableGenerator;
import org.metaborg.sdf2table.parsetable.ParseTableGenType;
import org.metaborg.sdf2table.parsetable.query.ActionsForCharacterRepresentation;
import org.metaborg.sdf2table.parsetable.query.ProductionToGotoRepresentation;
import org.metaborg.sdf2table.parsetable.test.JSGLR2TestSetReader;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.JSGLR2;
import org.spoofax.jsglr2.JSGLR2Variants;
import org.spoofax.jsglr2.JSGLR2Variants.ParseTableVariant;
import org.spoofax.jsglr2.JSGLR2Variants.ParserVariant;
import org.spoofax.jsglr2.JSGLR2Variants.Variant;
import org.spoofax.jsglr2.actions.ActionsFactory;
import org.spoofax.jsglr2.actions.IActionsFactory;
import org.spoofax.jsglr2.parseforest.ParseForestConstruction;
import org.spoofax.jsglr2.parseforest.ParseForestRepresentation;
import org.spoofax.jsglr2.parser.IParser;
import org.spoofax.jsglr2.parsetable.ParseTableReadException;
import org.spoofax.jsglr2.parsetable.ParseTableReader;
import org.spoofax.jsglr2.reducing.Reducing;
import org.spoofax.jsglr2.stack.StackRepresentation;
import org.spoofax.jsglr2.stack.collections.ActiveStacksRepresentation;
import org.spoofax.jsglr2.stack.collections.ForActorStacksRepresentation;
import org.spoofax.jsglr2.states.IStateFactory;
import org.spoofax.jsglr2.states.StateFactory;
import org.spoofax.jsglr2.testset.Input;
import org.spoofax.jsglr2.testset.TestSet;
import org.spoofax.jsglr2.testset.TestSetParseTableFromATerm;
import org.spoofax.jsglr2.testset.TestSetReader;
import org.spoofax.jsglr2.testset.TestSetSingleInput;
import org.spoofax.terms.ParseError;


public class JSGLR2Test {
	
	protected IParser<?, ?> parser; // Just parsing
    protected JSGLR2<?, ?> jsglr2; // Parsing and imploding (including tokenization)
    
    protected String basePath = "src/test/resources/";
    protected String grammarName;
    
	protected static TestSet testSet;
	
	public JSGLR2Test() {
		
		grammarName = "metaborgc"; //helloworld6, helloworld7, jasmin, Calc, Pascal, metaborgc
		
		testSet = new TestSet(grammarName, new TestSetParseTableFromATerm(grammarName), 
	    		new TestSetSingleInput(grammarName + "/test.txt"));
		
	}
	
	@Test
	public void JSGLR2CompileTest() throws Exception {
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
				files[2], files[3], dependencyPaths, ParseTableGenType.LR, 0);
		
		ptGen.outputTable(false, true, true);
		
		
		TestSetReader testSetReader = new JSGLR2TestSetReader(testSet);
		
		
		
		IStateFactory stateFactory = new StateFactory(StateFactory.defaultActionsForCharacterRepresentation,
	            StateFactory.defaultProductionToGotoRepresentation);
	        
        IActionsFactory actionsFactory = new ActionsFactory(true);
        ICharacterClassFactory characterClassFactory = new CharacterClassFactory(true, true);
        
        IParseTable parseTable = new ParseTableReader(characterClassFactory, actionsFactory, stateFactory).read(testSetReader.getParseTableTerm());
        
        ParseTableVariant bestParseTableVariant = new ParseTableVariant(ActionsForCharacterRepresentation.DisjointSorted, ProductionToGotoRepresentation.JavaHashMap);
		Variant variant = new Variant(bestParseTableVariant, new ParserVariant(ActiveStacksRepresentation.ArrayList, ForActorStacksRepresentation.ArrayDeque, ParseForestRepresentation.Hybrid, ParseForestConstruction.Optimized, StackRepresentation.HybridElkhound, Reducing.Elkhound));
		 
				
		parser = JSGLR2Variants.getParser(parseTable, variant.parser);
        jsglr2 = JSGLR2Variants.getJSGLR2(parseTable, variant.parser);
        
        
        Iterable<Input> inputs = testSetReader.getInputs();
        
        for(Input input : inputs) {
			parser.parseUnsafe(input.content, input.filename, null);
			//jsglr2.parseUnsafe(input.content, input.filename, null);
        }
	}
	
	
}
