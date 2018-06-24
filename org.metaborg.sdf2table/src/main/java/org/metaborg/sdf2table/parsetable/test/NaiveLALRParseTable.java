package org.metaborg.sdf2table.parsetable.test;

import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.parsetable.LALRParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableGenType;
import org.metaborg.sdf2table.parsetable.State;
import org.metaborg.sdf2table.parsetable.StateStatus;


// ParseTable class for LALR(1) with naive first/follow implementation
public class NaiveLALRParseTable extends NaiveLRParseTable {

	private static final long serialVersionUID = -5059636749895036064L;

	public NaiveLALRParseTable(NormGrammar grammar, boolean dynamic, boolean dataDependent, boolean solveDeepConflicts,
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