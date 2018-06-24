package org.metaborg.sdf2table.parsetable.test;

import java.util.Map;

import org.metaborg.sdf2table.grammar.CharacterClass;
import org.metaborg.sdf2table.grammar.IProduction;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.grammar.Symbol;
import org.metaborg.sdf2table.parsetable.LRItem;
import org.metaborg.sdf2table.parsetable.LRParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableGenType;
import org.metaborg.sdf2table.parsetable.State;


// ParseTable class for LR(0) and LR(1) with naive first/follow implementation
public class NaiveLRParseTable extends LRParseTable {

	private static final long serialVersionUID = -5059636749895036064L;

	public NaiveLRParseTable(NormGrammar grammar, boolean dynamic, boolean dataDependent, boolean solveDeepConflicts,
			ParseTableGenType parseType, int k) {
		
		super(grammar, dynamic, dataDependent, solveDeepConflicts, parseType, k);
	}

	// Calculates follow sets for symbols for this state
	@Override
    public void calculateLRFirstSets(State state) {
    	prepareLRFirstSets(state);
    	
    	// Calculate first sets for each symbol in state
    	for(LRItem item : state.getItems()) {
    		IProduction prod = item.getProd();
    		Symbol s = prod.leftHand();
    		calculateLRFirstSet(state, s);
    	}
    }
	
	// Calculates all first set characters of symbol s, following naive algorithm
    public void calculateLRFirstSet(State state, Symbol s) {
    	Map<Symbol, CharacterClass> stateFirstSets = firstSetsLR.get(state);
    	boolean allComplete = false;
    	
    	while(allComplete == false) {
    		allComplete = true;
    		
	    	for(LRItem item : state.getItems()) {
	    		int i = 0;
	    		IProduction prod = item.getProd();
	    		Symbol rhsSymbol = prod.rightHand().get(0);
	    		
	    		// Look at the first RHS symbol, and the next one if the previous one is nullable, etc.
	    		while(i < prod.rightHand().size() && (i == 0 || rhsSymbol.isNullable())) {
	    			rhsSymbol = prod.rightHand().get(i);
	    			if(s.equals(prod.leftHand())) {
	    				// If first RHS symbol is terminal, add to first set, else, add first set of rhsSymbol to first set of s
	    				if(rhsSymbol instanceof CharacterClass) {
	    					CharacterClass cc = (CharacterClass) rhsSymbol;
	    					stateFirstSets.put(s, CharacterClass.union(stateFirstSets.get(s), cc));
	    				} else {
	    					CharacterClass union = CharacterClass.union(stateFirstSets.get(s), stateFirstSets.get(rhsSymbol));
	    					if(!stateFirstSets.get(s).equals(union)) {
	    						stateFirstSets.put(s, union);
	    						allComplete = false;
	    					}
	    				}	
	    			}
	    			i++;
	    		}
	    	}
    	}
    }
	
	
	
	// Calculates follow sets for this state
	@Override
    public void calculateLRFollowSets(State state) {
    	prepareLRFollowSets(state);
    	
    	// Calculate follow sets for each symbol in state
    	for(LRItem item : state.getItems()) {
    		IProduction prod = item.getProd();
    		Symbol s = prod.leftHand();
    		calculateLRFollowSet(state, s);
    	}
    }
	
	// Calculates all follow set characters of symbol s, following naive algorithm
    public void calculateLRFollowSet(State state, Symbol s) {
    	Map<Symbol, CharacterClass> stateFirstSets = firstSetsLR.get(state);
    	Map<Symbol, CharacterClass> stateFollowSets = followSetsLR.get(state);
    	boolean allComplete = false;
    	
    	// Iterate until the sets stop changing
    	while(allComplete == false) {
    		allComplete = true;
	    	// Derive follow set of s by finding s in each RHS of productions
	    	for(LRItem item : state.getItems()) {
	    		IProduction prod = item.getProd();
	    		int sIndex = item.getDotPosition();
	
	    		if(sIndex < prod.rightHand().size() && prod.rightHand().get(sIndex).equals(s)) {
	    			// If production has format B -> alpha A, then add follow(B) to follow(A)
	    			if(sIndex == prod.rightHand().size()-1) {
	    				Symbol sLHS = prod.leftHand();
	    				CharacterClass union = CharacterClass.union(stateFollowSets.get(s), stateFollowSets.get(sLHS));
	    				if(!stateFollowSets.get(s).equals(union)) {
	    					stateFollowSets.put(s, union);
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
			    				stateFollowSets.put(s, CharacterClass.union(stateFollowSets.get(s), ccNext));
		    				} else {
		    					stateFollowSets.put(s, CharacterClass.union(stateFollowSets.get(s), stateFirstSets.get(sNext)));
		    				}
		    				i++;
		    			}
	    			}
	    		}
	    	}
    	}
    }
	
}