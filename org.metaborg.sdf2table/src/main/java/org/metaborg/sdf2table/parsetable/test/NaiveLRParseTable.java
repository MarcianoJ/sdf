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
    	
    	calculateLRFirstFollowSets(state);
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
	    	for(LRItem item : state.getItems()) {
	    		IProduction prod = item.getProd();
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
    			}
    			
    			
    			int i = item.getDotPosition();
    			
    			if(i < prod.rightHand().size()) {
    				Symbol y_i = prod.rightHand().get(i);
    				
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
}