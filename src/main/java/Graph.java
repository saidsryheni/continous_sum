import java.util.*;

public class Graph {

    private static Graph instance;
    private final Map<String, Integer> nodesIdMap;
    private final List<Node> nodes;

    private Graph(){
        nodesIdMap = new HashMap<>();
        nodes = new ArrayList<>();
    }

    public static Graph getInstance(){
        if(instance == null) instance = new Graph();
        return instance;
    }

    public static void clearInstance(){
        instance = null;
    }

    // ============================ Algorithm ============================

    public int getNodeId(String nodeName){
        nodesIdMap.putIfAbsent(nodeName, nodes.size());
        int nodeId = nodesIdMap.get(nodeName);
        if(nodeId == nodes.size()){
            Node node = new Node(nodeId, nodeName);
            nodes.add(node);
        }
        return nodeId;
    }

    public Node getNode(int nodeId){
        assert(nodeId >= 0 && nodeId < nodes.size());
        return nodes.get(nodeId);
    }

    public Node getNode(String nodeName){
        int nodeId = getNodeId(nodeName);
        return getNode(nodeId);
    }

    public void addEdge(int from, int to){
        Node fromNode = getNode(from), toNode = getNode(to);
        if (fromNode == toNode) return;
        fromNode.addChild(toNode);
    }

    public void addEdge(String from, String to){
        int fromId = getNodeId(from), toId = getNodeId(to);
        addEdge(fromId, toId);
    }

    public List<Node> getChildren(int nodeId){
        return getNode(nodeId).getChildren();
    }

    public List<Node> getChildren(String nodeName){
        int nodeId = getNodeId(nodeName);
        return getChildren(nodeId);
    }

    public List<Node> getParents(int nodeId){
        return getNode(nodeId).getParents();
    }

    public List<Node> getParents(String nodeName){
        int nodeId = getNodeId(nodeName);
        return getParents(nodeId);
    }

    private void ancestorsDfs(Node node, Set<Integer> visited){
        visited.add(node.getNodeId());
        List<Node> parents = node.getParents();
        for(Node parent : parents){
            if(visited.contains(parent.getNodeId())) continue;
            ancestorsDfs(parent, visited);
        }
    }

    public List<Node> getAncestors(int nodeId){
        Set<Integer> visited = new HashSet<>();
        ancestorsDfs(getNode(nodeId), visited);
        List<Node> ancestors = new ArrayList<>();
        for(int id : visited){
            assert(id >= 0 && id < nodes.size());
            ancestors.add(getNode(id));
        }
        return ancestors;
    }

    public List<Node> getAncestors(String nodeName){
        int nodeId = getNodeId(nodeName);
        return getAncestors(nodeId);
    }

    private void subtreeDfs(Node node, Set<Integer> visited){
        visited.add(node.getNodeId());
        List<Node> children = node.getChildren();
        for(Node child : children){
            if(visited.contains(child.getNodeId())) continue;
            subtreeDfs(child, visited);
        }
    }

    public List<Node> getSubtree(int nodeId){
        Set<Integer> visited = new HashSet<>();
        subtreeDfs(getNode(nodeId), visited);
        List<Node> subtree = new ArrayList<>();
        for(int id : visited){
            assert(id >= 0 && id < nodes.size());
            subtree.add(getNode(id));
        }
        return subtree;
    }

    public List<Node> getSubtree(String nodeName){
        int nodeId = getNodeId(nodeName);
        return getSubtree(nodeId);
    }

    public Node getRoot(){
        return getNode(Constants.GRAPH_ROOT_NAME);
    }

    // ============================ Setters and Getters ============================

    public Map<String, Integer> getNodesIdMap() {
        return nodesIdMap;
    }

    public List<Node> getNodes() {
        return nodes;
    }
}
