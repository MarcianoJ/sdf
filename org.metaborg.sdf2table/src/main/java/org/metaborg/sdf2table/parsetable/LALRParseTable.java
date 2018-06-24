package org.metaborg.sdf2table.parsetable;

import org.metaborg.sdf2table.grammar.NormGrammar;


// Parse table class for LALR(1)
public class LALRParseTable extends LRParseTable {

	private static final long serialVersionUID = -6837056749802732025L;


	public LALRParseTable(NormGrammar grammar, boolean dynamic, boolean dataDependent, boolean solveDeepConflicts,
			ParseTableGenType parseType, int k) {
		
		super(grammar, dynamic, dataDependent, solveDeepConflicts, parseType, k);
	}
	
	@Override
	public void processState(State state) {
        state.doShift(true);
        state.doReduces();
        state.calculateActionsForCharacter();
        state.setStatus(StateStatus.PROCESSED);
        setProcessedStates(getProcessedStates() + 1);
    }
}