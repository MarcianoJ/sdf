package org.metaborg.newsdf2table.grammar;

import java.util.Map;
import java.util.Set;

import org.metaborg.newsdf2table.parsetable.Context;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.Sets;

public class StartSymbol extends Symbol {

    private static final long serialVersionUID = -1033671297813111213L;

    public StartSymbol() {
        followRestrictions = Sets.newHashSet();
    }

    @Override public String name() {
        return "<START>";
    }

    @Override public String toString() {
        return name();
    }

    @Override public IStrategoTerm toAterm(ITermFactory tf) {
        return tf.makeAppl(tf.makeConstructor("sort", 1), tf.makeString(name()));
    }

    @Override public IStrategoTerm toSDF3Aterm(ITermFactory tf,
        Map<Set<Context>, Integer> ctx_vals, Integer ctx_val) {
        return tf.makeAppl(tf.makeConstructor("Sort", 1), tf.makeString(name()));
    }

    @Override public int hashCode() {
        return "<START>".hashCode();
    }
    
    @Override public boolean equals(Object s) {
        if(s == null)
            return false;
        return(s instanceof StartSymbol);
    }
}
