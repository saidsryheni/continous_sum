import java.util.*;

public class Node {

    private int nodeId;
    private String nodeName;
    private List<Node> children;
    private List<Node> parents;
    private boolean isOntologyNode;

    public Node(int nodeId, String nodeName){
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        children = new ArrayList<>();
        parents = new ArrayList<>();
        isOntologyNode = nodeName.contains("ontology") || nodeName.contains("www.w3.org");
    }

    public void addChild(Node child){
        children.add(child);
        child.parents.add(this);
    }

    public void addParent(Node parent){
        parent.addChild(this);
    }

    // ============================ Setters and Getters ============================

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public List<Node> getParents() {
        return parents;
    }

    public void setParents(List<Node> parents) {
        this.parents = parents;
    }

    public boolean isOntologyNode() {
        return isOntologyNode;
    }

    public void setOntologyNode(boolean ontologyNode) {
        isOntologyNode = ontologyNode;
    }
}
