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


public class JSGLR2Test extends TestSetReader {
	
	protected IParser<?, ?> parser; // Just parsing
    protected JSGLR2<?, ?> jsglr2; // Parsing and imploding (including tokenization)
    
	protected static TestSet testSet = new TestSet("helloworld6", new TestSetParseTableFromATerm("helloworld6"), 
    		new TestSetSingleInput("helloworld/test.txt"));
	
	public JSGLR2Test() {
		super(TestSet.greenMarl);
	}
	
	@Test
	public void JSGLR2CompileTest() throws Exception {
		String normGrammarPath = "grammars/";
    	String parseTablePath = "parsetables/";
    	String persistedObjectPath = "persisted_objects/";
    	
    	String grammarName = "helloworld6";
    	
    	//String basePath = new File(JSGLR2Test.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + "/classes/";
    	String basePath = "src/test/resources/";
    	
    	File[] files = new File[4];
		files[0] = new File(basePath + normGrammarPath + grammarName + ".aterm");
		files[1] = new File(basePath + parseTablePath + grammarName + ".tbl");
		files[2] = new File(basePath + persistedObjectPath + grammarName + ".obj");
		files[3] = new File(basePath + normGrammarPath + grammarName + ".xx");
		
		ParseTableGenerator ptGen = new ParseTableGenerator(files[0], files[1],
				files[2], files[3], new ArrayList<String>(), ParseTableGenType.LR, 0);
		
		ptGen.outputTable(false, true, true);
		
		
		IStateFactory stateFactory = new StateFactory(StateFactory.defaultActionsForCharacterRepresentation,
	            StateFactory.defaultProductionToGotoRepresentation);
	        
        IActionsFactory actionsFactory = new ActionsFactory(true);
        ICharacterClassFactory characterClassFactory = new CharacterClassFactory(true, true);
        
        IParseTable parseTable = new ParseTableReader(characterClassFactory, actionsFactory, stateFactory).read(this.getParseTableTerm());
        
        ParseTableVariant bestParseTableVariant = new ParseTableVariant(ActionsForCharacterRepresentation.DisjointSorted, ProductionToGotoRepresentation.JavaHashMap);
		Variant variant = new Variant(bestParseTableVariant, new ParserVariant(ActiveStacksRepresentation.ArrayList, ForActorStacksRepresentation.ArrayDeque, ParseForestRepresentation.Hybrid, ParseForestConstruction.Optimized, StackRepresentation.HybridElkhound, Reducing.Elkhound));
		
		// -> BenchmarkTestsetReader 
				
		parser = JSGLR2Variants.getParser(parseTable, variant.parser);
        jsglr2 = JSGLR2Variants.getJSGLR2(parseTable, variant.parser);
        
        Input in = new Input("test.txt", "aaaaaaa");
        
        jsglr2.parseUnsafe(in.content, in.filename, null);
        
//        for(Input input : inputs) {
//			//bh.consume(parser.parseUnsafe(input.content, input.filename, null));
//			bh.consume(jsglr2.parseUnsafe(input.content, input.filename, null));
//        }
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	private String basePath() {
        try {
            return new File(
            		JSGLR2Test.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
                    .getParent();
        } catch(URISyntaxException e) {
            throw new IllegalStateException("base path for benchmarks could not be retrieved");
        }
    }

    @Override
    public IStrategoTerm parseTableTerm(String filename) throws ParseError, IOException {
        InputStream inputStream = new FileInputStream(basePath() + "/" + filename);

        return getTermReader().parseFromStream(inputStream);
    }

    @Override
    public String grammarsPath() {
        return basePath() + "/grammars";
    }

    @Override
    public void setupParseTableFile(String parseTableName) throws IOException {
        File file = new File(basePath() + "/parsetables");
        file.mkdirs();
        
        Class aclass = this.getClass();
		try {
			aclass = JSGLR2Test.class.getProtectionDomain().getCodeSource().getLocation().toURI().getClass();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        URL s = aclass.getResource("/");
        
        InputStream defResourceInJar = getClass().getResourceAsStream("/" + parseTableName + ".tbl");
        InputStream defResourceInJar2 = getClass().getResourceAsStream(basePath() + "/parsetables/" + parseTableName + ".tbl");
        String destinationInTargetDir = basePath() + "/parsetables/" + parseTableName + ".tbl";

        Files.copy(defResourceInJar, Paths.get(destinationInTargetDir), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void setupDefFile(String grammarName) throws IOException {
        new File(basePath() + "/grammars").mkdirs();

        InputStream defResourceInJar = getClass().getResourceAsStream("/grammars/" + grammarName + ".def");
        String destinationInTargetDir = basePath() + "/grammars/" + grammarName + ".def";

        Files.copy(defResourceInJar, Paths.get(destinationInTargetDir), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    protected String getFileAsString(String filename) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/samples/" + filename);

        return inputStreamAsString(inputStream);
    }
}