package org.metaborg.newsdf2table.grammar;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.Sets;

public class Sort extends Symbol {

    String name;
    LiteralType type;

    public Sort(String name) {
        this.name = name;
        this.type = null;
        followRestrictions = Sets.newHashSet();
    }

    public Sort(String name, LiteralType type) {
        this.name = name;
        this.type = type;
        followRestrictions = Sets.newHashSet();
    }

    @Override public String name() {
        if(type == null)
            return name;
        else if(type == LiteralType.CiLit)
            return "'" + name + "'";
        else
            return "\"" + name + "\"";
    }

    @Override public IStrategoTerm toAterm(ITermFactory tf) {
        if(type == LiteralType.CiLit) {
            return tf.makeAppl(tf.makeConstructor("ci-lit", 1),
                tf.makeString(name.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\'", "\'")));
        } else if(type == LiteralType.Lit) {
            return tf.makeAppl(tf.makeConstructor("lit", 1),
                tf.makeString(name.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\'", "\'")));
        }
        return tf.makeAppl(tf.makeConstructor("sort", 1), tf.makeString(name));
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(!super.equals(obj))
            return false;
        if(getClass() != obj.getClass())
            return false;
        Sort other = (Sort) obj;
        if(name == null) {
            if(other.name != null)
                return false;
        } else if(!name.equals(other.name))
            return false;
        if(type != other.type)
            return false;
        return true;
    }
}