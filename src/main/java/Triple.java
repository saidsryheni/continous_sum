public class Triple {

    private int tripleId;
    private Source source;
    private Fact fact;
    private long version;
    private int numOfIterations;
    private boolean isExplicit;
    private double addedValueToSource;
    private double addedValueToFact;

    public Triple(int tripleId, Source source, Fact fact, boolean isExplicit) {
        this.tripleId = tripleId;
        this.source = source;
        this.fact = fact;
        version = TripleStore.getInstance().getTimer();
        this.isExplicit = isExplicit;
        addedValueToFact = 0;
        addedValueToSource = 0;
    }

    // ============================ Algorithm ============================

    public void updateVersion(){
        version = TripleStore.getInstance().getTimer();
    }

    public void updateNumOfIterations(){
        ++numOfIterations;
    }

    public void updateTriple(){
        updateVersion();
        updateNumOfIterations();
    }

    // ============================ Setters and Getters ============================

    public int getTripleId() {
        return tripleId;
    }

    public void setTripleId(int tripleId) {
        this.tripleId = tripleId;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Fact getFact() {
        return fact;
    }

    public void setFact(Fact fact) {
        this.fact = fact;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public double getAddedValueToSource() {
        return addedValueToSource;
    }

    public void setAddedValueToSource(double addedValueToSource) {
        this.addedValueToSource = addedValueToSource;
    }

    public double getAddedValueToFact() {
        return addedValueToFact;
    }

    public void setAddedValueToFact(double addedValueToFact) {
        this.addedValueToFact = addedValueToFact;
    }

    public int getNumOfIterations() {
        return numOfIterations;
    }

    public void setNumOfIterations(int numOfIterations) {
        this.numOfIterations = numOfIterations;
    }

    public boolean isExplicit() {
        return isExplicit;
    }

    public void setExplicit(boolean explicit) {
        isExplicit = explicit;
    }
}
