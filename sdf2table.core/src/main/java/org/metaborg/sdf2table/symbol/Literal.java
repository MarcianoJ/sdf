package org.metaborg.sdf2table.symbol;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.StrategoConstructor;
import org.spoofax.terms.StrategoString;

public class Literal extends ConcreteNonTerminal{
	private static final StrategoConstructor CONS_LIT = new StrategoConstructor("lit", 1);
	
	private String _value;
	
	@Override
	public Type type(){
		return Type.LITERAL;
	}
	
	@Override
	public boolean nonEpsilon(){
		return true;
	}
	
	@Override
	public boolean isLayout(){
		return false;
	}
	
	public Literal(String value){
		super();
		_value = value;
	}
	
	public String getValue(){
		return _value;
	}
	
	@Override
	public String toString(){
		return "\""+_value+"\"";
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof Literal){
			Literal l = (Literal)other;
			return other != null && l.getValue().equals(_value);
		}
		return false;
	}
	
	public IStrategoTerm toATerm(){
		return new StrategoAppl(CONS_LIT, new IStrategoTerm[]{new StrategoString(_value, null, 0)}, null, 0);
	}
}
