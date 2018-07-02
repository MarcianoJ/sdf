package org.metaborg.sdf2table.parsetable;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.metaborg.characterclasses.CharacterClassFactory;
import org.metaborg.characterclasses.CharacterClassSingle;
import org.metaborg.parsetable.characterclasses.ICharacterClass;
import org.metaborg.sdf2table.grammar.CharacterClass;
import org.metaborg.sdf2table.grammar.IProduction;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.grammar.Symbol;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
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
    protected SetMultimap<Symbol, Symbol> followFirstSetDependenciesSLR;
    
    // Stores symbols visited and complete for first and follow sets, used by calculateSetDependencies()
    protected Map<Symbol, Map<Integer, Boolean>> firstSetsVisitedSLR;
    protected Map<Symbol, Boolean> firstSetsCompleteSLR;
    protected Map<Symbol, Map<Integer, Boolean>> followSetsVisitedSLR;
    protected Map<Symbol, Boolean> followSetsCompleteSLR;
    protected Map<Symbol, Map<Integer, Boolean>> followFirstSetsVisitedSLR;
    protected Map<Symbol, Boolean> followFirstSetsCompleteSLR;

    protected static int FIRST = 0;
    protected static int FOLLOWFIRST = 1;
    protected static int FOLLOW = 2;
    
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
        followFirstSetDependenciesSLR = HashMultimap.create();

        firstSetsVisitedSLR = Maps.newLinkedHashMap();
        firstSetsCompleteSLR = Maps.newLinkedHashMap();
        followSetsVisitedSLR = Maps.newLinkedHashMap();
        followSetsCompleteSLR = Maps.newLinkedHashMap();
        
        followFirstSetsVisitedSLR = Maps.newLinkedHashMap();
        followFirstSetsCompleteSLR = Maps.newLinkedHashMap();
        
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
				followFirstSetsVisitedSLR.put(s, Maps.newLinkedHashMap());
    		}
    	}
        
        for(Symbol s : allSymbols) {
        //for(IProduction prod : productionsMapping.keySet()) {
    		//Symbol s = prod.leftHand();
    		if(followFirstSetsVisitedSLR.get(s) == null) {
				followFirstSetsVisitedSLR.put(s, Maps.newLinkedHashMap());
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
//    	for(IProduction prod : productionsMapping.keySet()) {
//    		Symbol s = prod.leftHand();
//    		calculateSLRFirstSetInitial(s);
//    	}
//    	
//    	for(IProduction prod : productionsMapping.keySet()) {
//    		Symbol s = prod.leftHand();
//    		calculateSetDependencies(s, firstSetsSLR, firstSetDependenciesSLR, firstSetsVisitedSLR, firstSetsCompleteSLR);
//    	}
    	for(int i = 0; i <= 256; i++) {
			CharacterClass ccSingle = new CharacterClass(new CharacterClassSingle(i));
			CharacterClass ccEmpty = (CharacterClass) new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
			firstSetsSLR.put(ccSingle, ccSingle);
			followSetsSLR.put(ccSingle, ccEmpty);
			
			firstSetsVisitedSLR.put(ccSingle, Maps.newLinkedHashMap());
			followSetsVisitedSLR.put(ccSingle, Maps.newLinkedHashMap());
			followFirstSetsVisitedSLR.put(ccSingle, Maps.newLinkedHashMap());
		}
    	
    	calculateSLRFirstFollowSetsFirst();
    	
    	for(Symbol s : firstSetsVisitedSLR.keySet()) {
    		calculateSetDependencies(s, firstSetDependenciesSLR, firstSetsVisitedSLR, firstSetsCompleteSLR, FIRST);
    	}
    	
    	calculateSLRFirstFollowSetsFollowFirst();
    	for(Symbol s : followFirstSetsVisitedSLR.keySet()) {
    		calculateSetDependencies(s, followFirstSetDependenciesSLR, followFirstSetsVisitedSLR, followFirstSetsCompleteSLR, FOLLOWFIRST);
    	}
    	
    	calculateSLRFirstFollowSetsFollow();
    	System.out.println(followSetDependenciesSLR);
    	for(Symbol s : followSetsVisitedSLR.keySet()) {
    		calculateSetDependencies(s, followSetDependenciesSLR, followSetsVisitedSLR, followSetsCompleteSLR, FOLLOW);
    	}
    }
    
    public void calculateSLRFirstFollowSetsFirst() {
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
    				addDependency(firstSetDependenciesSLR, x, y_i);
//    				CharacterClass union = CharacterClass.union(getFirst(x), getFirst(y_i));
//    				if(!getFirst(x).equals(union)) {
//    					//addFirst(x, getFirst(y_i));
//    					addFirst(x, union);
//	    				allComplete = false;
//    				}
    			}
	    	}
    	}
    }
    
    public void calculateSLRFirstFollowSetsFollowFirst() {
    	for(IProduction prod : productionsMapping.keySet()) {
    		int k = prod.rightHand().size()-1;
    		
    		// Production X -> Y_0 Y_1...Y_k
    		Symbol x = prod.leftHand();
	    	for(int i = 0; i <= k; i++) {
	    		Symbol y_i = prod.rightHand().get(i);
	    		
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
        				addDependency(followFirstSetDependenciesSLR, y_i, y_j);
//        				CharacterClass union = CharacterClass.union(getFollow(y_i), getFirst(y_j));
//        				if(!getFollow(y_i).equals(union)) {
//        					//addFollow(y_i, getFirst(y_j));
//        					addFollow(y_i, union);
//	        				allComplete = false;
//        				}
        			}
    			}
	    	}
    	}
    }
    
    public void calculateSLRFirstFollowSetsFollow() {
    	for(IProduction prod : productionsMapping.keySet()) {
    		int k = prod.rightHand().size()-1;
    		
    		// Production X -> Y_0 Y_1...Y_k
    		Symbol x = prod.leftHand();
	    	for(int i = 0; i <= k; i++) {
	    		Symbol y_i = prod.rightHand().get(i);
	    		
	    		boolean allNullableAfterI = true;
    			for(int b = i+1; b <= k; b++) {
    				Symbol sAfter = prod.rightHand().get(b);
    				if(!(sAfter.isNullable() || i == k)) {
    					allNullableAfterI = false;
    				}
    			}
    			if(allNullableAfterI) {
    				addDependency(followSetDependenciesSLR, y_i, x);
//    				CharacterClass union = CharacterClass.union(getFollow(y_i), getFollow(x));
//    				if(!getFollow(y_i).equals(union)) {
//    					//addFollow(y_i, getFollow(x));
//    					addFollow(y_i, union);
//	    				allComplete = false;
//    				}
    			}
	    	}
    	}
    }
    
    

    
    public void addDependency(SetMultimap<Symbol, Symbol> dependencies, Symbol sSuper, Symbol sSub) {
    	Set<Symbol> sSupers = new HashSet<Symbol>();
    	Set<Symbol> sSubs = new HashSet<Symbol>();
    	
    	if(sSuper instanceof CharacterClass) {
    		CharacterClass cc = (CharacterClass) sSuper;
			for(int i = cc.min(); i <= cc.max(); i++) {
				if(cc.contains(i)) {
					CharacterClass ccSingle = new CharacterClass(new CharacterClassSingle(i));
					sSupers.add(ccSingle);
				}
			}
    	} else {
    		sSupers.add(sSuper);
    	}
    	
    	if(sSub instanceof CharacterClass) {
    		CharacterClass cc = (CharacterClass) sSub;
			for(int i = cc.min(); i <= cc.max(); i++) {
				if(cc.contains(i)) {
					CharacterClass ccSingle = new CharacterClass(new CharacterClassSingle(i));
					sSubs.add(ccSingle);
				}
			}
    	} else {
    		sSubs.add(sSub);
    	}
    	
    	for(Symbol sSupersElem : sSupers) {
    		for(Symbol sSubssElem : sSubs) {
    			dependencies.put(sSupersElem, sSubssElem);
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
    
    
    

    public void calculateSetDependencies(Symbol s,
    		SetMultimap<Symbol, Symbol> setDependencies, Map<Symbol, Map<Integer, Boolean>> symbolsVisited, Map<Symbol, Boolean> symbolsComplete, int phase) {
    	
    	// Only calculate if s has not been calculated before
    	if(!symbolsComplete.containsKey(s)) {
	    	boolean allComplete = false;
	    	int cycle = 0;
	    	
	    	// Keep doing passes until no changes are perceived
	    	while(allComplete == false) {
	    		allComplete = true;
	    		ArrayList<Symbol> visitedThisCycle = new ArrayList<Symbol>();
	    		visitedThisCycle.add(s);
	    		symbolsVisited.get(s).put(cycle, true);
	    		
		    	Set<Symbol> sDependencies = setDependencies.get(s);
		    	Deque<Symbol[]> stack = Lists.newLinkedList();
		    	for(Symbol sDependency : sDependencies) {
		    		stack.push(new Symbol[] {s, sDependency});
		    	}
		    	
		    	// Use stack for DFS through graph
		    	while(!stack.isEmpty()) {
		    		// sSuper is dependent on sSub
		    		Symbol[] symbols = stack.peek();
		    		Symbol sSuper = symbols[0];
		    		Symbol sSub = symbols[1];
		    		
		    		// Keep track of which symbols are visited to avoid infinite loops
		    		boolean sSubVisited = symbolsVisited.get(sSub).containsKey(cycle);
		    		boolean sSubComplete = symbolsComplete.containsKey(sSub);
		    		
		    		// If sSub is complete or has been visited this cycle, add set(sSub) to set(sSuper)
		    		// and pop [sSuper, sSub] from stack
		    		if (sSubComplete || sSubVisited) {
//		    			CharacterClass union = CharacterClass.union(sets.get(sSuper), sets.get(sSub));
//		    			if(!union.equals(sets.get(sSuper))) {	
//		    				sets.put(sSuper, union);
//		    				allComplete = false;
//		    			}

		    			if(phase == FIRST) {
		    				CharacterClass union = CharacterClass.union(getFirst(sSuper), getFirst(sSub));
		    				if(!getFirst(sSuper).equals(union)) {
		    					addFirst(sSuper, union);
			    				allComplete = false;
		    				}
		    			} else if(phase == FOLLOWFIRST) {
		    				CharacterClass union = CharacterClass.union(getFollow(sSuper), getFirst(sSub));
		    				if(!getFollow(sSuper).equals(union)) {
		    					addFollow(sSuper, union);
			    				allComplete = false;
		    				}
		    			} else {
		    				CharacterClass union = CharacterClass.union(getFollow(sSuper), getFollow(sSub));
		    				if(!getFollow(sSuper).equals(union)) {
		    					addFollow(sSuper, union);
			    				allComplete = false;
		    				}
		    			}
		    			stack.pop();
		    		// Else process sSub
		    		} else {
		                symbolsVisited.get(sSub).put(cycle, true);
		                visitedThisCycle.add(sSub);
		                
			    		// If there are non-processed nested dependencies, push them on the stack
			    		Set<Symbol> nestedDependencies = setDependencies.get(sSub);
		    			for(Symbol sNested : nestedDependencies) {
	                        // sSub is dependent on sNested
		    				boolean sNestedIsVisited = symbolsVisited.get(sNested).containsKey(cycle);
	                        boolean sNestedComplete = symbolsComplete.containsKey(sNested);
	                        
		    				if(!(sNestedComplete || sNestedIsVisited)) {
		    					stack.push(new Symbol[] {sSub, sNested});
		    				}
		    			}
		    		}
		    	}
		    	
		    	// If nothing changed this cycle, mark each visited symbol as complete
		    	if(allComplete) {
		    		for(Symbol sVisited : visitedThisCycle) {
		    			symbolsComplete.put(sVisited, true);
		    		}
		    	}
		    	cycle++;
	    	}
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
//    	for(IProduction prod : productionsMapping.keySet()) {
//    		Symbol s = prod.leftHand();
//    		calculateSLRFollowSetInitial(s);
//    	}
//    	
//    	for(IProduction prod : productionsMapping.keySet()) {
//    		Symbol s = prod.leftHand();
//    		calculateSetDependencies(s, followSetsSLR, followSetDependenciesSLR, followSetsVisitedSLR, followSetsCompleteSLR);
//    	}
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