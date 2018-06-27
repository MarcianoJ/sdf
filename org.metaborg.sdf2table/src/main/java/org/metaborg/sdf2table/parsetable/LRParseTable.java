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


// ParseTable class for LR(0) and LR(1)
public class LRParseTable extends ParseTable {

	private static final long serialVersionUID = -5059636749895036064L;
	
	// Maps symbols to first and follow sets and symbols to dependent symbols for each state
    protected Map<State, Map<Symbol, CharacterClass>> firstSetsLR;
    protected Map<State, SetMultimap<Symbol, Symbol>> firstSetDependenciesLR;
    protected Map<State, Map<Symbol, CharacterClass>> followSetsLR;
    protected Map<State, SetMultimap<Symbol, Symbol>> followSetDependenciesLR;
    
    // Stores symbols visited and complete for first and follow sets for each state, used by calculateSetDependencies()
    protected Map<State, Map<Symbol, Map<Integer, Boolean>>> firstSetsVisitedLR;
    protected Map<State, Map<Symbol, Boolean>> firstSetsCompleteLR;
    protected Map<State, Map<Symbol, Map<Integer, Boolean>>> followSetsVisitedLR;
    protected Map<State, Map<Symbol, Boolean>> followSetsCompleteLR;
    

	public LRParseTable(NormGrammar grammar, boolean dynamic, boolean dataDependent, boolean solveDeepConflicts,
			ParseTableGenType parseType, int k) {
		
		super(grammar, dynamic, dataDependent, solveDeepConflicts, parseType, k);
	}
	
	@Override
	protected void initVariables() {
		firstSetsLR = Maps.newLinkedHashMap();
        firstSetDependenciesLR = Maps.newLinkedHashMap();
        followSetsLR = Maps.newLinkedHashMap();
        followSetDependenciesLR = Maps.newLinkedHashMap();

        firstSetsVisitedLR = Maps.newLinkedHashMap();
        firstSetsCompleteLR = Maps.newLinkedHashMap();
        followSetsVisitedLR = Maps.newLinkedHashMap();
        followSetsCompleteLR = Maps.newLinkedHashMap();
	}
	
	@Override
	protected void initProcessing() {
        State s0 = new State(initialProduction, this);
        stateQueue.add(s0);
        processStateQueue();
    }
	
	@Override
	public void prepareState(State state) {
        state.closure();
        if(kLookahead > 0) {
        	calculateLRFirstSets(state);
        	calculateLRFollowSets(state);
        	applyFollowSets(state);
        }
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
    	
    	if (kLookahead == 0) {
    		firstList.add(CharacterClass.getFullCharacterClass());
    	} else {
    		firstList.add(firstSetsLR.get(state).get(s));
    	}
    	return firstList;
    }
    
	@Override
    public List<ICharacterClass> getFollowSet(State state, Symbol s) {
    	List<ICharacterClass> followList = new ArrayList<ICharacterClass>();
    	
    	if (kLookahead == 0) {
    		followList.add(CharacterClass.getFullCharacterClass());
    	} else {
    		followList.add(followSetsLR.get(state).get(s));
    	}
    	return followList;
    }
    
    // Calculates follow sets for symbols for this state
    public void calculateLRFirstSets(State state) {
    	prepareLRFirstSets(state);
    	
    	Map<Symbol, CharacterClass> stateFirstSets = firstSetsLR.get(state);
    	SetMultimap<Symbol, Symbol> stateFirstSetDependencies = firstSetDependenciesLR.get(state);
    	Map<Symbol, Boolean> stateSymbolsComplete = firstSetsCompleteLR.get(state);
    	Map<Symbol, Map<Integer, Boolean>> stateSymbolsVisited = firstSetsVisitedLR.get(state);
    	
    	// Calculate first sets for each symbol in state
    	for(LRItem item : state.getItems()) {
    		IProduction prod = item.getProd();
    		Symbol s = prod.leftHand();
    		calculateLRFirstSetInitial(state, s);
    	}
    	for(LRItem item : state.getItems()) {
    		IProduction prod = item.getProd();
    		Symbol s = prod.leftHand();
    		
    		calculateSetDependencies(s, stateFirstSets, stateFirstSetDependencies, stateSymbolsVisited, stateSymbolsComplete);
    	}
    }
    
    public void prepareLRFirstSets(State state) {
    	Map<Symbol, CharacterClass> stateFirstSets = firstSetsLR.get(state);
    	SetMultimap<Symbol, Symbol> stateFirstSetDependencies = firstSetDependenciesLR.get(state);
    	Map<Symbol, Boolean> stateSymbolsComplete = firstSetsCompleteLR.get(state);
    	Map<Symbol, Map<Integer, Boolean>> stateSymbolsVisited = Maps.newLinkedHashMap();
    	
    	if(stateFirstSets == null) {
    		stateFirstSets = Maps.newLinkedHashMap();
    		firstSetsLR.put(state, stateFirstSets);
    	}
    	if(stateFirstSetDependencies == null) {
    		stateFirstSetDependencies = HashMultimap.create();
    		firstSetDependenciesLR.put(state, stateFirstSetDependencies);
    	}
    	if(stateSymbolsComplete == null) {
    		stateSymbolsComplete = Maps.newLinkedHashMap();
    		firstSetsCompleteLR.put(state, stateSymbolsComplete);
    	}
    	
    	Multiset<Symbol> allSymbols = normalizedGrammar().getSymbolProductionsMapping().keys();
    	for(Symbol s : allSymbols) {
    		if(stateFirstSets.get(s) == null) {
    			CharacterClass cc = new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
    			stateFirstSets.put(s, cc);
    		}
    		stateSymbolsVisited.put(s, Maps.newLinkedHashMap());    
    	}
    	
    	firstSetsVisitedLR.put(state, stateSymbolsVisited);
    }
    
    // Calculates the immediately derivable first set characters of symbol s and maps first set dependencies
    public void calculateLRFirstSetInitial(State state, Symbol s) {
    	Map<Symbol, CharacterClass> stateFirstSets = firstSetsLR.get(state);
    	SetMultimap<Symbol, Symbol> stateFirstSetDependencies = firstSetDependenciesLR.get(state);
    	
    	for(LRItem item : state.getItems()) {
    		int i = 0;
    		IProduction prod = item.getProd();
    		if(prod.rightHand().size() > 0) {
	    		Symbol rhsSymbol = prod.rightHand().get(0);
	    		
	    		// Look at the first RHS symbol, and the next one if the previous one is nullable, etc.
	    		while(i < prod.rightHand().size() && (i == 0 || rhsSymbol.isNullable())) {
	    			rhsSymbol = prod.rightHand().get(i);
	    			if(s.equals(prod.leftHand())) {
	    				// If first RHS symbol is terminal, add to first set, else, create dependency
	    				if(rhsSymbol instanceof CharacterClass) {
	    					CharacterClass cc = (CharacterClass) rhsSymbol;
	    					stateFirstSets.put(s, CharacterClass.union(stateFirstSets.get(s), cc));
	    				} else {
	    					stateFirstSetDependencies.put(s, rhsSymbol);
	    				}	
	    			}
	    			i++;
	    		}
    		}
    	}
    }
    
    
    // Calculates follow sets for this state
    public void calculateLRFollowSets(State state) {
    	prepareLRFollowSets(state);
    	
    	Map<Symbol, CharacterClass> stateFollowSets = followSetsLR.get(state);
    	SetMultimap<Symbol, Symbol> stateFollowSetDependencies = followSetDependenciesLR.get(state);
    	Map<Symbol, Boolean> stateSymbolsComplete = followSetsCompleteLR.get(state);
    	Map<Symbol, Map<Integer, Boolean>> stateSymbolsVisited = followSetsVisitedLR.get(state);
    	
    	
    	// Calculate follow sets for each symbol in state
    	for(LRItem item : state.getItems()) {
    		IProduction prod = item.getProd();
    		Symbol s = prod.leftHand();
    		calculateLRFollowSetInitial(state, s);
    	}
    	for(LRItem item : state.getItems()) {
    		IProduction prod = item.getProd();
    		Symbol s = prod.leftHand();
    		calculateSetDependencies(s, stateFollowSets, stateFollowSetDependencies, stateSymbolsVisited, stateSymbolsComplete);
    	}
    }
    
    public void prepareLRFollowSets(State state) {
    	Map<Symbol, CharacterClass> stateFollowSets = followSetsLR.get(state);
    	SetMultimap<Symbol, Symbol> stateFollowSetDependencies = followSetDependenciesLR.get(state);
    	Map<Symbol, Boolean> stateSymbolsComplete = followSetsCompleteLR.get(state);
    	Map<Symbol, Map<Integer, Boolean>> stateSymbolsVisited = Maps.newLinkedHashMap();
    	
    	if(stateFollowSets == null) {
    		stateFollowSets = Maps.newLinkedHashMap();
    		followSetsLR.put(state, stateFollowSets);
    	}
    	if(stateFollowSetDependencies == null) {
    		stateFollowSetDependencies = HashMultimap.create();
    		followSetDependenciesLR.put(state, stateFollowSetDependencies);
    	}
    	if(stateSymbolsComplete == null) {
    		stateSymbolsComplete = Maps.newLinkedHashMap();
    		followSetsCompleteLR.put(state, stateSymbolsComplete);
    	}
    	
    	for(LRItem item : state.getKernel()) {
    		IProduction prod = item.getProd();
    		Symbol s = prod.leftHand();
			CharacterClass itemLookahead = (CharacterClass) item.getLookahead().get(0);
			stateFollowSets.put(s, itemLookahead);
    	}
    	
    	Multiset<Symbol> allSymbols = normalizedGrammar().getSymbolProductionsMapping().keys();
    	for(Symbol s : allSymbols) {
    		if(stateFollowSets.get(s) == null) {
    			CharacterClass cc = new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
    			stateFollowSets.put(s, cc);
    		}
    		stateSymbolsVisited.put(s, Maps.newLinkedHashMap());
    	}
    	
    	followSetsVisitedLR.put(state, stateSymbolsVisited);
    }
    
    // Calculates the immediately derivable follow set characters of symbol s and maps follow set dependencies
    public void calculateLRFollowSetInitial(State state, Symbol s) {
    	Map<Symbol, CharacterClass> stateFirstSets = firstSetsLR.get(state);
    	Map<Symbol, CharacterClass> stateFollowSets = followSetsLR.get(state);
    	SetMultimap<Symbol, Symbol> stateFollowSetDependencies = followSetDependenciesLR.get(state);
    	
    	// Derive follow set of s by finding s in each RHS of productions
    	for(LRItem item : state.getItems()) {
    		IProduction prod = item.getProd();
    		int sIndex = item.getDotPosition();

    		if(sIndex < prod.rightHand().size() && prod.rightHand().get(sIndex).equals(s)) {
    			// If production has format B -> alpha A, then create dependency for follow(B) and follow(A)
    			if(sIndex == prod.rightHand().size()-1) {
    				Symbol sLHS = prod.leftHand();
    				stateFollowSetDependencies.put(s, sLHS);
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