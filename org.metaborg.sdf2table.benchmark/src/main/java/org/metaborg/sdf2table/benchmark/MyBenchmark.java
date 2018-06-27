package org.metaborg.sdf2table.benchmark;

import java.io.File;
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
import org.openjdk.jmh.annotations.Scope;
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
public class MyBenchmark extends BaseBenchmark {

	protected IParser<?, ?> parser; // Just parsing
	protected JSGLR2<?, ?> jsglr2; // Parsing and imploding (including tokenization)

	protected String grammarName;

	// protected TestSet testSet = TestSet.greenMarl;

	// protected static TestSet testSet = new TestSet("greenmarl", new
	// TestSetParseTableFromATerm("GreenMarl"),
	// new TestSetSingleInput("GreenMarl/infomap.gm"));

	protected static TestSet testSet;

	// new TestSetSizedInput(n -> {
	// return String.join("", Collections.nCopies(n, "a"));
	// }, 7, 7, 7));

	public MyBenchmark() {
		super(testSet);

		grammarName = "metaborgc";  //helloworld6, helloworld7, jasmin, Calc, Pascal, metaborgc

		testSet = new TestSet(grammarName, new TestSetParseTableFromATerm(grammarName),
				new TestSetSingleInput(grammarName + "/test.txt"));
	}

	// @Param({ "7", "7", "7" }) public int n;

	@Benchmark
	public void testMethod(Blackhole bh) throws Exception {
		String normGrammarPath = "grammars/";
		String parseTablePath = "parsetables/"; // must end with "parsetables/"
		String persistedObjectPath = "persisted_objects/";

		String basePath = new File(
				BenchmarkTestsetReader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
						.getParent()
				+ "/";
		String inputPath = basePath + "classes/";

		new File(basePath + "generated/" + parseTablePath).mkdirs();
		new File(basePath + "generated/" + persistedObjectPath).mkdirs();

		ArrayList<String> dependencyPaths = new ArrayList<String>(); // give path without "/normalized"
		dependencyPaths.add(inputPath + normGrammarPath + grammarName);
		dependencyPaths.add(inputPath + normGrammarPath + "common");

		File[] files = new File[4];
		files[0] = new File(inputPath + normGrammarPath + grammarName + "/normalized/" + grammarName + "-norm.aterm");
		files[1] = new File(basePath + "generated/" + parseTablePath + grammarName + ".tbl");
		files[2] = new File(basePath + "generated/" + persistedObjectPath + grammarName + ".obj");
		files[3] = new File(basePath + "generated/" + grammarName + ".xx");

		ParseTableGenerator ptGen = new ParseTableGenerator(files[0], files[1], files[2], files[3], dependencyPaths,
				ParseTableGenType.SLR, 1);

		ptGen.outputTable(false, true, true);

//		Iterable<Input> inputs;
//
//		TestSetReader testSetReader = new BenchmarkTestsetReader(testSet);
//		if (n == -1)
//			inputs = testSetReader.getInputs();
//		else
//			inputs = testSetReader.getInputsForSize(n);
//
//		IStateFactory stateFactory = new StateFactory(StateFactory.defaultActionsForCharacterRepresentation,
//				StateFactory.defaultProductionToGotoRepresentation);
//
//		IActionsFactory actionsFactory = new ActionsFactory(true);
//		ICharacterClassFactory characterClassFactory = new CharacterClassFactory(true, true);
//
//		IParseTable parseTable = new ParseTableReader(characterClassFactory, actionsFactory, stateFactory)
//				.read(testSetReader.getParseTableTerm());
//
//		ParseTableVariant bestParseTableVariant = new ParseTableVariant(
//				ActionsForCharacterRepresentation.DisjointSorted, ProductionToGotoRepresentation.JavaHashMap);
//		Variant variant = new Variant(bestParseTableVariant,
//				new ParserVariant(ActiveStacksRepresentation.ArrayList, ForActorStacksRepresentation.ArrayDeque,
//						ParseForestRepresentation.Hybrid, ParseForestConstruction.Optimized,
//						StackRepresentation.HybridElkhound, Reducing.Elkhound));
//
//		parser = JSGLR2Variants.getParser(parseTable, variant.parser);
//		jsglr2 = JSGLR2Variants.getJSGLR2(parseTable, variant.parser);
//
//		for (Input input : inputs) {
//			// bh.consume(parser.parseUnsafe(input.content, input.filename, null));
//			bh.consume(jsglr2.parseUnsafe(input.content, input.filename, null));
//		}
	}

}
