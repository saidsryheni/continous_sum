import java.util.*;

public class Conflict {

    private int conflictId;
    private String subject;
    private String predicate;
    private List<Triple> triples;
    private List<Fact> facts;
    private Set<Fact> correctFacts;
    private Map<Node, Fact> nodeFactMap;

    public static String calcConflictName(String subject, String predicate){
        return subject + "|" + predicate;
    }

    public Conflict(int conflictId, String subject, String predicate) {
        this.conflictId = conflictId;
        this.subject = subject;
        this.predicate = predicate;
        this.triples = new ArrayList<>();
        this.facts = new ArrayList<>();
    }

    // ============================ Algorithm ============================

    public int getNumOfTriples(){
        return triples.size();
    }

    public void updateScoresIfNeeded(){
        if(getNumOfTriples() % Constants.CONFLICT_PERIOD != 0) return;
        facts.forEach(Fact::updateScores);
    }

    public void calcCorrectFacts(){
        nodeFactMap = new HashMap<>();
        correctFacts = new HashSet<>();
        double maxValue = 0;
        for (Fact fact : facts) {
            nodeFactMap.putIfAbsent(fact.getGraphNode(), fact);
            if(fact.getGraphNode().isOntologyNode()) continue;
            if(fact.getCorrectness() - Constants.EPS > maxValue){
                maxValue = fact.getCorrectness();
                correctFacts.clear();
                correctFacts.add(fact);
            }
            else if(Math.abs(fact.getCorrectness() - maxValue) <= Constants.EPS){
                correctFacts.add(fact);
            }
        }
        Set<Fact> maxChildren;
        while(!correctFacts.isEmpty()){
            maxChildren = new HashSet<>();
            maxValue = 0;
            for (Fact fact : correctFacts){
                List<Node> children = fact.getGraphNode().getChildren();
                for (Node child : children){
                    if(child.isOntologyNode()) continue;
                    if(!nodeFactMap.containsKey(child)) continue;
                    Fact childFact = nodeFactMap.get(child);
                    if(childFact.getCorrectness() - Constants.EPS > maxValue){
                        maxValue = childFact.getCorrectness();
                        maxChildren.clear();
                        maxChildren.add(childFact);
                    }
                    else if(Math.abs(childFact.getCorrectness() - maxValue) <= Constants.EPS){
                        maxChildren.add(childFact);
                    }
                }
            }
            if(maxChildren.isEmpty()) break;
            correctFacts = maxChildren;
        }

        maxChildren = correctFacts;
        correctFacts = new HashSet<>();
        for (Fact factChild : maxChildren){
            List<Node> ancestorNodes = Graph.getInstance().getAncestors(factChild.getGraphNode().getNodeId());
            List<Fact> ancestorFacts = new ArrayList<>();
            for (Node node : ancestorNodes){
                if(nodeFactMap.containsKey(node)) ancestorFacts.add(nodeFactMap.get(node));
            }
            correctFacts.addAll(ancestorFacts);
        }
    }

    // ============================ Storage ============================

    public void addTriple(Triple triple){
        triples.add(triple);
    }

    public void addFact(Fact fact){
        facts.add(fact);
    }

    public Fact getFact(Node node){
        return nodeFactMap.get(node);
    }

    // ============================ Setters and Getters ============================

    public int getConflictId() {
        return conflictId;
    }

    public void setConflictId(int conflictId) {
        this.conflictId = conflictId;
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

    public List<Triple> getTriples() {
        return triples;
    }

    public void setTriples(List<Triple> triples) {
        this.triples = triples;
    }

    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }

    public Set<Fact> getCorrectFacts() {
        return correctFacts;
    }

    public void setCorrectFacts(Set<Fact> correctFacts) {
        this.correctFacts = correctFacts;
    }

    public Map<Node, Fact> getNodeFactMap() {
        return nodeFactMap;
    }

    public void setNodeFactMap(Map<Node, Fact> nodeFactMap) {
        this.nodeFactMap = nodeFactMap;
    }
}
