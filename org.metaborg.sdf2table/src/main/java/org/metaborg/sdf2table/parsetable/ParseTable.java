package org.metaborg.sdf2table.parsetable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.metaborg.characterclasses.CharacterClassFactory;
import org.metaborg.characterclasses.CharacterClassSingle;
import org.metaborg.characterclasses.ICharacterClassFactory;
import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.IState;
import org.metaborg.parsetable.characterclasses.ICharacterClass;
import org.metaborg.sdf2table.deepconflicts.Context;
import org.metaborg.sdf2table.deepconflicts.ContextPosition;
import org.metaborg.sdf2table.deepconflicts.ContextType;
import org.metaborg.sdf2table.deepconflicts.ContextualProduction;
import org.metaborg.sdf2table.deepconflicts.ContextualSymbol;
import org.metaborg.sdf2table.deepconflicts.DeepConflictsAnalyzer;
import org.metaborg.sdf2table.grammar.CharacterClass;
import org.metaborg.sdf2table.grammar.GeneralAttribute;
import org.metaborg.sdf2table.grammar.IPriority;
import org.metaborg.sdf2table.grammar.IProduction;
import org.metaborg.sdf2table.grammar.LiteralType;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.grammar.Priority;
import org.metaborg.sdf2table.grammar.Production;
import org.metaborg.sdf2table.grammar.Sort;
import org.metaborg.sdf2table.grammar.Symbol;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class ParseTable implements IParseTable, Serializable {

    private static final long serialVersionUID = -1845408435423897026L;

    public static final int FIRST_PRODUCTION_LABEL = 257;
    public static final int INITIAL_STATE_NUMBER = 0;
    public static final int VERSION_NUMBER = 6;

    public static final int CALCFIRST = 0;
    public static final int CALCFOLLOW = 1;

    // private static final ILogger logger = LoggerUtils.logger(ParseTable.class);
    private NormGrammar grammar;

    private final int initialStateNumber = 0;
    private int processedStates = 0;

    private IProduction initialProduction;

    // FIXME Currently generating an LR(0) table, compute first/follow sets to generate SLR(1)
    // create first/follow sets by calculating dependencies and using Tarjan's algorithm
    // see http://compilers.iecc.com/comparch/article/01-04-079

    // deep priority conflict resolution is left to parse time
    private final boolean dataDependent;

    private Queue<State> stateQueue = Lists.newLinkedList();

    private BiMap<IProduction, Integer> productionLabels;
    private LabelFactory prodLabelFactory = new LabelFactory(ParseTable.FIRST_PRODUCTION_LABEL);
    private Map<Integer, State> stateLabels = Maps.newLinkedHashMap();

    // mapping from a symbol X to all items A = α . X β to all states s that have that item
    private SymbolStatesMapping symbolStatesMapping = new SymbolStatesMapping();

    private Map<Set<LRItem>, State> kernelStatesMapping = Maps.newLinkedHashMap();
    private Map<Map<LRItem, List<ICharacterClass>>, State> augmentedKernelStatesMapping = Maps.newLinkedHashMap();
    private Map<LRItem, Set<LRItem>> itemDerivedItemsCache = Maps.newLinkedHashMap();

    // maps a set of contexts to a unique integer
    private Map<Set<Context>, Integer> ctxUniqueInt = Maps.newHashMap();

    private final Map<Integer, Integer> leftmostContextsMapping = Maps.newLinkedHashMap();
    private final Map<Integer, Integer> rightmostContextsMapping = Maps.newLinkedHashMap();

    private int totalStates = 0;

    private List<org.metaborg.parsetable.IProduction> productions = Lists.newArrayList();
    Map<IProduction, ParseTableProduction> productionsMapping = Maps.newLinkedHashMap();
    
    // Parse table generation type and k (lookahead)
    private ParseTableGenType parseTableGenType;
	private int kLookahead;
    
    // Constructor, with support for adapting parse table generation type
    public ParseTable(NormGrammar grammar, boolean dynamic, boolean dataDependent, boolean solveDeepConflicts,
    		ParseTableGenType parseType, int k) {
    	
    	this.grammar = grammar;
        this.dataDependent = dataDependent;
        this.parseTableGenType = parseType;
    	this.kLookahead = k;

        // calculate nullable symbols
        calculateNullable();

        // calculate left and right recursive productions (considering nullable symbols)
        calculateRecursion();

        // normalize priorities according to recursion
        normalizePriorities();

        // create labels for productions
        createLabels();

        // calculate deep priority conflicts based on current priorities
        // and generate contextual productions
        if (solveDeepConflicts) {
            final DeepConflictsAnalyzer analysis = DeepConflictsAnalyzer.fromParseTable(this);
            analysis.patchParseTable();

            updateLabelsContextualProductions();
        }

        createJSGLRParseTableProductions(productionLabels);
        
        // create states if the table should not be generated dynamically
        initialProduction = grammar.getInitialProduction();

        if(!dynamic) {
        	initProcessing();
        }
    }
    
    public ParseTable(NormGrammar grammar, boolean dynamic, boolean dataDependent, boolean solveDeepConflicts) {
        this(grammar, dynamic, dataDependent, solveDeepConflicts, ParseTableGenType.LR, 0);
    }
    
    private void initProcessing() {
    	if(parseTableGenType == ParseTableGenType.SLR && kLookahead == 1) {
        	calculateSLRFirstSets();
        	calculateSLRFollowSets();
        }
        
        State s0 = new State(initialProduction, this);
        stateQueue.add(s0);
        processStateQueue();
    }

    private void calculateNullable() {
        boolean markedNullable = false;
        do {
            markedNullable = false;
            for(IProduction p : grammar.getUniqueProductionMapping().values()) {
                if(grammar.getProductionAttributesMapping().get(p).contains(new GeneralAttribute("recover"))) {
                    continue;
                }
                if(p.rightHand().isEmpty() && !p.leftHand().isNullable()) {
                    p.leftHand().setNullable(true);
                    markedNullable = true;
                } else {
                    boolean nullable = true;
                    for(Symbol s : p.rightHand()) {
                        if(s.isNullable() == false) {
                            nullable = false;
                            break;
                        }
                    }
                    if(nullable == true && !p.leftHand().isNullable()) {
                        p.leftHand().setNullable(nullable);
                        markedNullable = true;
                    }
                }
            }

        } while(markedNullable);
    }

    private void calculateRecursion() {
        // direct and indirect left recursion :
        // depth first search, whenever finding a cycle, those symbols are left recursive with respect to each other

        List<IProduction> prodsVisited = Lists.newArrayList();
        for(IProduction p : grammar.getUniqueProductionMapping().values()) {
            leftRecursive(p, Lists.newArrayList(), Lists.newArrayList());
        }

        // similar idea with right recursion
        prodsVisited.clear();
        for(IProduction p : grammar.getUniqueProductionMapping().values()) {
            rightRecursive(p, Lists.newArrayList(), Lists.newArrayList());
        }

        for(IProduction p : grammar.getUniqueProductionMapping().values()) {
            p.calculateRecursion(grammar);
        }
    }

    private void leftRecursive(IProduction prod, List<Symbol> seen, List<IProduction> prodsVisited) {

        if(prodsVisited.contains(prod)) {
            return;
        } else {
            prodsVisited.add(prod);
        }

        List<Symbol> just_seen = Lists.newArrayList(seen);

        // mark left hand symbol as visited
        just_seen.add(prod.leftHand());

        // call left recursive with all productions of
        // the leftmost symbols of rhs (considering nullables)
        for(Symbol s : prod.rightHand()) {
            if(just_seen.contains(s)) {
                // found a cycle
                Set<Symbol> cycle = Sets.newHashSet();
                int pos = just_seen.size() - 1;
                while(pos != just_seen.indexOf(s)) {
                    cycle.add(just_seen.get(pos));
                    pos--;
                }
                cycle.add(just_seen.get(pos));
                // add all symbols in the cycle to the recursive symbols of themselves
                for(Symbol symbol : cycle) {
                    grammar.getLeftRecursiveSymbolsMapping().putAll(symbol, cycle);
                }
            } else {
                for(IProduction p : grammar.getSymbolProductionsMapping().get(s)) {
                    leftRecursive(p, just_seen, prodsVisited);
                }
            }
            if(!s.isNullable()) {
                break;
            }
        }

    }

    private void rightRecursive(IProduction prod, List<Symbol> seen, List<IProduction> prodsVisited) {

        if(prodsVisited.contains(prod)) {
            return;
        } else {
            prodsVisited.add(prod);
        }

        List<Symbol> just_seen = Lists.newArrayList(seen);

        // mark left hand symbol as visited
        just_seen.add(prod.leftHand());

        // call right recursive with all productions of
        // the rightmost symbols of rhs (considering nullables)
        for(int i = prod.rightHand().size() - 1; i >= 0; i--) {
            Symbol s = prod.rightHand().get(i);
            if(just_seen.contains(s)) {
                // found a cycle
                Set<Symbol> cycle = Sets.newHashSet();
                int pos = just_seen.size() - 1;
                while(pos != just_seen.indexOf(s)) {
                    cycle.add(just_seen.get(pos));
                    pos--;
                }
                cycle.add(just_seen.get(pos));
                // add all symbols in the cycle to the recursive symbols of themselves
                for(Symbol symbol : cycle) {
                    grammar.getRightRecursiveSymbolsMapping().putAll(symbol, cycle);
                }
            } else {
                for(IProduction p : grammar.getSymbolProductionsMapping().get(s)) {
                    rightRecursive(p, just_seen, prodsVisited);
                }
            }
            if(!s.isNullable()) {
                break;
            }
        }
    }

    private void normalizePriorities() {

        normalizeAssociativePriorities();

        for(IPriority p : grammar.priorities().keySet()) {
            if(grammar.priorities().get(p).contains(-1)) {
                // mutually recursive priorities = operator precedence
                if(mutuallyRecursive(p)) {
                    // p1 > p2 becomes p1 left p2 and p1 right p2
                    Set<Integer> new_values = Sets.newHashSet();

                    // if p2 : A = A w, priority should affect only right recursive position of p1
                    if(p.lower().leftRecursivePosition() != -1 && p.lower().rightRecursivePosition() == -1) {
                        new_values.add(p.higher().rightRecursivePosition());
                    }

                    // p2 : A = w A, priority should affect only left recursive position of p1
                    if(p.lower().rightRecursivePosition() != -1 && p.lower().leftRecursivePosition() == -1) {
                        new_values.add(p.higher().leftRecursivePosition());
                    }

                    // p2 : A = A w A, priority should affect left and right recursive positions of p1
                    if(p.lower().rightRecursivePosition() != -1 && p.lower().leftRecursivePosition() != -1) {
                        new_values.add(p.higher().rightRecursivePosition());
                        new_values.add(p.higher().leftRecursivePosition());
                    }

                    // if p2 : A = B or p2 : A =
                    if(p.lower().rightHand().size() == 1 || p.lower().rightHand().size() == 0) {
                        new_values.add(p.higher().rightRecursivePosition());
                        new_values.add(p.higher().leftRecursivePosition());
                    }

                    // if p2 : A = w1 A w2, priority should have no effect

                    // infix-prefix, infix-postfix productions

                    // if p1 : A = pre E in E and p2 : A = pre E or p1 : A = pre E in E and p2 : A = E in E
                    if(p.higher().leftRecursivePosition() == -1 && p.lower().leftRecursivePosition() == -1
                        && (p.lower().rightRecursivePosition() != -1 || p.higher().rightRecursivePosition() != -1)) {

                        // dangling else (p1 : A = pre E in E and p2 : A = pre E)
                        boolean matchPrefix = false;
                        for(int i = 0; i < Math.min(p.higher().rightHand().size(), p.lower().rightHand().size()); i++) {
                            if(p.higher().rightHand().get(i).equals(p.lower().rightHand().get(i))) {
                                matchPrefix = true;
                            } else {
                                matchPrefix = false;
                                break;
                            }
                        }
                        if(matchPrefix) {
                            new_values
                                .add(Math.min(p.higher().rightRecursivePosition(), p.lower().rightRecursivePosition()));
                        }
                        // }
                    }

                    // if p1 : A = E in E pos and p2 : A = E pos or p1 : A = E in E pos and p2 : A = E in E
                    if(p.higher().rightRecursivePosition() == -1 && p.higher().leftRecursivePosition() != -1
                        && p.lower().leftRecursivePosition() != -1
                        && p.higher().rightHand().size() > p.lower().rightHand().size()) {

                        // p1 : A = E in E pos and p2 : A = E in E
                        if(p.lower().rightRecursivePosition() != -1) {
                            boolean matchPrefix = false;
                            for(int i = 0; i < p.lower().rightHand().size(); i++) {
                                if(p.higher().rightHand().get(i).equals(p.lower().rightHand().get(i))) {
                                    matchPrefix = true;
                                } else {
                                    matchPrefix = false;
                                    break;
                                }
                            }
                            if(matchPrefix) {
                                new_values.add(p.lower().rightRecursivePosition());
                            }
                        } else { // mirrored dangling else (p1 : A = E in E pos and p2 : A = E pos)
                            boolean matchSuffix = false;
                            for(int i = 0; i < p.lower().rightHand().size(); i++) {
                                if(p.higher().rightHand().get(p.higher().rightHand().size() - 1 - i) // suffix matches
                                    .equals(p.lower().rightHand().get(p.lower().rightHand().size() - 1 - i))) {
                                    matchSuffix = true;
                                } else {
                                    matchSuffix = false;
                                    break;
                                }
                            }
                            if(matchSuffix) {
                                new_values.add(p.higher().rightHand().size() - p.lower().rightHand().size());
                            }
                        }
                    }

                    new_values.addAll(grammar.priorities().get(p));
                    grammar.priorities().replaceValues(p, new_values);
                }
            }
        }

        // to calculate the parenthesizer
        for(IPriority p : grammar.priorities().keySet()) {
            grammar.getHigherPriorityProductions().put(p.higher(), p);
        }

    }

    private void normalizeAssociativePriorities() {

        // priorities derived from associativity of indirectly recursive productions
        SetMultimap<IPriority, Integer> new_priorities = HashMultimap.create();

        for(IPriority p : grammar.priorities().keySet()) {
            // right associative
            if(grammar.priorities().get(p).contains(Integer.MIN_VALUE)) {
                if(p.higher().leftRecursivePosition() == -1)
                    continue;

                // p right p and indirect recursion on p
                Symbol leftRecursive = p.higher().rightHand().get(p.higher().leftRecursivePosition());
                if(p.higher().equals(p.lower()) && !leftRecursive.equals(p.higher().leftHand())) {
                    for(IProduction prod : grammar.getSymbolProductionsMapping().get(leftRecursive)) {
                        // if prod : A = w A, add new priority because it consists of a deep conflict
                        if(prod.leftRecursivePosition() == -1 && prod.rightRecursivePosition() != -1) {
                            new_priorities.put(new Priority(p.higher(), prod, false),
                                p.higher().leftRecursivePosition());
                        }
                    }
                }

                if(p.higher().leftRecursivePosition() != -1) {
                    grammar.priorities().put(p, p.higher().leftRecursivePosition());
                }
            }
            // left associative
            if(grammar.priorities().get(p).contains(Integer.MAX_VALUE)) {
                // if production is not both right and left recursive
                if(p.higher().rightRecursivePosition() == -1)
                    continue;

                // p left p and indirect recursion on p
                Symbol rightRecursive = p.higher().rightHand().get(p.higher().rightRecursivePosition());
                if(p.higher().equals(p.lower()) && !rightRecursive.equals(p.higher().leftHand())) {
                    for(IProduction prod : grammar.getSymbolProductionsMapping().get(rightRecursive)) {
                        // if prod : A = A w, add new priority because it consists of a deep conflict
                        if(prod.leftRecursivePosition() != -1 && prod.rightRecursivePosition() == -1) {
                            new_priorities.put(new Priority(p.higher(), prod, false),
                                p.higher().rightRecursivePosition());
                        }
                    }
                }

                if(p.higher().rightRecursivePosition() != -1) {
                    grammar.priorities().put(p, p.higher().rightRecursivePosition());
                }
            }
        }

        grammar.priorities().putAll(new_priorities);
    }

    private boolean mutuallyRecursive(IPriority p) {
        return grammar.getLeftRecursiveSymbolsMapping().get(p.higher().leftHand()).contains(p.lower().leftHand())
            || grammar.getRightRecursiveSymbolsMapping().get(p.higher().leftHand()).contains(p.lower().leftHand());
    }

    /*
     * TODO calculate first and follow sets private void calculateFirstFollow() { for(IProduction p :
     * getGrammar().prods.values()) { p.calculateDependencies(getGrammar()); }
     * 
     * tarjanStack = new Stack<>(); first_components = Sets.newHashSet(); for(IProduction p :
     * getGrammar().prods.values()) { if(p.firstSet().index == -1) { stronglyConnectedTarjan(p.firstSet(),
     * first_components); } } }
     * 
     * 
     * private void stronglyConnectedTarjan(TableSet v, Set<Set<TableSet>> components) { // Set the depth index for v to
     * the smallest unused index v.index = index; v.low_link = index; index++; tarjanStack.push(v); v.onStack = true;
     * 
     * for(TableSet d : v.depends_on) { if(d.index == -1) { // Successor w has not yet been visited; recurse on it
     * stronglyConnectedTarjan(d, components); v.add(d.value); d.low_link = Math.min(v.low_link, d.low_link); } else
     * if(d.onStack) { // Successor w is in stack S and hence in the current SCC v.low_link = Math.min(v.low_link,
     * d.index); } }
     * 
     * TableSet t; // If v is a root node, pop the stack and generate an SCC if(v.low_link == v.index) { Set<TableSet>
     * component = Sets.newHashSet(); do { t = tarjanStack.pop(); t.onStack = false; t.add(v.value); component.add(t); }
     * while(t != v); components.add(component); } }
     */

    private void createLabels() {
        BiMap<IProduction, Integer> labels = HashBiMap.create();

        for(IProduction p : grammar.getUniqueProductionMapping().values()) {
            labels.put(p, prodLabelFactory.getNextLabel());
        }

        productionLabels = labels;
    }

    // private BiMap<IProduction, Integer> createLabels(Map<UniqueProduction, IProduction> prods,
    // Map<IProduction, ContextualProduction> contextual_prods) {
    // BiMap<IProduction, Integer> labels = HashBiMap.create();
    //
    // if(!dataDependent) {
    // deriveContextualProductions();
    // } else {
    // // the productions for the contextual symbol are the same as the ones for the original symbol
    // for(ContextualProduction p : grammar.getProdContextualProdMapping().values()) {
    // for(Symbol s : p.rightHand()) {
    // if(s instanceof ContextualSymbol) {
    // grammar.getContextualSymbols().add((ContextualSymbol) s);
    // Set<IProduction> productions =
    // grammar.getSymbolProductionsMapping().get(((ContextualSymbol) s).getOrigSymbol());
    // grammar.getSymbolProductionsMapping().putAll(s, productions);
    // }
    // }
    // }
    // }
    //
    // for(IProduction p : prods.values()) {
    // if(contextual_prods.containsKey(p)) {
    // labels.put(contextual_prods.get(p), prodLabelFactory.getNextLabel());
    // } else {
    // labels.put(p, prodLabelFactory.getNextLabel());
    // }
    // }
    //
    // for(ContextualProduction p : grammar.getDerivedContextualProds()) {
    // labels.put(p, prodLabelFactory.getNextLabel());
    // }
    //
    // for(int i = 0; i < labels.size(); i++) {
    // IProduction p = labels.inverse().get(i + FIRST_PRODUCTION_LABEL);
    // IProduction orig_p = p;
    // if(p instanceof ContextualProduction) {
    // orig_p = ((ContextualProduction) p).getOrigProduction();
    // }
    // ParseTableProduction prod = new ParseTableProduction(i + FIRST_PRODUCTION_LABEL, p,
    // grammar.getProductionAttributesMapping().get(orig_p));
    // productions.add(prod);
    // productionsMapping.put(p, prod);
    // }
    //
    // return labels;
    // }


    private void updateLabelsContextualProductions() {
        BiMap<IProduction, Integer> labels = productionLabels;

        if(!dataDependent) {
            deriveContextualProductions();

            for(IProduction p : grammar.getUniqueProductionMapping().values()) {
                if (grammar.getProdContextualProdMapping().containsKey(p)) {
                    labels.inverse().put(labels.get(p), grammar.getProdContextualProdMapping().get(p));
                }
            }

            for(ContextualProduction p : grammar.getDerivedContextualProds()) {
                labels.put(p, prodLabelFactory.getNextLabel());
            }
        } else {
            // the productions for the contextual symbol are the same as the ones for the original symbol
            for(ContextualProduction p : grammar.getProdContextualProdMapping().values()) {
                for(Symbol s : p.rightHand()) {
                    if(s instanceof ContextualSymbol) {
                        grammar.getContextualSymbols().add((ContextualSymbol) s);
                        Set<IProduction> productions =
                            grammar.getSymbolProductionsMapping().get(((ContextualSymbol) s).getOrigSymbol());
                        grammar.getSymbolProductionsMapping().putAll(s, productions);
                    }
                }
            }

            for(IProduction p : grammar.getUniqueProductionMapping().values()) {
                if (grammar.getProdContextualProdMapping().containsKey(p)) {
                    labels.inverse().put(labels.get(p), grammar.getProdContextualProdMapping().get(p));
                }
            }
        }
    }

    private void createJSGLRParseTableProductions(BiMap<IProduction, Integer> labels) {
        for(int i = 0; i < labels.size(); i++) {
            IProduction p = labels.inverse().get(i + FIRST_PRODUCTION_LABEL);
            IProduction orig_p = p;
            if(p instanceof ContextualProduction) {
                orig_p = ((ContextualProduction) p).getOrigProduction();
            }

            ParseTableProduction prod = new ParseTableProduction(i + FIRST_PRODUCTION_LABEL, p,
                grammar.getProductionAttributesMapping().get(orig_p), leftmostContextsMapping,
                rightmostContextsMapping);
            productions.add(prod);
            productionsMapping.put(p, prod);
        }
    }

    private void deriveContextualProductions() {
        for(ContextualProduction p : grammar.getProdContextualProdMapping().values()) {
            for(Symbol s : p.rightHand()) {
                if(s instanceof ContextualSymbol) {
                    grammar.getContextualSymbols().add((ContextualSymbol) s);
                }
            }
        }

        Queue<ContextualSymbol> contextual_symbols = Queues.newArrayDeque(grammar.getContextualSymbols());
        Set<ContextualSymbol> processed_symbols = Sets.newHashSet();

        while(!contextual_symbols.isEmpty()) {
            ContextualSymbol ctx_s = contextual_symbols.poll();
            if(processed_symbols.contains(ctx_s))
                continue;
            processed_symbols.add(ctx_s);
            if(!getCtxUniqueInt().containsKey(ctx_s.getContexts())) {
                getCtxUniqueInt().put(ctx_s.getContexts(), getCtxUniqueInt().size());
            }

            for(IProduction p : grammar.getSymbolProductionsMapping().get(ctx_s.getOrigSymbol())) {
                int labelP = productionLabels.get(p);

                // generate new productions for deep contexts
                Context deepLeft_ctx = new Context(labelP, ContextType.DEEP, ContextPosition.LEFTMOST, false, leftmostContextsMapping, rightmostContextsMapping);
                Context deepRight_ctx = new Context(labelP, ContextType.DEEP, ContextPosition.RIGHTMOST, false, leftmostContextsMapping, rightmostContextsMapping);
                if(ctx_s.getContexts().contains(deepLeft_ctx) || ctx_s.getContexts().contains(deepRight_ctx)) {
                    continue;
                }

                ContextualProduction ctx_p = null;
                if(grammar.getProdContextualProdMapping().get(p) != null) {
                    ctx_p = grammar.getProdContextualProdMapping().get(p);
                }

                if(ctx_p != null) {
                    ContextualProduction new_prod = ctx_p.mergeContext(ctx_s.getContexts(), contextual_symbols,
                        processed_symbols, grammar.getProductionAttributesMapping(), this);
                    grammar.getDerivedContextualProds().add(new_prod);
                    grammar.getSymbolProductionsMapping().put(ctx_s, new_prod);
                } else if(!(ctx_s.getContexts().contains(deepLeft_ctx)
                    || ctx_s.getContexts().contains(deepRight_ctx))) {
                    ContextualProduction new_prod = new ContextualProduction(p, ctx_s.getContexts(), contextual_symbols,
                        processed_symbols, grammar.getProductionAttributesMapping(), productionLabels.get(p), this);
                    grammar.getDerivedContextualProds().add(new_prod);
                    grammar.getSymbolProductionsMapping().put(ctx_s, new_prod);
                }
            }
        }
    }

    private void processState(State state) {
        state.closure();
        //state.calculateFirstFollowSets();
        state.doShift();
        state.doReduces();
        state.calculateActionsForCharacter();
        state.setStatus(StateStatus.PROCESSED);
    }

    public boolean isDataDependent() {
        return dataDependent;
    }

    public void setGrammar(NormGrammar grammar) {
        this.grammar = grammar;
    }

    public IState startState() {
        State s0;
        if(totalStates == 0) {
            s0 = new State(initialProduction(), this);
            s0.closure();
            s0.doShift();
            s0.doReduces();
            s0.calculateActionsForCharacter();
            s0.setStatus(StateStatus.PROCESSED);
            setProcessedStates(getProcessedStates() + 1);
        } else if(((State) stateLabels.get(0)).status() != StateStatus.PROCESSED) {
            s0 = (State) stateLabels.get(0);
            if(s0.status() == StateStatus.DIRTY) {
                // TODO: garbage collection of unreferenced states
                // uncheckOldReferences(s0.gotos());
                s0.gotos().clear();
            }
            s0.closure();
            s0.doShift();
            s0.doReduces();
            s0.calculateActionsForCharacter();
            s0.setStatus(StateStatus.PROCESSED);
        } else {
            return stateLabels.get(0);
        }
        return s0;
    }

    public IState getState(int index) {
        State s = (State) stateLabels.get(index);
        if(s.status() != StateStatus.PROCESSED) {
            if(s.status() == StateStatus.DIRTY) {
                // TODO: garbage collection of unreferenced states
                // uncheckOldReferences(s0.gotos());
                s.gotos().clear();
            }
            s.closure();
            s.doShift();
            s.doReduces();
            s.calculateActionsForCharacter();
            s.setStatus(StateStatus.PROCESSED);
            setProcessedStates(getProcessedStates() + 1);
        }
        return s;
    }
    
    private Map<Symbol, CharacterClass> firstSetsSLR = Maps.newLinkedHashMap();
    private SetMultimap<Symbol, Symbol> firstSetDependenciesSLR = HashMultimap.create();
    private Map<Symbol, CharacterClass> followSetsSLR = Maps.newLinkedHashMap();
    private SetMultimap<Symbol, Symbol> followSetDependenciesSLR = HashMultimap.create();
    
    private Map<State, Map<Symbol, CharacterClass>> firstSetsLR = Maps.newLinkedHashMap();
    private Map<State, SetMultimap<Symbol, Symbol>> firstSetDependenciesLR = Maps.newLinkedHashMap();
    private Map<State, Map<Symbol, CharacterClass>> followSetsLR = Maps.newLinkedHashMap();
    private Map<State, SetMultimap<Symbol, Symbol>> followSetDependenciesLR = Maps.newLinkedHashMap();
    
    private Map<Symbol, boolean[]> symbolsVisited = Maps.newLinkedHashMap();

    public void calculateSLRFirstSets() {
    	for(IProduction prod : productionsMapping.keySet()) {
    		Symbol s = prod.leftHand();
    		if(firstSetsSLR.get(s) == null) {
    			CharacterClass cc = new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
				firstSetsSLR.put(s, cc);
                symbolsVisited.put(s, new boolean[] {false, false, false, false});
    		}
    		calculateSLRFirstSetInitial(s);
    	}
    	for(IProduction prod : productionsMapping.keySet()) {
    		Symbol s = prod.leftHand();
    		calculateSetDependencies(CALCFIRST, s, firstSetsSLR, firstSetDependenciesSLR, symbolsVisited);
    	}
    }
    
    public void calculateSLRFirstSetInitial(Symbol s) {
    	for(IProduction prod : productionsMapping.keySet()) {
    		int i = 0;
    		Symbol rhsSymbol = prod.rightHand().get(0);
    		
    		while(i < prod.rightHand().size() && (i == 0 || rhsSymbol.isNullable())) {
    			rhsSymbol = prod.rightHand().get(i);
    			if(s.equals(prod.leftHand())) {
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
    
    public void calculateSLRFollowSets() {
    	for(IProduction prod : productionsMapping.keySet()) {
    		Symbol s = prod.leftHand();
    		if(followSetsSLR.get(s) == null) {
    			CharacterClass cc = new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
				followSetsSLR.put(s, cc);
    		}
    		calculateSLRFollowSetInitial(s);
    	}
    	for(IProduction prod : productionsMapping.keySet()) {
    		Symbol s = prod.leftHand();
    		calculateSetDependencies(CALCFOLLOW, s, followSetsSLR, followSetDependenciesSLR, symbolsVisited);
    	}
    }
    
    public void calculateSLRFollowSetInitial(Symbol s) {
    	for(IProduction prod : productionsMapping.keySet()) {
    		for(int sIndex = 0; sIndex < prod.rightHand().size(); sIndex++) {
	    		if(prod.rightHand().get(sIndex).equals(s)) {
	    			if(sIndex == prod.rightHand().size()-1) {
	    				Symbol sLHS = prod.leftHand();
	    				followSetDependenciesSLR.put(s, sLHS);
	    			}
	    			int i = 1;
	        		Symbol sNext = null;
	        		if(sIndex+i < prod.rightHand().size()) {
	        			sNext = prod.rightHand().get(sIndex+i);
	        		}
	        		
	        		while(sNext != null && sIndex+i < prod.rightHand().size() && (i == 1 || sNext.isNullable())) {
	            		sNext = prod.rightHand().get(sIndex+i);
	    				if (prod.rightHand().get(sIndex+1) instanceof CharacterClass) {
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
    
    
    
    
    
    
    // LR
    
    public void calculateLRFirstSets(State state) {
    	Map<Symbol, CharacterClass> stateFirstSets = firstSetsLR.get(state);
    	SetMultimap<Symbol, Symbol> stateFirstSetDependencies = firstSetDependenciesLR.get(state);
    	
    	if(stateFirstSets == null) {
    		stateFirstSets = Maps.newLinkedHashMap();
    		firstSetsLR.put(state, stateFirstSets);
    	}
    	if(stateFirstSetDependencies == null) {
    		stateFirstSetDependencies = HashMultimap.create();
    		firstSetDependenciesLR.put(state, stateFirstSetDependencies);
    	}
    	for(LRItem item : state.getItems()) {
    		IProduction prod = item.getProd();
    		Symbol s = prod.leftHand();
    		
    		if(stateFirstSets.get(s) == null) {
    			CharacterClass cc = new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
    			stateFirstSets.put(s, cc);
                symbolsVisited.put(s, new boolean[] {false, false, false, false});
    		}
    		calculateLRFirstSetInitial(state, s);
    	}
    	for(LRItem item : state.getItems()) {
    		IProduction prod = item.getProd();
    		Symbol s = prod.leftHand();
    		calculateSetDependencies(CALCFIRST, s, stateFirstSets, stateFirstSetDependencies, symbolsVisited);
    	}
    }
    
    public void calculateLRFirstSetInitial(State state, Symbol s) {
    	Map<Symbol, CharacterClass> stateFirstSets = firstSetsLR.get(state);
    	SetMultimap<Symbol, Symbol> stateFirstSetDependencies = firstSetDependenciesLR.get(state);
    	
    	for(LRItem item : state.getItems()) {
    		int i = 0;
    		IProduction prod = item.getProd();
    		Symbol rhsSymbol = prod.rightHand().get(0);
    		
    		while(i < prod.rightHand().size() && (i == 0 || rhsSymbol.isNullable())) {
    			rhsSymbol = prod.rightHand().get(i);
    			if(s.equals(prod.leftHand())) {
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
    
    public void calculateLRFollowSets(State state) {
    	Map<Symbol, CharacterClass> stateFollowSets = followSetsLR.get(state);
    	SetMultimap<Symbol, Symbol> stateFollowSetDependencies = followSetDependenciesLR.get(state);
    	
    	if(stateFollowSets == null) {
    		stateFollowSets = Maps.newLinkedHashMap();
    		firstSetsLR.put(state, stateFollowSets);
    	}
    	if(stateFollowSetDependencies == null) {
    		stateFollowSetDependencies = HashMultimap.create();
    		firstSetDependenciesLR.put(state, stateFollowSetDependencies);
    	}
    	for(LRItem item : state.getItems()) {
    		IProduction prod = item.getProd();
    		Symbol s = prod.leftHand();
    		if(stateFollowSets.get(s) == null) {
    			CharacterClass cc = new CharacterClass(CharacterClassFactory.EMPTY_CHARACTER_CLASS);
    			stateFollowSets.put(s, cc);
    		}
    		calculateLRFollowSetInitial(state, s);
    	}
    	for(LRItem item : state.getItems()) {
    		IProduction prod = item.getProd();
    		Symbol s = prod.leftHand();
    		calculateSetDependencies(CALCFOLLOW, s, stateFollowSets, stateFollowSetDependencies, symbolsVisited);
    	}
    	for(LRItem item : state.getItems()) {
    		Symbol s = item.getProd().leftHand();
    		List<ICharacterClass> itemLookahead = new ArrayList();
    		itemLookahead.add(stateFollowSets.get(s));
    		item.setLookahead(itemLookahead);
    	}
    }
    
    public void calculateLRFollowSetInitial(State state, Symbol s) {
    	Map<Symbol, CharacterClass> stateFirstSets = firstSetsLR.get(state);
    	Map<Symbol, CharacterClass> stateFollowSets = followSetsLR.get(state);
    	SetMultimap<Symbol, Symbol> stateFollowSetDependencies = followSetDependenciesLR.get(state);
    	
    	for(LRItem item : state.getItems()) {
    		IProduction prod = item.getProd();
    		int startIndex = item.getDotPosition();
    		
    		for(int sIndex = startIndex; sIndex < prod.rightHand().size(); sIndex++) {
	    		if(prod.rightHand().get(sIndex).equals(s)) {
	    			if(sIndex == prod.rightHand().size()-1) {
	    				Symbol sLHS = prod.leftHand();
	    				stateFollowSetDependencies.put(s, sLHS);
	    			}
	    			int i = 1;
	        		Symbol sNext = null;
	        		if(sIndex+i < prod.rightHand().size()) {
	        			sNext = prod.rightHand().get(sIndex+i);
	        		}
	        		
	        		while(sNext != null && sIndex+i < prod.rightHand().size() && (i == 1 || sNext.isNullable())) {
	            		sNext = prod.rightHand().get(sIndex+i);
	    				if (prod.rightHand().get(sIndex+1) instanceof CharacterClass) {
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
    
    
    
    public void calculateSetDependencies(int type, Symbol s, Map<Symbol, CharacterClass> sets, SetMultimap<Symbol, Symbol> setDependencies, Map<Symbol, boolean[]> symbolsVisited) {
    	int startCycle;
    	if (type == CALCFOLLOW) {
    		startCycle = 2;
    	} else {
    		startCycle = 0;
    	}
    	
    	for(int cycle = startCycle; cycle < startCycle + 2; cycle++) {
	    	Set<Symbol> sDependencies = setDependencies.get(s);
	    	Deque<Symbol[]> stack = Lists.newLinkedList();
	    	for(Symbol sDependency : sDependencies) {
	    		stack.push(new Symbol[] {s, sDependency});
	    	}
	    	
	    	while(!stack.isEmpty()) {
	    		Symbol[] symbols = stack.peek();
	    		Symbol sSuper = symbols[0];
	    		Symbol sSub = symbols[1];
	    		boolean sSubVisited = symbolsVisited.get(sSub)[cycle];
	    		
	    		if (sSubVisited) {
	    			stack.pop();
	    		} else {
	                symbolsVisited.get(sSub)[cycle] = true;
		    		
		    		Set<Symbol> nestedDependencies = setDependencies.get(sSub);
		    		if(nestedDependencies.isEmpty()) {
		    			sets.put(sSuper, CharacterClass.union(sets.get(sSuper), sets.get(sSub)));
		    			stack.pop();
		    		} else {
		    			boolean processNested = false;
		    			for(Symbol sNested : nestedDependencies) {
	                        boolean sNestedIsVisited = symbolsVisited.get(sNested)[cycle];
		    				if(sNestedIsVisited) {
		    					sets.put(sSub, CharacterClass.union(sets.get(sSub), sets.get(sNested)));
		    				} else {
		    					stack.push(new Symbol[] {sSub, sNested});
		    					processNested = true;
		    				}
		    			}
		    			if (!processNested) {
		    				sets.put(sSuper, CharacterClass.union(sets.get(sSuper), sets.get(sSub)));
		    				stack.pop();
		    			}
		    		}
	    		}
	    	}
    	}
    }
    
    public CharacterClass getFollowSet(Symbol s, State state) {
    	if (kLookahead == 1 && parseTableGenType == ParseTableGenType.SLR) {
    		return followSetsSLR.get(s);
    	} else if (kLookahead == 1 && (parseTableGenType == ParseTableGenType.LR || parseTableGenType == ParseTableGenType.LALR)) {
    		return state.getFollowSet(s);
    	} else {
    		return CharacterClass.getFullCharacterClass();
    	}
//    	ICharacterClassFactory ccFactory = new CharacterClassFactory(true, true);
//    	return new CharacterClass(ccFactory.fromSingle(97));
    }
    

    private void processStateQueue() {
        while(!stateQueue.isEmpty()) {
            State state = stateQueue.poll();
            if(state.status() != StateStatus.PROCESSED) {
                processState(state);
            }
        }
    }

    public int totalStates() {
        return totalStates;
    }

    public int getProcessedStates() {
        return processedStates;
    }

    public void setProcessedStates(int processedStates) {
        this.processedStates = processedStates;
    }

    public void incTotalStates() {
        totalStates++;
    }

    public Map<Set<LRItem>, State> kernelMap() {
        return kernelStatesMapping;
    }

    public IProduction initialProduction() {
        return initialProduction;
    }

    public NormGrammar normalizedGrammar() {
        return grammar;
    }

    public BiMap<IProduction, Integer> productionLabels() {
        return productionLabels;
    }

    public LabelFactory getProdLabelFactory() {
        return prodLabelFactory;
    }

    public Map<LRItem, Set<LRItem>> cachedItems() {
        return itemDerivedItemsCache;
    }

    public Queue<State> stateQueue() {
        return stateQueue;
    }

    public Map<Integer, State> stateLabels() {
        return stateLabels;
    }

    public int getVersionNumber() {
        return VERSION_NUMBER;
    }

    public int getInitialStateNumber() {
        return initialStateNumber;
    }

    public Map<Set<Context>, Integer> getCtxUniqueInt() {
        return ctxUniqueInt;
    }

    public void setCtxUniqueInt(Map<Set<Context>, Integer> ctx_vals) {
        this.ctxUniqueInt = ctx_vals;
    }

    public SymbolStatesMapping getSymbolStatesMapping() {
        return symbolStatesMapping;
    }

    public void setSymbolStatesMapping(SymbolStatesMapping symbolStatesMapping) {
        this.symbolStatesMapping = symbolStatesMapping;
    }

    public List<org.metaborg.parsetable.IProduction> productions() {
        return productions;
    }

    public Map<IProduction, ParseTableProduction> productionsMapping() {
        return productionsMapping;
    }

    public Map<Integer, Integer> getLeftmostContextsMapping() {
        return leftmostContextsMapping;
    }

    public Map<Integer, Integer> getRightmostContextsMapping() {
        return rightmostContextsMapping;
    }

    @Override public IState getStartState() {
        return startState();
    }
    
    public ParseTableGenType getParseTableGenType() {
		return parseTableGenType;
	}

	public void setParseTableGenType(ParseTableGenType parseTableGenType) {
		this.parseTableGenType = parseTableGenType;
	}

	public int getK() {
		return kLookahead;
	}

	public void setK(int k) {
		this.kLookahead = k;
	}



}
