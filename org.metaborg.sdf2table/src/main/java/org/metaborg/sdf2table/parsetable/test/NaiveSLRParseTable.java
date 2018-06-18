package org.metaborg.sdf2table.parsetable.test;

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
    	for(IProduction prod : productionsMapping.keySet()) {
    		Symbol s = prod.leftHand();
    		calculateSLRFirstSet(s);
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
    
    @Override
    public void calculateSLRFollowSets() {
    	for(IProduction prod : productionsMapping.keySet()) {
    		Symbol s = prod.leftHand();
    		calculateSLRFollowSet(s);
    	}
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