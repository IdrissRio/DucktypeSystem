package it.uniud.ducktypesystem.distributed.data;

import java.util.ArrayList;

public interface DSGraph {
    class Node {
        String label;
        Node(String label) {
            this.label = label;
        }
        public String getLabel() {
            return label;
        }
    }

    int numNodes();
    Node getNode(int i);
    ArrayList<Node> getNodes();
    int numAdjNodes(Node n);
    ArrayList<Node> adjNodes(Node node);
    boolean areAdj(Node n1, Node n2);
    void addNode(Node node);
    void removeNode(Node node);
    void removeEdge(Node n1, Node n2); // Don't remove trailing nodes!

    // remove nodes whose adj info are empty.
    void shrinkRedundancies();

    Node chooseNext(Node current);

    boolean isEmpty();
    boolean isRedundant();

    Object getGraph();
}
