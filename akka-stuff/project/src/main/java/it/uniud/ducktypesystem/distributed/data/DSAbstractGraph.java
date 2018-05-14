package it.uniud.ducktypesystem.distributed.data;

import java.util.List;

public interface DSAbstractGraph {
    // FIXME:
    public class Node {
        public String label;
        // index in current subgraph structure
        public int index;
    }
    public class Nodes {
        // FIXME:
        public List<Node> nodes;
    }

    public Nodes getNodes();
    public Nodes adjNodes(Node node);
    public void addNode(Node node);
    public void removeNode(Node node);
    public void removeEdge(Node n1, Node n2); // Don't remove trailing nodes!

    // remove nodes whose adj info are empty.
    public void shrinkRedundancies();

    Node chooseNext(Node current);

    public boolean isEmpty();
    public boolean isRedundant();
}
