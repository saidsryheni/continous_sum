import java.util.*;

public class TripleStore {

    // ============ Storage ============
    private static TripleStore instance;
    private List<Triple> triples;
    private List<Source> sources;
    private List<Fact> facts;
    private List<Conflict> conflicts;
    private Map<String, Integer> sourcesIdMap;
    private Map<String, Integer> factsIdMap;
    private Map<String, Integer> conflictsIdMap;
    private Deque<Triple> triplesQueue;

    private long timer;
    private double maxFactsCorrectness;
    private double maxSourcesTrust;

    private TripleStore(){
        triples = new ArrayList<>();
        sources = new ArrayList<>();
        facts = new ArrayList<>();
        conflicts = new ArrayList<>();
        sourcesIdMap = new HashMap<>();
        factsIdMap = new HashMap<>();
        conflictsIdMap = new HashMap<>();
        triplesQueue = new LinkedList<>();
        timer = 0;
        maxFactsCorrectness = maxSourcesTrust = 0.0;
    }

    public static TripleStore getInstance(){
        if(instance == null) instance = new TripleStore();
        return instance;
    }

    public static void clearInstance(){
        instance = null;
    }

    // ============================ Algorithm ============================

    public void updateMaxFactCorrectness(double oldCorrectness, double newCorrectness){
        maxFactsCorrectness = Math.max(maxFactsCorrectness, newCorrectness);
        // maxFactsCorrectness.remove(oldCorrectness);
        // maxFactsCorrectness.add(newCorrectness);
    }

    public void updateMaxSourceTrust(double oldTrust, double newTrust){
        maxSourcesTrust = Math.max(maxSourcesTrust, newTrust);
        // maxSourcesTrust.remove(oldTrust);
        // maxSourcesTrust.add(newTrust);
    }

    public double getMaxFactCorrectness(){
        return maxFactsCorrectness;
        // return maxFactsCorrectness.last();
    }

    public double getMaxSourceTrust(){
        return maxSourcesTrust;
        // return maxSourcesTrust.last();
    }

    public void updateOldTriples(){
        while(!triplesQueue.isEmpty()){
            Triple triple = triplesQueue.getFirst();
            if(triple.getVersion() + Constants.STORE_PERIOD > timer) break;
            triplesQueue.removeFirst();
            triple.getFact().updateCorrectness(triple);
            triple.getSource().updateTrust(triple);
            triple.updateTriple();
            if(triple.getNumOfIterations() < Constants.MAX_TRIPLE_ITERATIONS){
                triplesQueue.addLast(triple);
            }
        }
    }

    // ============================ Storage ============================

    public int getSourceId(String sourceName){
        sourcesIdMap.putIfAbsent(sourceName, sources.size());
        int sourceId = sourcesIdMap.get(sourceName);
        if(sourceId == sources.size()){
            Source source = new Source(sourceId, sourceName);
            sources.add(source);
            updateMaxSourceTrust(0.0, source.getTrustSum());
            // maxSourcesTrust.add(source.getTrust());
        }
        return sourceId;
    }

    public Source getSource(int sourceId){
        assert(sourceId >= 0 && sourceId < sources.size());
        return sources.get(sourceId);
    }

    public Source getSource(String sourceName){
        int sourceId = getSourceId(sourceName);
        return getSource(sourceId);
    }

    public int getFactId(String subject, String predicate, String object){
        String factName = Fact.calcFactName(subject, predicate, object);
        factsIdMap.putIfAbsent(factName, facts.size());
        int factId = factsIdMap.get(factName);
        if(factId == facts.size()){
            Fact fact = new Fact(factId, subject, predicate, object);
            fact.getConflict().addFact(fact);
            facts.add(fact);
            updateMaxFactCorrectness(0.0, fact.getCorrectnessSum());
            // maxFactsCorrectness.add(fact.getCorrectness());
        }
        return factId;
    }

    public Fact getFact(int factId){
        assert(factId >= 0 && factId < facts.size());
        return facts.get(factId);
    }

    public Fact getFact(String subject, String predicate, String object){
        int factId = getFactId(subject, predicate, object);
        return getFact(factId);
    }

    public int getTripleId(Source source, Fact fact, boolean isExplicit) {

        int tripleId = triples.size();
        Triple triple = new Triple(tripleId, source, fact, isExplicit);
        source.addTriple(triple);
        fact.getConflict().addTriple(triple);
        fact.addTriple(triple);
        fact.updateCorrectness(triple);
        source.updateTrust(triple);
        triples.add(triple);

        return tripleId;
    }

    public Triple getTriple(int tripleId){
        assert(tripleId >= 0 && tripleId < triples.size());
        return triples.get(tripleId);
    }

    public Triple getTriple(String sourceName, String subject, String predicate, String object, boolean isExplicit){
        Source source = getSource(sourceName);
        Fact fact = getFact(subject, predicate, object);
        int tripleId = getTripleId(source, fact, isExplicit);
        return getTriple(tripleId);
    }

    public Triple getTriple(Source source, Fact fact, boolean isExplicit){
        int tripleId = getTripleId(source, fact, isExplicit);
        return getTriple(tripleId);
    }

    public void addTriple(String sourceName, String subject, String predicate, String object){
        ++timer;
        Fact fact = getFact(subject, predicate, object);
        Source source = getSource(sourceName);
        List<Node> ancestorNodes = Graph.getInstance().getAncestors(object);
        List<Fact> ancestorFacts = new ArrayList<>();
        for(Node node : ancestorNodes) {
            Fact ancestorFact = getFact(subject, predicate, node.getNodeName());
            if(ancestorFact == fact) continue;
            ancestorFacts.add(ancestorFact);
        }
        Triple triple = getTriple(source, fact, true);
        triplesQueue.addLast(triple);
        for(Fact ancestorFact : ancestorFacts){
            Triple ancestorTriple = getTriple(source, ancestorFact, false);
            triplesQueue.addLast(ancestorTriple);
        }
        fact.getConflict().updateScoresIfNeeded();
        source.updateScoresIfNeeded();
        updateOldTriples();
    }

    public int getConflictId(String subject, String predicate){
        String conflictName = Conflict.calcConflictName(subject, predicate);
        conflictsIdMap.putIfAbsent(conflictName, conflicts.size());
        int conflictId = conflictsIdMap.get(conflictName);
        if(conflictId == conflicts.size()){
            Conflict conflict = new Conflict(conflictId, subject, predicate);
            conflicts.add(conflict);
        }
        return conflictId;
    }

    public Conflict getConflict(int conflictId){
        assert(conflictId >= 0 && conflictId < conflicts.size());
        return conflicts.get(conflictId);
    }

    public Conflict getConflict(String subject, String predicate){
        int conflictId = getConflictId(subject, predicate);
        return getConflict(conflictId);
    }

    // ============================ Setters and Getters ============================

    public List<Triple> getTriples() {
        return triples;
    }

    public void setTriples(List<Triple> triples) {
        this.triples = triples;
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }

    public Map<String, Integer> getSourcesIdMap() {
        return sourcesIdMap;
    }

    public void setSourcesIdMap(Map<String, Integer> sourcesIdMap) {
        this.sourcesIdMap = sourcesIdMap;
    }

    public Map<String, Integer> getFactsIdMap() {
        return factsIdMap;
    }

    public void setFactsIdMap(Map<String, Integer> factsIdMap) {
        this.factsIdMap = factsIdMap;
    }

    public long getTimer() {
        return timer;
    }

    public void setTimer(long timer) {
        this.timer = timer;
    }

    public double getMaxFactsCorrectness() {
        return maxFactsCorrectness;
    }

    public void setMaxFactsCorrectness(double maxFactsCorrectness) {
        this.maxFactsCorrectness = maxFactsCorrectness;
    }

    public double getMaxSourcesTrust() {
        return maxSourcesTrust;
    }

    public void setMaxSourcesTrust(double maxSourcesTrust) {
        this.maxSourcesTrust = maxSourcesTrust;
    }

    public List<Conflict> getConflicts() {
        return conflicts;
    }

    public void setConflicts(List<Conflict> conflicts) {
        this.conflicts = conflicts;
    }

    public Map<String, Integer> getConflictsIdMap() {
        return conflictsIdMap;
    }

    public void setConflictsIdMap(Map<String, Integer> conflictsIdMap) {
        this.conflictsIdMap = conflictsIdMap;
    }
}
