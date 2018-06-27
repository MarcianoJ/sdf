package org.metaborg.sdf2table.parsetable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.metaborg.characterclasses.CharacterClassFactory;
import org.metaborg.parsetable.characterclasses.ICharacterClass;
import org.metaborg.sdf2table.grammar.CharacterClass;
import org.metaborg.sdf2table.grammar.IProduction;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.grammar.Symbol;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;

// Parse table class for SLR(1)
public class SLRParseTable extends ParseTable {

	private static final long serialVersionUID = -3925212050088208703L;
	
	// Maps symbols to first and follow sets and symbols to dependent symbols
    protected Map<Symbol, CharacterClass> firstSetsSLR;
    protected SetMultimap<Symbol, Symbol> firstSetDependenciesSLR;
    protected Map<Symbol, CharacterClass> followSetsSLR;
    protected SetMultimap<Symbol, Symbol> followSetDependenciesSLR;
    
    // Stores symbols visited and complete for first and follow sets, used by calculateSetDependencies()
    protected Map<Symbol, Map<Integer, Boolean>> firstSetsVisitedSLR;
    protected Map<Symbol, Boolean> firstSetsCompleteSLR;
    protected Map<Symbol, Map<Integer, Boolean>> followSetsVisitedSLR;
    protected Map<Symbol, Boolean> followSetsCompleteSLR;


	public SLRParseTable(NormGrammar grammar, boolean dynamic, boolean dataDependent, boolean solveDeepConflicts,
			ParseTableGenType parseType, int k) {
		
		super(grammar, dynamic, dataDependent, solveDeepConflicts, parseType, k);
	}
	
	@Override
	protected void initVariables() {
		// Init first/follow set variables
		firstSetsSLR = Maps.newLinkedHashMap();
        firstSetDependenciesSLR = HashMultimap.create();
        followSetsSLR = Maps.newLinkedHashMap();
        followSetDependenciesSLR = HashMultimap.create();

        firstSetsVisitedSLR = Maps.newLinkedHashMap();
        firstSetsCompleteSLR = Maps.newLinkedHashMap();
        followSetsVisitedSLR = Maps.newLinkedHashMap();
        followSetsCompleteSLR = Maps.newLinkedHashMap();
        
        // Fill first and follow sets with empty CharacterClasses
        Multiset<Symbol> allSymbols = normalizedGrammar().getSymbolProductionsMapping().keys();
        for(Symbol s : allSymbols) {
        //for(IProduction prod : productionsMapping.keySet()) {
    		//Symbol s = prod.leftHand();
    		if(firstSetsSLR.get(s) == null) {
    			CharacterClass cc = new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
				firstSetsSLR.put(s, cc);
                firstSetsVisitedSLR.put(s, Maps.newLinkedHashMap());
    		}
    	}
        
        for(Symbol s : allSymbols) {
        //for(IProduction prod : productionsMapping.keySet()) {
    		//Symbol s = prod.leftHand();
    		if(followSetsSLR.get(s) == null) {
    			CharacterClass cc = new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
				followSetsSLR.put(s, cc);
				followSetsVisitedSLR.put(s, Maps.newLinkedHashMap());
    		}
    	}
	}
	
	@Override
	protected void initProcessing() {
    	calculateSLRFirstSets();
    	calculateSLRFollowSets();
        
        State s0 = new State(initialProduction, this);
        stateQueue.add(s0);
        processStateQueue();
    }
	
	@Override
	public void prepareState(State state) {
        state.closure();
        applyFollowSets(state);
    }
	
	@Override
	public void processState(State state) {
        state.doShift(false);
        state.doReduces();
        state.calculateActionsForCharacter();
        state.setStatus(StateStatus.PROCESSED);
        setProcessedStates(getProcessedStates() + 1);
	}
	
	@Override
	public List<ICharacterClass> getFirstSet(State state, Symbol s) {
    	List<ICharacterClass> firstList = new ArrayList<ICharacterClass>();
    	
    	firstList.add(firstSetsSLR.get(s));
    	
    	return firstList;
    }
	
	@Override
	public List<ICharacterClass> getFollowSet(State state, Symbol s) {
    	List<ICharacterClass> followList = new ArrayList<ICharacterClass>();
    	
    	followList.add(followSetsSLR.get(s));
    	
    	return followList;
    }
     
    // Calculates firsts sets for symbols
    public void calculateSLRFirstSets() {
    	for(IProduction prod : productionsMapping.keySet()) {
    		Symbol s = prod.leftHand();
    		calculateSLRFirstSetInitial(s);
    	}
    	
    	for(IProduction prod : productionsMapping.keySet()) {
    		Symbol s = prod.leftHand();
    		calculateSetDependencies(s, firstSetsSLR, firstSetDependenciesSLR, firstSetsVisitedSLR, firstSetsCompleteSLR);
    	}
    }
    
    // Calculates the immediately derivable first characters of symbol s and maps first set dependencies
    public void calculateSLRFirstSetInitial(Symbol s) {
    	for(IProduction prod : productionsMapping.keySet()) {
    		int i = 0;
    		if(prod.rightHand().size() > 0) {
	    		Symbol rhsSymbol = prod.rightHand().get(0);
	    		
	    		// Look at the first RHS symbol, and the next one if the previous one is nullable, etc.
	    		while(i < prod.rightHand().size() && (i == 0 || rhsSymbol.isNullable())) {
	    			rhsSymbol = prod.rightHand().get(i);
	    			if(s.equals(prod.leftHand())) {
	    				// If first RHS symbol is terminal, add to first set, else, create dependency
	    				if(rhsSymbol instanceof CharacterClass) {
	    					CharacterClass cc = (CharacterClass) rhsSymbol;
	    					firstSetsSLR.put(s, CharacterClass.union(firstSetsSLR.get(s), cc));
	    				} else {
		    				firstSetDependenciesSLR.put(s, rhsSymbol);
	    				}	
	    			}
	    			i++;
	    		}
    		}
    	}
    }
    
    // Calculates follow sets for symbols
    public void calculateSLRFollowSets() {
    	for(IProduction prod : productionsMapping.keySet()) {
    		Symbol s = prod.leftHand();
    		calculateSLRFollowSetInitial(s);
    	}
    	
    	for(IProduction prod : productionsMapping.keySet()) {
    		Symbol s = prod.leftHand();
    		calculateSetDependencies(s, followSetsSLR, followSetDependenciesSLR, followSetsVisitedSLR, followSetsCompleteSLR);
    	}
    }
    
	// Calculates the immediately derivable first set characters of symbol s and maps first set dependencies
    public void calculateSLRFollowSetInitial(Symbol s) {
    	// Derive follow set of s by finding s in each RHS of productions
    	for(IProduction prod : productionsMapping.keySet()) {
    		for(int sIndex = 0; sIndex < prod.rightHand().size(); sIndex++) {
	    		if(prod.rightHand().get(sIndex).equals(s)) {
	    			// If production has format B -> alpha A, then create dependency for follow(B) and follow(A)
	    			if(sIndex == prod.rightHand().size()-1) {
	    				Symbol sLHS = prod.leftHand();
	    				followSetDependenciesSLR.put(s, sLHS);
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