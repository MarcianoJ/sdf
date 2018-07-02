package org.metaborg.sdf2table.parsetable.test;

import java.util.List;
import java.util.Map;

import org.metaborg.characterclasses.CharacterClassFactory;
import org.metaborg.characterclasses.CharacterClassSingle;
import org.metaborg.sdf2table.grammar.CharacterClass;
import org.metaborg.sdf2table.grammar.IProduction;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.grammar.Symbol;
import org.metaborg.sdf2table.parsetable.LRItem;
import org.metaborg.sdf2table.parsetable.LRParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableGenType;
import org.metaborg.sdf2table.parsetable.State;

import com.google.common.collect.Maps;


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
    	prepareLRFollowSets(state);
    	
//    	// Calculate first sets for each symbol in state
//    	for(LRItem item : state.getItems()) {
//    		IProduction prod = item.getProd();
//    		Symbol s = prod.leftHand();
//    		calculateLRFirstSet(state, s);
//    	}
    	
    	calculateLRFirstFollowSets(state);
    	
//    	Map<Symbol, CharacterClass> stateFollowSets = followSetsLR.get(state);
//    	for(Symbol s : stateFollowSets.keySet()) {
//    		// LF, VT, FF, CR, Space, BS, DEL
//    		if(s instanceof CharacterClass) {
//    			CharacterClass cc = (CharacterClass) s;
//    			boolean ccContainsWhitespaceChars = cc.contains(10) || cc.contains(11) || cc.contains(12) || cc.contains(32) || cc.contains(8) || cc.contains(127);
//    			if(ccContainsWhitespaceChars) {
//    				stateFollowSets.put(s, CharacterClass.getFullCharacterClass());
//    			}
//    		}
//    		CharacterClass ccFollow = stateFollowSets.get(s);
//    		
//    		if(ccFollow.isEmptyCC()) {
//    			stateFollowSets.put(s, CharacterClass.getFullCharacterClass());
//    		}
//    	}
    }
	
    public void calculateLRFirstFollowSets(State state) {
    	for(int i = 0; i <= 256; i++) {
			CharacterClass ccSingle = new CharacterClass(new CharacterClassSingle(i));
			CharacterClass ccEmpty = (CharacterClass) new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
			firstSetsLR.get(state).put(ccSingle, ccSingle);
			followSetsLR.get(state).put(ccSingle, ccEmpty);
		}
    	
    	Map<Symbol, Boolean> lhsMapping = Maps.newLinkedHashMap();
    	for(LRItem item : state.getItems()) {
    		lhsMapping.put(item.getProd().leftHand(), true);
    	}
    	
    	boolean allComplete = false;
    	
    	while(allComplete == false) {
    		allComplete = true;
	    	//for(IProduction prod : productionsMapping.keySet()) {
	    	for(LRItem item : state.getItems()) {
	    		IProduction prod = item.getProd();
	    		int k = prod.rightHand().size()-1;
	    		
	    		// Production X -> Y_0 Y_1...Y_k
	    		Symbol x = prod.leftHand();
    			//int i = item.getDotPosition();
    			
    			for(int i = 0; i <= k; i++) {
	    			if(i < prod.rightHand().size()) {
			    		Symbol y_i = prod.rightHand().get(i);
			    		
			    		boolean allNullableBeforeI = true;
		    			for(int a = 0; a < i; a++) {
		    				Symbol sBefore = prod.rightHand().get(a);
		    				if(!(sBefore.isNullable() || i == 1)) {
		    					allNullableBeforeI = false;
		    				}
		    			}
		    			if(allNullableBeforeI) {
		    				CharacterClass superSet = getFirst(state, x);
		    				CharacterClass subSet = getFirst(state, y_i);
		    				
		    				if(y_i instanceof CharacterClass || lhsMapping.containsKey(y_i)) {
			    				CharacterClass union = CharacterClass.union(getFirst(state, x), getFirst(state, y_i));
			    				if(!getFirst(state, x).equals(union)) {
			    					addFirst(state, x, union);
				    				allComplete = false;
			    				}
		    				} else {
		    					CharacterClass fullCC = CharacterClass.getFullCharacterClass();
		        				if(!getFirst(state, x).equals(fullCC)) {
		        					addFirst(state, x, fullCC);
			        				allComplete = false;
		        				}
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
		    				CharacterClass superSet = getFollow(state, y_i);
		    				CharacterClass subSet = getFollow(state, x);
		    				
		    				if(x instanceof CharacterClass || lhsMapping.containsKey(x)) {
			    				CharacterClass union = CharacterClass.union(getFollow(state, y_i), getFollow(state, x));
			    				if(!getFollow(state, y_i).equals(union)) {
			    					addFollow(state, y_i, union);
				    				allComplete = false;
			    				}
			    			} else {
		    					CharacterClass fullCC = CharacterClass.getFullCharacterClass();
		        				if(!getFollow(state, y_i).equals(fullCC)) {
		        					addFollow(state, y_i, fullCC);
			        				allComplete = false;
		        				}
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
		        				CharacterClass superSet = getFollow(state, y_i);
			    				CharacterClass subSet = getFirst(state, y_j);
			    				
			    				if(y_j instanceof CharacterClass || lhsMapping.containsKey(y_j)) {
			        				CharacterClass union = CharacterClass.union(getFollow(state, y_i), getFirst(state, y_j));
			        				if(!getFollow(state, y_i).equals(union)) {
			        					addFollow(state, y_i, union);
				        				allComplete = false;
			        				}
			    				} else {
			    					CharacterClass fullCC = CharacterClass.getFullCharacterClass();
			        				if(!getFollow(state, y_i).equals(fullCC)) {
			        					addFollow(state, y_i, fullCC);
				        				allComplete = false;
			        				}
			    				}
		        			}
		    			}
	    			}
		    	}
	    	}
    	}
    }
    
    public CharacterClass getFirst(State state, Symbol s) {
    	CharacterClass result = null;
    	if(s instanceof CharacterClass) {
			CharacterClass cc = (CharacterClass) s;
			CharacterClass union = (CharacterClass) new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
			for(int i = cc.min(); i <= cc.max(); i++) {
				if(cc.contains(i)) {
					CharacterClass ccSingle = new CharacterClass(new CharacterClassSingle(i));
					union = CharacterClass.union(union, firstSetsLR.get(state).get(ccSingle));
				}
			}
			result = union;
		} else {
			result = firstSetsLR.get(state).get(s);
    	}
    	return result;
    }
    
    public void addFirst(State state, Symbol s, CharacterClass ccAdd) {
    	if(s instanceof CharacterClass) {
    		CharacterClass cc = (CharacterClass) s;
			for(int i = cc.min(); i <= cc.max(); i++) {
				if(cc.contains(i)) {
					CharacterClass ccSingle = new CharacterClass(new CharacterClassSingle(i));
					firstSetsLR.get(state).put(ccSingle, ccAdd);
				}
			}
    	} else {
    		firstSetsLR.get(state).put(s, ccAdd);
    	}
    }
    
    
    public CharacterClass getFollow(State state, Symbol s) {
    	CharacterClass result = null;
    	if(s instanceof CharacterClass) {
			CharacterClass cc = (CharacterClass) s;
			CharacterClass union = (CharacterClass) new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
			for(int i = cc.min(); i <= cc.max(); i++) {
				if(cc.contains(i)) {
					CharacterClass ccSingle = new CharacterClass(new CharacterClassSingle(i));
					union = CharacterClass.union(union, followSetsLR.get(state).get(ccSingle));
				}
			}
			result = union;
		} else {
			result = followSetsLR.get(state).get(s);
    	}
    	return result;
    }
    
    public void addFollow(State state, Symbol s, CharacterClass ccAdd) {
    	if(s instanceof CharacterClass) {
    		CharacterClass cc = (CharacterClass) s;
			for(int i = cc.min(); i <= cc.max(); i++) {
				if(cc.contains(i)) {
					CharacterClass ccSingle = new CharacterClass(new CharacterClassSingle(i));
					followSetsLR.get(state).put(ccSingle, ccAdd);
				}
			}
    	} else {
    		followSetsLR.get(state).put(s, ccAdd);
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
	    		if(prod.rightHand().size() > 0) {
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
    }
	
	
	
	// Calculates follow sets for this state
	@Override
    public void calculateLRFollowSets(State state) {
//    	prepareLRFollowSets(state);
//    	
//    	// Calculate follow sets for each symbol in state
//    	for(LRItem item : state.getItems()) {
//    		IProduction prod = item.getProd();
//    		Symbol s = prod.leftHand();
//    		calculateLRFollowSet(state, s);
//    	}
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