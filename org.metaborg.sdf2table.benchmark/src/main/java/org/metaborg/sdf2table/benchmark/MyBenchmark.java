/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.metaborg.sdf2table.benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
import org.spoofax.jsglr2.testset.TestSetSingleInput;
import org.spoofax.jsglr2.testset.TestSetSizedInput;



@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class MyBenchmark extends BaseBenchmark {// extends JSGLR2BenchmarkParseTable {
	
	protected IParser<?, ?> parser; // Just parsing
    protected JSGLR2<?, ?> jsglr2; // Parsing and imploding (including tokenization)
    
    //protected TestSet testSet = TestSet.greenMarl;
    
    //protected static TestSet testSet = new TestSet("greenmarl", new TestSetParseTableFromATerm("GreenMarl"),
    //	        new TestSetSingleInput("GreenMarl/infomap.gm"));
    
    protected static TestSet testSet = new TestSet("helloworld6", new TestSetParseTableFromATerm("helloworld6"), 
    		new TestSetSingleInput("helloworld/test.txt"));
    		
//    		new TestSetSizedInput(n -> {
//        return String.join("", Collections.nCopies(n, "a"));
//    }, 7, 7, 7));
    
    public MyBenchmark() {
		super(testSet);
	}
	
    //@Param({ "7", "7", "7" }) public int n;
	
    
    @Benchmark
    public void testMethod(Blackhole bh) throws Exception {
    	String normGrammarPath = "grammars/";
    	String parseTablePath = "parsetables/";
    	String persistedObjectPath = "persisted_objects/";
    	
    	String grammarName = "helloworld6";
    	
    	String basePath = new File(BenchmarkTestsetReader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + "/classes/";
    	
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
        
        IParseTable parseTable = new ParseTableReader(characterClassFactory, actionsFactory, stateFactory).read(testSetReader.getParseTableTerm());
        
        ParseTableVariant bestParseTableVariant = new ParseTableVariant(ActionsForCharacterRepresentation.DisjointSorted, ProductionToGotoRepresentation.JavaHashMap);
		Variant variant = new Variant(bestParseTableVariant, new ParserVariant(ActiveStacksRepresentation.ArrayList, ForActorStacksRepresentation.ArrayDeque, ParseForestRepresentation.Hybrid, ParseForestConstruction.Optimized, StackRepresentation.HybridElkhound, Reducing.Elkhound));
		
		// -> BenchmarkTestsetReader 
				
		parser = JSGLR2Variants.getParser(parseTable, variant.parser);
        jsglr2 = JSGLR2Variants.getJSGLR2(parseTable, variant.parser);
        
        for(Input input : inputs) {
			bh.consume(parser.parseUnsafe(input.content, input.filename, null));
			//bh.consume(jsglr2.parseUnsafe(input.content, input.filename, null));
        }
    }

}






