package org.metaborg.newsdf2table.parsetable;

import java.util.Queue;
import java.util.Set;

import org.metaborg.newsdf2table.grammar.CharacterClass;
import org.metaborg.newsdf2table.grammar.CharacterClassNumeric;
import org.metaborg.newsdf2table.grammar.CharacterClassSeq;
import org.metaborg.newsdf2table.grammar.IProduction;
import org.metaborg.newsdf2table.grammar.Priority;
import org.metaborg.newsdf2table.grammar.Symbol;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class State implements Comparable<State> {

    ParseTable pt;

    int label;
    Set<GoTo> gotos;
    Set<Action> actions;
    Set<LRItem> kernel;
    Set<LRItem> items;
    SetMultimap<Symbol, LRItem> symbol_items;
    SetMultimap<CharacterClass, Action> lr_actions;

    Queue<LRItem> itemsQueue;

    boolean processed = false;

    public static Set<State> states = Sets.newHashSet();

    public State(IProduction p, ParseTable pt) {
        itemsQueue = Lists.newLinkedList();
        items = Sets.newHashSet();
        actions = Sets.newHashSet();
        gotos = Sets.newHashSet();
        kernel = Sets.newHashSet();
        symbol_items = HashMultimap.create();
        lr_actions = HashMultimap.create();

        this.pt = pt;
        label = this.pt.totalStates;
        this.pt.totalStates++;

        LRItem item = new LRItem(p, 0, pt);
        kernel.add(item);
        pt.kernel_states.put(kernel, this);
        itemsQueue.add(item);
        closure();
    }

    public State(Set<LRItem> kernel, ParseTable pt) {
        itemsQueue = Lists.newLinkedList();
        items = Sets.newHashSet();
        actions = Sets.newHashSet();
        gotos = Sets.newHashSet();
        symbol_items = HashMultimap.create();
        lr_actions = HashMultimap.create();

        this.kernel = Sets.newHashSet();
        this.kernel.addAll(kernel);
        pt.kernel_states.put(kernel, this);

        this.pt = pt;
        label = this.pt.totalStates;
        this.pt.totalStates++;

        for(LRItem item : kernel) {
            itemsQueue.add(item);
        }
        closure();
    }

    private void closure() {
        while(!itemsQueue.isEmpty()) {
            LRItem current = itemsQueue.poll();
            current.process(items, itemsQueue, symbol_items);
        }
    }

    public void doShift() {
        for(Symbol s_at_dot : symbol_items.keySet()) {
            if(s_at_dot instanceof CharacterClass) {
                Set<LRItem> new_kernel = Sets.newHashSet();
                Set<GoTo> new_gotos = Sets.newHashSet();
                Set<Shift> new_shifts = Sets.newHashSet();
                for(LRItem item : symbol_items.get(s_at_dot)) {
                    Shift shift = new Shift((CharacterClass) s_at_dot);
                    new_kernel.add(item.shiftDot());
                    if(!(item.prod.equals(pt.initial_prod) && item.dotPosition == 1)) {
                        new_shifts.add(shift);
                    }
                    new_gotos.add(new GoTo((CharacterClass) s_at_dot, pt));
                }
                if(!new_kernel.isEmpty()) {
                    checkKernel(new_kernel, new_gotos, new_shifts);
                }
            } else {
                // if symbol is contextual, shift using all productions, but the context

                for(IProduction p : pt.getGrammar().symbol_prods.get(s_at_dot)) {

                    // p might be the problematic contextual production
                    if(pt.getGrammar().contextual_prods.get(p) != null) {
                        p = pt.getGrammar().contextual_prods.get(p);
                    }

                    Set<LRItem> new_kernel = Sets.newHashSet();
                    Set<GoTo> new_gotos = Sets.newHashSet();
                    Set<Shift> new_shifts = Sets.newHashSet();
                    for(LRItem item : symbol_items.get(s_at_dot)) {
                        // if item.prod does not conflict with p
                        if(!isPriorityConflict(item, p)) {
                            new_kernel.add(item.shiftDot());
                            new_gotos.add(new GoTo(pt.prod_labels.get(p), pt));
                        } else {
                            // it is a deep priority conflict and is not a conflicting arg, expand still
                            Set<Integer> conflicting_args = item.prod.isDeepPriorityConflict(pt, p);
                            if(!conflicting_args.isEmpty() && !conflicting_args.contains(item.dotPosition)) {
                                new_kernel.add(item.shiftDot());
                                new_gotos.add(new GoTo(pt.prod_labels.get(p), pt));
                            }
                        }
                    }
                    if(!new_kernel.isEmpty()) {
                        checkKernel(new_kernel, new_gotos, new_shifts);
                    }
                }
            }
        }
    }

    public void doReduces() {
        // for each item p_i : A = A0 ... AN .
        // add a reduce action reduce([0-256] / follow(A), pi)
        for(LRItem item : items) {
                       

            if(item.dotPosition == item.prod.rightHand().size()) {
                int prod_label = pt.prod_labels.get(item.prod); 
                
                if(item.prod.leftHand().followRestriction().isEmpty()) {
                    addReduceAction(item.prod, prod_label, CharacterClass.maxCC, null);
                } else {
                    CharacterClass final_range = CharacterClass.maxCC;
                    for(Symbol s : item.prod.leftHand().followRestriction()) {
                        if(s instanceof CharacterClassSeq) {
                            Symbol cc_restriction = ((CharacterClassSeq) s).getHead();
                            Set<Symbol> lookahead_symbols = ((CharacterClassSeq) s).getTail();
                            CharacterClass[] lookahead_array = new CharacterClass[lookahead_symbols.size()];
                            int i = 0;
                            for(Symbol lookahead_symbol : lookahead_symbols) {
                                lookahead_array[i] = new CharacterClass(lookahead_symbol);
                                i++;
                            }
                            CharacterClass lookahead = CharacterClass.union(lookahead_array);
                            CharacterClass reduction_range =
                                CharacterClass.intersection(CharacterClass.maxCC, new CharacterClass(cc_restriction));
                            if(reduction_range != null) {
                                final_range = final_range.difference(reduction_range);
                                addReduceAction(item.prod, prod_label, reduction_range, lookahead);
                            }
                        } else {
                            final_range = final_range.difference(new CharacterClass(s));
                        }
                    }
                    addReduceAction(item.prod, prod_label, final_range, null);
                }
            }
            // <Start> = <START> . EOF
            if(item.prod.equals(pt.initial_prod) && item.dotPosition == 1) {
                lr_actions.put(new CharacterClass(new CharacterClassNumeric(256)), new Accept());
            }
        }
    }


    private void addReduceAction(IProduction prod, Integer label, CharacterClass cc, CharacterClass lookahead) {
        CharacterClass final_range = cc;

        for(CharacterClass range : lr_actions.keySet()) {
            if(final_range == null) {
                break;
            }
            CharacterClass intersection = CharacterClass.intersection(final_range, range);
            if(intersection != null) {
                if(intersection.equals(range)) {
                    lr_actions.put(intersection, new Reduce(prod, label, range, lookahead));
                    final_range = final_range.difference(intersection);
                }
            }
        }

        if(final_range != null) {
            lr_actions.put(final_range, new Reduce(prod, label, final_range, lookahead));
        }
    }

    private void checkKernel(Set<LRItem> new_kernel, Set<GoTo> new_gotos, Set<Shift> new_shifts) {
        if(pt.kernel_states.containsKey(new_kernel)) {
            int stateNumber = pt.kernel_states.get(new_kernel).label;
            // set recently added shift and goto actions to new state
            for(Shift shift : new_shifts) {
                shift.setState(stateNumber);

                this.lr_actions.put(shift.cc, shift);
                // this.lr_actions.add(new LRAction(shift.cc, shift));
                this.actions.add(shift);
            }
            for(GoTo g : new_gotos) {
                g.setState(stateNumber);
                this.gotos.add(g);
            }
        } else {
            State new_state = new State(new_kernel, pt);
            for(Shift shift : new_shifts) {
                shift.setState(new_state.label);
                this.lr_actions.put(shift.cc, shift);
                // this.lr_actions.add(new LRAction(shift.cc, shift));
                this.actions.add(shift);
            }
            for(GoTo g : new_gotos) {
                g.setState(new_state.label);
                this.gotos.add(g);
            }
            pt.stateQueue.add(new_state);
        }
    }

    private boolean isPriorityConflict(LRItem item, IProduction p) {
        Priority prio = new Priority(item.prod, p, false);
        if(pt.getGrammar().priorities().containsKey(prio)) {
            Set<Integer> arguments = pt.getGrammar().priorities().get(prio);
            for(int i : arguments) {
                if(i == -1 || i == item.dotPosition)
                    return true;
            }
        }
        return false;
    }



    @Override public String toString() {
        String buf = "";
        int i = 0;
        buf += "State " + label;
        if(!gotos.isEmpty()) {
            buf += "\nGotos: ";
        }
        for(GoTo g : gotos) {
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
            for(Action a : lr_actions.get(cc)) {
                if(j != 0)
                    buf += ", ";
                buf += a;
                j++;
            }
            i++;
        }
        if(!items.isEmpty()) {
            buf += "\nItems: ";
        }
        i = 0;
        for(LRItem it : items) {
            if(i != 0)
                buf += "\n       ";
            buf += it.toString();
            i++;
        }

        return buf;
    }

    @Override public int compareTo(State o) {
        return this.label - o.label;
    }


}
