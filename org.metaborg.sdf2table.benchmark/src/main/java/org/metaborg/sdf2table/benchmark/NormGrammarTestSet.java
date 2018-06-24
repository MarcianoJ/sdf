package org.metaborg.sdf2table.benchmark;

import org.spoofax.jsglr2.testset.TestSet;
import org.spoofax.jsglr2.testset.TestSetInput;
import org.spoofax.jsglr2.testset.TestSetParseTable;
import org.spoofax.jsglr2.testset.TestSetParseTableFromATerm;
import org.spoofax.jsglr2.testset.TestSetSingleInput;

public class NormGrammarTestSet extends TestSet {
	
	public NormGrammarTestSet(String name, TestSetParseTable parseTable, TestSetInput input) {
		super(name, parseTable, input);
	}
	
	protected static TestSet testSet = new TestSet("greenmarl", new TestSetParseTableFromATerm("GreenMarl"),
	        new TestSetSingleInput("GreenMarl/infomap.gm"));
}