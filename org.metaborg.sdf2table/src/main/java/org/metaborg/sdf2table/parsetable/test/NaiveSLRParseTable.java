package org.metaborg.sdf2table.parsetable.test;

import org.metaborg.characterclasses.CharacterClassFactory;
import org.metaborg.characterclasses.CharacterClassSingle;
import org.metaborg.sdf2table.grammar.CharacterClass;
import org.metaborg.sdf2table.grammar.IProduction;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.grammar.Symbol;
import org.metaborg.sdf2table.parsetable.SLRParseTable;

import org.metaborg.sdf2table.parsetable.ParseTableGenType;


// ParseTable class for SLR(1) with naive first/follow implementation
public class NaiveSLRParseTable extends SLRParseTable {

	private static final long serialVersionUID = -5059636749895036064L;

	public NaiveSLRParseTable(NormGrammar grammar, boolean dynamic, boolean dataDependent, boolean solveDeepConflicts,
			ParseTableGenType parseType, int k) {
		
		super(grammar, dynamic, dataDependent, solveDeepConflicts, parseType, k);
	}
	
	// Calculates firsts sets for symbols
    public void calculateSLRFirstSets() {
//    	for(IProduction prod : productionsMapping.keySet()) {
//    		Symbol s = prod.leftHand();
//    		calculateSLRFirstSet(s);
//    	}
    	calculateSLRFirstFollowSets();
    }
    
    public void calculateSLRFirstFollowSets() {
    	for(int i = 0; i <= 256; i++) {
			CharacterClass ccSingle = new CharacterClass(new CharacterClassSingle(i));
			CharacterClass ccEmpty = (CharacterClass) new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
			firstSetsSLR.put(ccSingle, ccSingle);
			followSetsSLR.put(ccSingle, ccEmpty);
		}
    	
    	boolean allComplete = false;
    	
    	while(allComplete == false) {
    		allComplete = true;
	    	for(IProduction prod : productionsMapping.keySet()) {
	    		int k = prod.rightHand().size()-1;
	    		
	    		// Production X -> Y_0 Y_1...Y_k
	    		Symbol x = prod.leftHand();
		    	for(int i = 0; i <= k; i++) {
		    		Symbol y_i = prod.rightHand().get(i);
		    		
		    		boolean allNullableBeforeI = true;
	    			for(int a = 0; a < i; a++) {
	    				Symbol sBefore = prod.rightHand().get(a);
	    				if(!(sBefore.isNullable() || i == 1)) {
	    					allNullableBeforeI = false;
	    				}
	    			}
	    			if(allNullableBeforeI) {
	    				CharacterClass union = CharacterClass.union(getFirst(x), getFirst(y_i));
	    				if(!getFirst(x).equals(union)) {
	    					//addFirst(x, getFirst(y_i));
	    					addFirst(x, union);
		    				allComplete = false;
	    				}
	    			}
	    			
	    			boolean allNullableAfterI = true;
	    			for(int b = i+1; b <= k; b++) {
	    				Symbol sAfter = prod.rightHand().get(b);
	    				if(!(sAfter.isNullable() || i == k)) {
	    					allNullableAfterI = false;
	    				}
	    			}
	    			if(allNullableAfterI) {
	    				CharacterClass union = CharacterClass.union(getFollow(y_i), getFollow(x));
	    				if(!getFollow(y_i).equals(union)) {
	    					//addFollow(y_i, getFollow(x));
	    					addFollow(y_i, union);
		    				allComplete = false;
	    				}
	    			}
	    			
	    			for(int j = i+1; j <= k; j++) {
	    				Symbol y_j = prod.rightHand().get(j);
	    				boolean allNullableBetweenIandJ = true;
	    				for(int c = i+1; c <= j-1; c++) {
	        				Symbol sBetween = prod.rightHand().get(c);
	        				if(!(sBetween.isNullable() || i+1 == j)) {
	        					allNullableBetweenIandJ = false;
	        				}
	        			}
	        			if(allNullableBetweenIandJ) {
	        				CharacterClass union = CharacterClass.union(getFollow(y_i), getFirst(y_j));
	        				if(!getFollow(y_i).equals(union)) {
	        					//addFollow(y_i, getFirst(y_j));
	        					addFollow(y_i, union);
		        				allComplete = false;
	        				}
	        			}
	    			}
	    			
		    	}
	    	}
    	}
    }
    
    public CharacterClass getFirst(Symbol s) {
    	CharacterClass result = null;
    	if(s instanceof CharacterClass) {
			CharacterClass cc = (CharacterClass) s;
			CharacterClass union = (CharacterClass) new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
			for(int i = cc.min(); i <= cc.max(); i++) {
				if(cc.contains(i)) {
					CharacterClass ccSingle = new CharacterClass(new CharacterClassSingle(i));
					union = CharacterClass.union(union, firstSetsSLR.get(ccSingle));
				}
			}
			result = union;
		} else {
			result = firstSetsSLR.get(s);
    	}
    	return result;
    }
    
    public void addFirst(Symbol s, CharacterClass ccAdd) {
    	if(s instanceof CharacterClass) {
    		CharacterClass cc = (CharacterClass) s;
			for(int i = cc.min(); i <= cc.max(); i++) {
				if(cc.contains(i)) {
					CharacterClass ccSingle = new CharacterClass(new CharacterClassSingle(i));
					firstSetsSLR.put(ccSingle, ccAdd);
				}
			}
    	} else {
    		firstSetsSLR.put(s, ccAdd);
    	}
    }
    
    
    public CharacterClass getFollow(Symbol s) {
    	CharacterClass result = null;
    	if(s instanceof CharacterClass) {
			CharacterClass cc = (CharacterClass) s;
			CharacterClass union = (CharacterClass) new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
			for(int i = cc.min(); i <= cc.max(); i++) {
				if(cc.contains(i)) {
					CharacterClass ccSingle = new CharacterClass(new CharacterClassSingle(i));
					union = CharacterClass.union(union, followSetsSLR.get(ccSingle));
				}
			}
			result = union;
		} else {
			result = followSetsSLR.get(s);
    	}
    	return result;
    }
    
    public void addFollow(Symbol s, CharacterClass ccAdd) {
    	if(s instanceof CharacterClass) {
    		CharacterClass cc = (CharacterClass) s;
			for(int i = cc.min(); i <= cc.max(); i++) {
				if(cc.contains(i)) {
					CharacterClass ccSingle = new CharacterClass(new CharacterClassSingle(i));
					followSetsSLR.put(ccSingle, ccAdd);
				}
			}
    	} else {
    		followSetsSLR.put(s, ccAdd);
    	}
    }
	
	// Calculates all first characters of symbol s, following naive algorithm
    public void calculateSLRFirstSet(Symbol s) {
    	boolean allComplete = false;
    	
    	// Iterate until the sets stop changing
    	while(allComplete == false) {
    		allComplete = true;
	    	for(IProduction prod : productionsMapping.keySet()) {
	    		int i = 0;
	    		if(prod.rightHand().size() > 0) {
		    		Symbol rhsSymbol = prod.rightHand().get(0);
		    		
		    		// Look at the first RHS symbol, and the next one if the previous one is nullable, etc.
		    		while(i < prod.rightHand().size() && (i == 0 || rhsSymbol.isNullable())) {
		    			rhsSymbol = prod.rightHand().get(i);
		    			if(s.equals(prod.leftHand())) {
		    				// If first RHS symbol is terminal, add to first set, else, add first set of rhsSymbol to first set of s
		    				if(rhsSymbol instanceof CharacterClass) {
		    					CharacterClass cc = (CharacterClass) rhsSymbol;
		    					firstSetsSLR.put(s, CharacterClass.union(firstSetsSLR.get(s), cc));
		    				} else {
		    					CharacterClass union = CharacterClass.union(firstSetsSLR.get(s), firstSetsSLR.get(rhsSymbol));
		    					if(!firstSetsSLR.get(s).equals(union)) {
		    						firstSetsSLR.put(s, union);
		    						allComplete = false;
		    					}
		    				}
		    			}
		    			i++;
		    		}
	    		}
	    	}
    	}
    }
    
    @Override
    public void calculateSLRFollowSets() {
//    	for(IProduction prod : productionsMapping.keySet()) {
//    		Symbol s = prod.leftHand();
//    		calculateSLRFollowSet(s);
//    	}
    }
    
    // Calculates all first set characters of symbol s, following naive algorithm
    public void calculateSLRFollowSet(Symbol s) {
    	boolean allComplete = false;
    	
    	// Iterate until the sets stop changing
    	while(allComplete == false) {
    		allComplete = true;
    		
	    	// Derive follow set of s by finding s in each RHS of productions
	    	for(IProduction prod : productionsMapping.keySet()) {
	    		for(int sIndex = 0; sIndex < prod.rightHand().size(); sIndex++) {
		    		if(prod.rightHand().get(sIndex).equals(s)) {
		    			// If production has format B -> alpha A, then add follow(B) to follow(A)
		    			if(sIndex == prod.rightHand().size()-1) {
		    				Symbol sLHS = prod.leftHand();
		    				CharacterClass union = CharacterClass.union(followSetsSLR.get(s), followSetsSLR.get(sLHS));
		    				if(!followSetsSLR.get(s).equals(union)) {
		    					followSetsSLR.put(s, union);
		    					allComplete = false;
		    				}
		    			} else {
			    			int i = 1;
			        		Symbol sNext = prod.rightHand().get(sIndex+i);
			        		
			        		// Look at the first RHS symbol that comes after s, and the next one if the previous one is nullable, etc.
			        		while(sNext != null && sIndex+i < prod.rightHand().size() && (i == 1 || sNext.isNullable())) {
			            		sNext = prod.rightHand().get(sIndex+i);
			            		// If production has format B -> alpha A beta, then add first(beta) to follow(A)
			    				if (sNext instanceof CharacterClass) {
				    				CharacterClass ccNext = (CharacterClass) sNext;
				    				followSetsSLR.put(s, CharacterClass.union(followSetsSLR.get(s), ccNext));
			    				} else {
			    					followSetsSLR.put(s, CharacterClass.union(followSetsSLR.get(s), firstSetsSLR.get(sNext)));
			    				}
			    				i++;
			    			}
		    			}
		    		}
	    		}
	    	}
    	}
    }
}