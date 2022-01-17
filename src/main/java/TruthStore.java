import java.util.*;

public class TruthStore {

    // ============ Storage ============
    private static TruthStore instance;
    private Map<Conflict, Fact> correctFact;
    private int tp;
    private int fn;
    private int fp;
    private int tn;

    private TruthStore(){
        correctFact = new HashMap<>();
        tp = fn = fp = tn = 0;
    }

    public static TruthStore getInstance(){
        if(instance == null) instance = new TruthStore();
        return instance;
    }

    public static void clearInstance(){
        instance = null;
    }

    // ============ Algorithm ============
    public void addFact(String subject, String predicate, String object){
        Fact fact = TripleStore.getInstance().getFact(subject, predicate, object);
        correctFact.putIfAbsent(fact.getConflict(), fact);
    }

    public boolean isCorrect(Fact fact){
        return (correctFact.get(fact.getConflict()) == fact);
    }

    public void calcMeasures(){
        TripleStore tripleStore = TripleStore.getInstance();
        for (Conflict conflict : tripleStore.getConflicts()){
            if(conflict.getNodeFactMap() == null) continue;
            Set<Fact> actual = getActualCorrectFacts(conflict);
            Set<Fact> predicted = conflict.getCorrectFacts();
            for(Fact fact : conflict.getFacts()){
                if(actual.contains(fact) && predicted.contains(fact)) ++tp;
                else if(actual.contains(fact) && !predicted.contains(fact)) ++fn;
                else if(!actual.contains(fact) && predicted.contains(fact)) ++fp;
                else ++tn;
            }
        }
    }

    private Set<Fact> getActualCorrectFacts(Conflict conflict){
        List<Node> actualCorrectNodes = Graph.getInstance().getAncestors(correctFact.get(conflict).getGraphNode().getNodeId());
        Set<Fact> actualCorrect = new HashSet<>();
        for (Node node : actualCorrectNodes){
            actualCorrect.add(conflict.getFact(node));
        }
        return actualCorrect;
    }

    public Map<Conflict, Fact> getCorrectFact() {
        return correctFact;
    }

    public void setCorrectFact(Map<Conflict, Fact> correctFact) {
        this.correctFact = correctFact;
    }

    public int getTp() {
        return tp;
    }

    public void setTp(int tp) {
        this.tp = tp;
    }

    public int getFn() {
        return fn;
    }

    public void setFn(int fn) {
        this.fn = fn;
    }

    public int getFp() {
        return fp;
    }

    public void setFp(int fp) {
        this.fp = fp;
    }

    public int getTn() {
        return tn;
    }

    public void setTn(int tn) {
        this.tn = tn;
    }
}
