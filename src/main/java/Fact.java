import java.util.*;

public class Fact {

    private int factId;
    private Node graphNode;
    private String subject;
    private String predicate;
    private String object;
    private double correctnessSum;
    private double correctness;
    private Conflict conflict;
    private List<Triple> triples;


    public static String calcFactName(String subject, String predicate, String object){
        return subject + "|" + predicate + "|" + object;
    }

    public Fact(int factId, String subject, String predicate, String object) {
        this.factId = factId;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.graphNode = Graph.getInstance().getNode(object);
        this.correctnessSum = 0;
        this.conflict = TripleStore.getInstance().getConflict(subject, predicate);
        this.triples = new ArrayList<>();
    }

    public void addTriple(Triple triple){
        triples.add(triple);
    }

    public void updateCorrectness(Triple triple){
        double oldTrust = triple.getAddedValueToFact();
        double newTrust = triple.getSource().getTrust();
        double oldCorrectnessSum = correctnessSum;
        correctnessSum += newTrust - oldTrust;
        triple.setAddedValueToFact(newTrust);
        TripleStore.getInstance().updateMaxFactCorrectness(oldCorrectnessSum, correctnessSum);
        double maxValue = TripleStore.getInstance().getMaxFactCorrectness();
        correctness = correctnessSum / maxValue;
    }

    public void updateScores(){
        double oldCorrectnessSum = correctnessSum;
        correctnessSum = 0;
        for(Triple triple : triples){
            double trust = triple.getSource().getTrust();
            correctnessSum += trust;
            triple.setAddedValueToFact(trust);
        }
        TripleStore.getInstance().updateMaxFactCorrectness(oldCorrectnessSum, correctnessSum);
        double maxValue = TripleStore.getInstance().getMaxFactCorrectness();
        correctness = correctnessSum / maxValue;
    }

    // ============================ Setters and Getters ============================

    public int getFactId() {
        return factId;
    }

    public void setFactId(int factId) {
        this.factId = factId;
    }

    public Node getGraphNode() {
        return graphNode;
    }

    public void setGraphNode(Node graphNode) {
        this.graphNode = graphNode;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getObject() {
        return object;
    }

    public Conflict getConflict() {
        return conflict;
    }

    public void setConflict(Conflict conflict) {
        this.conflict = conflict;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public List<Triple> getTriples() {
        return triples;
    }

    public void setTriples(List<Triple> triples) {
        this.triples = triples;
    }

    public double getCorrectnessSum() {
        return correctnessSum;
    }

    public void setCorrectnessSum(double correctnessSum) {
        this.correctnessSum = correctnessSum;
    }

    public double getCorrectness() {
        return correctness;
    }

    public void setCorrectness(double correctness) {
        this.correctness = correctness;
    }
}
