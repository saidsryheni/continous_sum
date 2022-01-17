import java.util.*;

public class Source {

    private int sourceId;
    private String sourceName;
    private double trustSum;
    private double trust;
    private List<Triple> triples;
    private List<Triple> explicitTriples;
    private double accuracy;
    private int correctTriples;
    private int totalTriples;

    public Source(int sourceId, String sourceName) {
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.trust = Constants.SOURCE_INITIAL_TRUST;
        triples = new ArrayList<>();
        explicitTriples = new ArrayList<>();
    }

    public void addTriple(Triple triple){
        triples.add(triple);
        if(triple.isExplicit()){
            explicitTriples.add(triple);
        }
    }

    public void updateTrust(Triple triple){
        if(!triple.isExplicit()) return;
        double oldCorrectness = triple.getAddedValueToSource();
        double newCorrectness = triple.getFact().getCorrectness();
        double oldTrustSum = trustSum;
        trustSum += newCorrectness - oldCorrectness;
        triple.setAddedValueToSource(newCorrectness);
        TripleStore.getInstance().updateMaxSourceTrust(oldTrustSum, trustSum);
        double maxValue = TripleStore.getInstance().getMaxSourceTrust();
        trust = trustSum / maxValue;
    }

    public int getNumOfTriples(){
        return explicitTriples.size();
    }

    public void updateScoresIfNeeded(){
        if(getNumOfTriples() % Constants.SOURCE_PERIOD != 0) return;
        double oldTrustSum = trustSum;
        trustSum = 0;
        for(Triple triple : explicitTriples){
            double correctness = triple.getFact().getCorrectness();
            trustSum += correctness;
            triple.setAddedValueToSource(correctness);
        }
        TripleStore.getInstance().updateMaxSourceTrust(oldTrustSum, trustSum);
        double maxValue = TripleStore.getInstance().getMaxSourceTrust();
        trust = trustSum / maxValue;
    }

    public void calcAccuracy(){
        correctTriples = 0;
        totalTriples = 0;
        for (Triple triple : explicitTriples){
            if(triple.getFact().getConflict().getCorrectFacts().contains(triple.getFact())) correctTriples++;
            totalTriples++;
        }
        accuracy = correctTriples / (double)totalTriples;
    }

    // ============================ Setters and Getters ============================

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public List<Triple> getTriples() {
        return triples;
    }

    public void setTriples(List<Triple> triples) {
        this.triples = triples;
    }

    public double getTrustSum() {
        return trustSum;
    }

    public void setTrustSum(double trustSum) {
        this.trustSum = trustSum;
    }

    public double getTrust() {
        return trust;
    }

    public void setTrust(double trust) {
        this.trust = trust;
    }

    public List<Triple> getExplicitTriples() {
        return explicitTriples;
    }

    public void setExplicitTriples(List<Triple> explicitTriples) {
        this.explicitTriples = explicitTriples;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public int getCorrectTriples() {
        return correctTriples;
    }

    public void setCorrectTriples(int correctTriples) {
        this.correctTriples = correctTriples;
    }
}
