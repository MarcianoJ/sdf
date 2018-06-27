package org.metaborg.sdf2table.parsetable.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.testset.TestSet;
import org.spoofax.jsglr2.testset.TestSetReader;
import org.spoofax.terms.ParseError;

public class JSGLR2TestSetReader extends TestSetReader {
	
	public JSGLR2TestSetReader(TestSet testSet) {
		super(testSet);
	}
	
	private String basePath() {
        return "src/test/resources";
    }

    @Override
    public IStrategoTerm parseTableTerm(String filename) throws ParseError, IOException {
        InputStream inputStream = new FileInputStream(basePath() + "/generated/" + filename);

        return getTermReader().parseFromStream(inputStream);
    }

    @Override
    public String grammarsPath() {
        return basePath() + "/grammars";
    }

    @Override
    public void setupParseTableFile(String parseTableName) throws IOException {
//        File file = new File(basePath() + "/parsetables");
//        file.mkdirs();
//        
//        InputStream defResourceInJar = getClass().getResourceAsStream("/" + parseTableName + ".tbl");
//        String destinationInTargetDir = basePath() + "/parsetables/" + parseTableName + ".tbl";
//
//        Files.copy(defResourceInJar, Paths.get(destinationInTargetDir), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void setupDefFile(String grammarName) throws IOException {
//        new File(basePath() + "/grammars").mkdirs();
//
//        InputStream defResourceInJar = getClass().getResourceAsStream("/grammars/" + grammarName + ".def");
//        String destinationInTargetDir = basePath() + "/grammars/" + grammarName + ".def";
//
//        Files.copy(defResourceInJar, Paths.get(destinationInTargetDir), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    protected String getFileAsString(String filename) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/samples/" + filename);

        return inputStreamAsString(inputStream);
    }
}