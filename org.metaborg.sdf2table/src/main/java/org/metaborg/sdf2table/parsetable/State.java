package org.metaborg.sdf2table.parsetable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.metaborg.characterclasses.CharacterClassFactory;
import org.metaborg.characterclasses.ICharacterClassFactory;
import org.metaborg.parsetable.IParseInput;
import org.metaborg.parsetable.IState;
import org.metaborg.parsetable.actions.IAction;
import org.metaborg.parsetable.actions.IGoto;
import org.metaborg.parsetable.actions.IReduce;
import org.metaborg.parsetable.characterclasses.ICharacterClass;
import org.metaborg.sdf2table.grammar.CharacterClass;
import org.metaborg.sdf2table.grammar.IProduction;
import org.metaborg.sdf2table.grammar.Symbol;
import org.metaborg.sdf2table.parsetable.query.ActionsForCharacterDisjointSorted;
import org.metaborg.sdf2table.parsetable.query.ActionsPerCharacterClass;
import org.metaborg.sdf2table.parsetable.query.IActionsForCharacter;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class State implements IState, Comparable<State>, Serializable {

    private static final long serialVersionUID = 7118071460461287164L;

    ParseTable pt;

    private final int label;
    private Set<GoTo> gotos;
    private Map<Integer, IGoto> gotosMapping;
    private final Set<LRItem> kernel;
    private Set<LRItem> items;
    private LinkedHashMultimap<Symbol, LRItem> symbol_items;
    private LinkedHashMultimap<CharacterClass, Action> lr_actions;
    IActionsForCharacter actionsForCharacter;
    private boolean rejectable;

    private StateStatus status = StateStatus.VISIBLE;

    public Set<State> states = Sets.newHashSet();
    
    public State(IProduction p, ParseTable pt) {
    	this(p, pt, false);
    }
    
    public State(IProduction p, ParseTable pt, boolean temporary) {
        items = Sets.newLinkedHashSet();
        gotos = Sets.newLinkedHashSet();
        gotosMapping = Maps.newHashMap();
        kernel = Sets.newLinkedHashSet();
        symbol_items = LinkedHashMultimap.create();
        lr_actions = LinkedHashMultimap.create();
        this.rejectable = false;
        
        this.pt = pt;
        
        if(temporary) {
        	label = -1;
        } else {
	        label = this.pt.totalStates();
	        this.pt.stateLabels().put(label, this);
	        this.pt.incTotalStates();
        }

        LRItem item = new LRItem(p, 0, pt);
        kernel.add(item);
    }
    
    public State(Set<LRItem> kernel, ParseTable pt) {
    	this(kernel, pt, false);
    }

    public State(Set<LRItem> kernel, ParseTable pt, boolean temporary) {
        items = Sets.newLinkedHashSet();
        gotos = Sets.newLinkedHashSet();
        gotosMapping = Maps.newHashMap();
        symbol_items = LinkedHashMultimap.create();
        lr_actions = LinkedHashMultimap.create();
        this.rejectable = false;

        this.kernel = Sets.newLinkedHashSet();
        this.kernel.addAll(kernel);
        
        this.pt = pt;
        
        if(temporary) {
        	label = -1;
        } else {
	        label = this.pt.totalStates();
	        this.pt.stateLabels().put(label, this);
	        this.pt.incTotalStates();
        }
    }

    public void closure() {
        for(LRItem item : kernel) {
            // if(item.getDotPosition() < item.getProd().rightHand().size()) {
            // pt.symbolStatesMapping().put(item.getProd().rightHand().get(item.getDotPosition()), this);
            // }
            item.process(items, symbol_items, this);
        }
    }

    public void doShift(boolean mergeStates) {
        for(Symbol s_at_dot : symbol_items.keySet()) {
            if(s_at_dot instanceof CharacterClass) {
                Set<LRItem> new_kernel = Sets.newLinkedHashSet();
                Set<GoTo> new_gotos = Sets.newLinkedHashSet();
                Set<Shift> new_shifts = Sets.newLinkedHashSet();
                for(LRItem item : symbol_items.get(s_at_dot)) {
                    Shift shift = new Shift((CharacterClass) s_at_dot);
                    new_kernel.add(item.shiftDot());
                    if(!(item.getProd().equals(pt.initialProduction()) && item.getDotPosition() == 1)) {
                        new_shifts.add(shift);
                    }
                    new_gotos.add(new GoTo((CharacterClass) s_at_dot, pt));
                }
                if(!new_kernel.isEmpty()) {
                    checkKernel(new_kernel, new_gotos, new_shifts, mergeStates);
                }
            } else {
                for(IProduction p : pt.normalizedGrammar().getSymbolProductionsMapping().get(s_at_dot)) {

                    // p might be a contextual production
                    if(pt.normalizedGrammar().getProdContextualProdMapping().get(p) != null) {
                        p = pt.normalizedGrammar().getProdContextualProdMapping().get(p);
                    }

                    Set<LRItem> new_kernel = Sets.newLinkedHashSet();
                    Set<GoTo> new_gotos = Sets.newLinkedHashSet();
                    Set<Shift> new_shifts = Sets.newLinkedHashSet();
                    for(LRItem item : symbol_items.get(s_at_dot)) {
                        // if item.prod does not conflict with p
                        if(!LRItem.isPriorityConflict(item, p, pt.normalizedGrammar().priorities())) {
                            new_kernel.add(item.shiftDot());
                            new_gotos.add(new GoTo(pt.productionLabels().get(p), pt));
                        }
                    }
                    if(!new_kernel.isEmpty()) {
                        checkKernel(new_kernel, new_gotos, new_shifts, mergeStates);
                    }
                }
            }
        }
    }

    public void doReduces() {
        // for each item p_i : A = A0 ... AN .
        // add a reduce action reduce([0-256] / follow(A), p_i)
        for(LRItem item : items) {

            if(item.getDotPosition() == item.getProd().rightHand().size()) {
                int prod_label = pt.productionLabels().get(item.getProd());
                
                List<ICharacterClass> itemFollowSet = item.getLookahead();
                CharacterClass final_range = (CharacterClass) itemFollowSet.get(0);
                
                // TODO: support lookahead for followSets with k > 1
                if(item.getProd().leftHand().followRestriction() == null
                    || item.getProd().leftHand().followRestriction().isEmptyCC()) {
                    addReduceAction(item.getProd(), prod_label, final_range, null);
                } else {
                	final_range = final_range.difference(item.getProd().leftHand().followRestriction());
                    for(CharacterClass[] s : item.getProd().leftHand().followRestrictionLookahead()) {
                        final_range = final_range.difference(s[0]);

                        // create reduce Lookahead actions
                        CharacterClass[] lookahead = Arrays.copyOfRange(s, 1, s.length);
                        addReduceAction(item.getProd(), prod_label, s[0], lookahead);
                    }
                    addReduceAction(item.getProd(), prod_label, final_range, null);
                }
            }
            // <Start> = <START> . EOF
            if(item.getProd().equals(pt.initialProduction()) && item.getDotPosition() == 1) {
                lr_actions.put(new CharacterClass(CharacterClassFactory.EOF_SINGLETON),
                    new Accept(new CharacterClass(CharacterClassFactory.EOF_SINGLETON)));
            }
        }
    }


    private void addReduceAction(IProduction p, Integer label, CharacterClass cc, CharacterClass[] lookahead) {
        CharacterClass final_range = cc;
        ParseTableProduction prod = pt.productionsMapping().get(p);

        LinkedHashMultimap<CharacterClass, Action> newLR_actions = LinkedHashMultimap.create();;
        for(CharacterClass range : lr_actions.keySet()) {
            if(final_range.isEmptyCC()) {
                break;
            }
            CharacterClass intersection = CharacterClass.intersection(final_range, range);
            if(!intersection.isEmptyCC()) {
                if(intersection.equals(range)) {
                    if(lookahead != null) {
                        newLR_actions.put(intersection, new ReduceLookahead(prod, label, intersection, lookahead));
                    } else {
                        newLR_actions.put(intersection, new Reduce(prod, label, intersection));
                    }
                    final_range = final_range.difference(intersection);
                }
            }
        }
        
        lr_actions.putAll(newLR_actions);

        if(!final_range.isEmptyCC()) {
            if(lookahead != null) {
                lr_actions.put(final_range, new ReduceLookahead(prod, label, final_range, lookahead));
            } else {
                lr_actions.put(final_range, new Reduce(prod, label, final_range));
            }
        }
    }

    private void checkKernel(Set<LRItem> new_kernel, Set<GoTo> new_gotos, Set<Shift> new_shifts, boolean mergeStates) {
    	Map<LRItem, List<ICharacterClass>> newAugmentedKernel = Maps.newLinkedHashMap();
        for(LRItem newKernelItem : new_kernel) {
        	newAugmentedKernel.put(newKernelItem, newKernelItem.getLookahead());
        }
        
    	//if(pt.kernelMap().containsKey(new_kernel)) {
    	if(pt.augmentedKernelMap().containsKey(newAugmentedKernel)) {
            int stateNumber = pt.augmentedKernelMap().get(newAugmentedKernel).getLabel();
            // set recently added shift and goto actions to existing state
            addNewShiftsAndGotos(stateNumber, new_shifts, new_gotos);
            
        // If state with same kernel with same lookahead doesn't yet exist and mergeStates == true,
        // create temporary state from new_kernel, compute follow and merge item lookahead from temp state to existing state
    	} else if(mergeStates == true && pt.kernelMap().containsKey(new_kernel)) {
    		int stateNumber = pt.kernelMap().get(new_kernel).getLabel();
    		addNewShiftsAndGotos(stateNumber, new_shifts, new_gotos);
    		
        	State existingState = pt.kernelMap().get(new_kernel);
        	State temporaryState = new State(new_kernel, pt, true);
        	pt.prepareState(temporaryState);
        	
        	Map<LRItem, List<ICharacterClass>> temporaryStateItems = Maps.newLinkedHashMap();
        	for (LRItem item : existingState.getItems()) {
        		temporaryStateItems.put(item, item.getLookahead());
        	}
        	for(LRItem item : existingState.getKernel()) {
        		List<ICharacterClass> lookahead = temporaryStateItems.get(item);
        		item.mergeLookahead(lookahead);
        	}
        } else {
            State new_state = new State(new_kernel, pt);
            for(Shift shift : new_shifts) {
                shift.setState(new_state.getLabel());
                this.lr_actions.put(shift.cc, shift);
                // this.lr_actions.add(new LRAction(shift.cc, shift));
            }
            for(GoTo g : new_gotos) {
                g.setState(new_state.getLabel());
                this.gotos.add(g);
                this.gotosMapping.put(g.label, g);
            }
            pt.stateQueue().add(new_state);
        }
    }
    
    private void addNewShiftsAndGotos(int stateNumber, Set<Shift> new_shifts, Set<GoTo> new_gotos) {
    	for(Shift shift : new_shifts) {
            shift.setState(stateNumber);

            this.lr_actions.put(shift.cc, shift);
            // this.lr_actions.add(new LRAction(shift.cc, shift));
        }
        for(GoTo g : new_gotos) {
            g.setState(stateNumber);
            this.gotos.add(g);
            this.gotosMapping.put(g.label, g);
        }
    }

    @Override public String toString() {
        String buf = "";
        int i = 0;
        buf += "State " + getLabel();
        if(!gotos.isEmpty()) {
            buf += "\nGotos: ";
        }
        for(IGoto g : gotos) {
            if(i != 0)
                buf += "\n     , ";
            buf += g;
            i++;
        }
        if(!lr_actions.isEmpty()) {
            buf += "\nActions: ";
        }
        i = 0;
        for(CharacterClass cc : lr_actions.keySet()) {
            if(i != 0)
                buf += "\n       , ";
            buf += cc + ": ";
            int j = 0;
            for(IAction a : lr_actions.get(cc)) {
                if(j != 0)
                    buf += ", ";
                buf += a;
                j++;
            }
            i++;
        }
        if(!items.isEmpty()) {
            buf += "\nItems: ";

            i = 0;
            for(LRItem it : items) {
                if(i != 0)
                    buf += "\n       ";
                buf += it.toString();
                i++;
            }
        } else {
            buf += "\nItems: ";

            i = 0;
            for(LRItem it : kernel) {
                if(i != 0)
                    buf += "\n       ";
                buf += it.toString();
                i++;
            }
        }

        return buf;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((kernel == null) ? 0 : kernel.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        State other = (State) obj;
        if(kernel == null) {
            if(other.kernel != null)
                return false;
        } else if(!kernel.equals(other.kernel))
            return false;
        return true;
    }

    @Override public int compareTo(State o) {
        return this.getLabel() - o.getLabel();
    }

    public int getLabel() {
        return label;
    }
    
    public Set<LRItem> getKernel() {
    	return kernel;
    }

    public Set<LRItem> getItems() {
        return items;
    }

    public StateStatus status() {
        return status;
    }

    public void setStatus(StateStatus status) {
        this.status = status;
    }

    public void markDirty() {
        this.items.clear();
        this.symbol_items.clear();
        this.lr_actions.clear();
        this.setStatus(StateStatus.DIRTY);
    }

    public Set<GoTo> gotos() {
        return gotos;
    }

    public Iterable<Action> actions() {
        Set<Action> actions = Sets.newHashSet(lr_actions.values());
        return actions;
    }

    public SetMultimap<CharacterClass, Action> actionsMapping() {
        return lr_actions;
    }

    @Override
    public boolean isRejectable() {
        return rejectable;
    }

    public void markRejectable() {
        this.rejectable = true;
    }

    @Override public int id() {
        return label;
    }

    @Override public Iterable<IAction> getApplicableActions(IParseInput parseInput) {
        return actionsForCharacter.getApplicableActions(parseInput);
    }

    @Override public Iterable<IReduce> getApplicableReduceActions(IParseInput parseInput) {
        return actionsForCharacter.getApplicableReduceActions(parseInput);
    }

    @Override public int getGotoId(int productionId) {
        return gotosMapping.get(productionId).gotoStateId();
    }

    public void calculateActionsForCharacter() {
        ActionsPerCharacterClass[] actions = readActions();

        actionsForCharacter = new ActionsForCharacterDisjointSorted(actions);
    }

    private ActionsPerCharacterClass[] readActions() {
        List<ActionsPerCharacterClass> actionsPerCharacterClasses =
            new ArrayList<ActionsPerCharacterClass>(lr_actions.size());

        for(CharacterClass cc : lr_actions.keySet()) {
            actionsPerCharacterClasses.add(
                new ActionsPerCharacterClass(cc, lr_actions.get(cc).toArray(new IAction[lr_actions.get(cc).size()])));
        }

        return actionsPerCharacterClasses.toArray(new ActionsPerCharacterClass[actionsPerCharacterClasses.size()]);
    }


}
