package org.metaborg.sdf2table.benchmark;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.metaborg.characterclasses.CharacterClassFactory;
import org.metaborg.characterclasses.ICharacterClassFactory;
import org.metaborg.parsetable.IParseTable;
import org.metaborg.sdf2table.io.ParseTableGenerator;
import org.metaborg.sdf2table.parsetable.ParseTableGenType;
import org.metaborg.sdf2table.parsetable.query.ActionsForCharacterRepresentation;
import org.metaborg.sdf2table.parsetable.query.ProductionToGotoRepresentation;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
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
import org.spoofax.jsglr2.parser.ParseException;
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

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ParseTableBenchmark extends BaseBenchmark {
	
	public enum BenchmarkType {
		ParseGen, Parsing;
	}
	
	protected String basePath;
	protected String inputPath;
	
	protected String normGrammarPath;
	protected String parseTablePath;
	protected String persistedObjectPath;
	protected ArrayList<String> dependencyPaths;
	protected File[] parseTableGenFiles;
	
	protected ParseTableGenerator ptGen;
	

	protected static TestSet testSet;
	Iterable<Input> inputs;

	protected IParser<?, ?> parser; // Just parsing
	protected JSGLR2<?, ?> jsglr2; // Parsing and imploding (including tokenization)
	
	
	
	
	
	
	@Param({"LR", "SLR"}) public ParseTableGenType parseTableGenType;
	@Param({"0", "1"}) public int k;
	@Param({"Calc", "Pascal", "metaborgc"}) public String grammarName;
	@Param({"ParseGen", "Parsing"}) public BenchmarkType benchmarkType;

	public ParseTableBenchmark() throws Exception {
		super(testSet);
	}
	
	
	
	
	@Benchmark
	public void testParseTableGen() throws Exception {
		ptGen.outputTable(false, true, true);
	}
	
	@Benchmark
	public void testParsing(Blackhole bh) throws ParseException {
		for (Input input : inputs) {
			bh.consume(jsglr2.parseUnsafe(input.content, input.filename, null));
		}
	}
	
	
	

	@Setup
	public void setupBenchmark() throws Exception {
		testSet = new TestSet(grammarName, new TestSetParseTableFromATerm(grammarName),
				new TestSetSingleInput(grammarName + "/test.txt"));
		
		normGrammarPath = "grammars/";
		parseTablePath = "parsetables/"; // must end with "parsetables/"
		persistedObjectPath = "persisted_objects/";

		basePath = new File(
				BenchmarkTestsetReader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
						.getParent()
				+ "/";
		inputPath = basePath + "classes/";

		new File(basePath + "generated/" + parseTablePath).mkdirs();
		new File(basePath + "generated/" + persistedObjectPath).mkdirs();

		dependencyPaths = new ArrayList<String>(); // give path without "/normalized"
		dependencyPaths.add(inputPath + normGrammarPath + grammarName);
		dependencyPaths.add(inputPath + normGrammarPath + "common");

		parseTableGenFiles = new File[4];
		parseTableGenFiles[0] = new File(inputPath + normGrammarPath + grammarName + "/normalized/" + grammarName + "-norm.aterm");
		parseTableGenFiles[1] = new File(basePath + "generated/" + parseTablePath + grammarName + ".tbl");
		parseTableGenFiles[2] = new File(basePath + "generated/" + persistedObjectPath + grammarName + ".obj");
		parseTableGenFiles[3] = new File(basePath + "generated/" + grammarName + ".xx");
		
		ParseTableGenerator ptGen = new ParseTableGenerator(parseTableGenFiles[0], parseTableGenFiles[1], parseTableGenFiles[2], parseTableGenFiles[3], dependencyPaths,
				parseTableGenType, k, true);
		
		
		
		if(benchmarkType == BenchmarkType.Parsing) {
			ptGen.outputTable(false, true, true);


			TestSetReader testSetReader = new BenchmarkTestsetReader(testSet);
			if (n == -1)
				inputs = testSetReader.getInputs();
			else
				inputs = testSetReader.getInputsForSize(n);

			IStateFactory stateFactory = new StateFactory(StateFactory.defaultActionsForCharacterRepresentation,
					StateFactory.defaultProductionToGotoRepresentation);

			IActionsFactory actionsFactory = new ActionsFactory(true);
			ICharacterClassFactory characterClassFactory = new CharacterClassFactory(true, true);

			IParseTable parseTable = new ParseTableReader(characterClassFactory, actionsFactory, stateFactory)
					.read(testSetReader.getParseTableTerm());

			ParseTableVariant bestParseTableVariant = new ParseTableVariant(
					ActionsForCharacterRepresentation.DisjointSorted, ProductionToGotoRepresentation.JavaHashMap);
			Variant variant = new Variant(bestParseTableVariant,
					new ParserVariant(ActiveStacksRepresentation.ArrayList, ForActorStacksRepresentation.ArrayDeque,
							ParseForestRepresentation.Hybrid, ParseForestConstruction.Optimized,
							StackRepresentation.HybridElkhound, Reducing.Elkhound));

			parser = JSGLR2Variants.getParser(parseTable, variant.parser);
			jsglr2 = JSGLR2Variants.getJSGLR2(parseTable, variant.parser);
		}
	}
}
