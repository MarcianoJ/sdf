package org.metaborg.newsdf2table.parsetable;

import java.util.Queue;
import java.util.Set;

import org.metaborg.newsdf2table.grammar.IProduction;
import org.metaborg.newsdf2table.grammar.Priority;
import org.metaborg.newsdf2table.grammar.Symbol;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class LRItem {

    ParseTable pt;
    IProduction prod;
    int dotPosition;

    public LRItem(IProduction prod, int dotPosition, ParseTable pt) {
        if(!(prod instanceof ContextualProduction) && pt.getGrammar().contextual_prods.containsKey(prod)) {
            this.prod = pt.getGrammar().contextual_prods.get(prod);
        } else {
            this.prod = prod;
        }
        this.pt = pt;
        this.dotPosition = dotPosition;
    }

    public void process(Set<LRItem> items, Queue<LRItem> itemsQueue, SetMultimap<Symbol, LRItem> symbol_items) {
        if(pt.item_derivedItems.containsKey(this)) {
            for(LRItem derivedItem : pt.item_derivedItems.get(this)) {
                if(!items.contains(derivedItem)) {
                    itemsQueue.add(derivedItem);
                }
                items.add(derivedItem);
            }
            items.add(this);

            if(this.dotPosition < prod.rightHand().size()) {
                symbol_items.put(prod.rightHand().get(this.dotPosition), this);
            }

        } else {
            
            Set<LRItem> derivedItems = Sets.newHashSet();

            if(dotPosition < prod.rightHand().size()) {

                Symbol s_at_dot = prod.rightHand().get(dotPosition);

                for(IProduction p : pt.getGrammar().symbol_prods.get(s_at_dot)) {

                    if(!isPriorityConflict(this, p)) {

                        // p might be the problematic contextual production
                        if(pt.getGrammar().contextual_prods.get(p) != null) {
                            p = pt.getGrammar().contextual_prods.get(p);
                        }

                        LRItem newItem = new LRItem(p, 0, pt);
                        derivedItems.add(newItem);
                        
                        if(!items.contains(newItem)) {
                            itemsQueue.add(newItem);
                        }
                    }
                }
            }

            pt.item_derivedItems.put(this, derivedItems);
            items.add(this);
            if(this.dotPosition < prod.rightHand().size()) {
                symbol_items.put(prod.rightHand().get(this.dotPosition), this);
            }
        }
    }

    public LRItem shiftDot() {
        return new LRItem(this.prod, this.dotPosition + 1, this.pt);
    }

    private boolean isPriorityConflict(LRItem item, IProduction p) {
        IProduction higher = item.prod;
        IProduction lower = p;

        if(higher instanceof ContextualProduction) {
            higher = ((ContextualProduction) higher).orig_prod;
        }

        if(lower instanceof ContextualProduction) {
            lower = ((ContextualProduction) lower).orig_prod;
        }

        Priority prio = new Priority(higher, lower, false);
        if(pt.getGrammar().priorities().containsKey(prio)) {
            Set<Integer> arguments = pt.getGrammar().priorities().get(prio);
            for(int i : arguments) {
                if(i == item.dotPosition) {
                    return true;
                }
            }
        }
        return false;
    }



    @Override public String toString() {
        String buf = "";
        buf += prod.leftHand();
        buf += " -> ";
        for(int i = 0; i < prod.rightHand().size(); i++) {
            if(i != 0)
                buf += " ";
            if(i == dotPosition) {
                buf += ". ";
            }
            buf += prod.rightHand().get(i);
        }
        if(dotPosition >= prod.rightHand().size()) {
            buf += " .";
        }

        return buf;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dotPosition;
        result = prime * result + ((prod == null) ? 0 : prod.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        LRItem other = (LRItem) obj;
        if(dotPosition != other.dotPosition)
            return false;
        if(prod == null) {
            if(other.prod != null)
                return false;
        } else if(!prod.equals(other.prod))
            return false;
        return true;
    }

}
