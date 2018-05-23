package org.metaborg.sdf2table.tests.lr_table;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.metaborg.characterclasses.CharacterClassFactory;
import org.metaborg.sdf2table.grammar.CharacterClass;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.grammar.Production;
import org.metaborg.sdf2table.io.GrammarReader;
import org.metaborg.sdf2table.io.ParseTableGenerator;
import org.metaborg.sdf2table.parsetable.LabelFactory;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableGenType;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.operations.FileOperations;

import org.apache.commons.vfs2.VFS;;

public class LRTableTest {
	
	public LRTableTest() {
		//setup
	}
	
	@Test
    public void LR0Test() throws Exception {
		String resourcesPath = "src/test/resources/";
		//String grammarName = "sum-nonambiguous";
		String grammarName = "helloworld5";
		
		String tableFilePath = resourcesPath + "GreenMarl.tbl";
		
		File tableFile = new File(resourcesPath + "GreenMarl.tbl");
		File inputFile = new File(resourcesPath + grammarName + "_in.aterm");
		File outputFile = new File(resourcesPath + grammarName + "_out.aterm");
		File outputNormGrammarFile = new File(resourcesPath + grammarName + ".xx");
		File outputContextGrammarFile = new File(resourcesPath + grammarName + ".yy");
		
//		System.out.println(inputFile.getAbsolutePath());
//		System.out.println(inputFile.exists());
//		System.out.println(inputFile.isFile());
//		System.out.println(inputFile.isDirectory());
		assert(inputFile.exists() && !inputFile.isDirectory());
		
//		CharacterClass range = new CharacterClass(new CharacterClassFactory(true, true).fromSingle(99));
//		CharacterClass single = new CharacterClass(new CharacterClassFactory(true, true).fromRange(99, 102));
//		
//		CharacterClass res = range.difference(single);
		
		
		ParseTableGenerator ptGen = new ParseTableGenerator(inputFile, outputFile,
				outputNormGrammarFile, outputContextGrammarFile, new ArrayList<String>(), ParseTableGenType.LR, 0);
		
		ptGen.outputTable(false, true, true);
		
//		ParseTable pt = ptGen.getParseTable();
//		
//		Production prod = (Production) pt.initialProduction();
		
//		System.out.println(tableFile.toURI());
//		
//		FileObject fileObject = VFS.getManager().resolveFile("/home/marciano/git/sdf/org.metaborg.sdf2table/src/test/resources/GreenMarl.tbl");
//		System.out.println(fileObject.getPublicURIString());
//		ParseTableGenerator ptGen = new ParseTableGenerator(fileObject);
		
		NormGrammar grammar = new GrammarReader().readGrammar(inputFile, new ArrayList<String>());
		
//		System.out.println(ptGen.toString());
//		
//		LabelFactory lf = new LabelFactory(257);
//		
//		for(int i = 0; i < 10; i++) {System.out.println(lf.getNextLabel());}
//		ptGen.createParseTable(true, true, true);
//		
//		
//		ptGen.outputTable(false, false, false);
		
//		ParseTable pt = ptGen.getParseTable();
		
//		System.out.println(pt.getProdLabelFactory());
		
		
		
	}
}