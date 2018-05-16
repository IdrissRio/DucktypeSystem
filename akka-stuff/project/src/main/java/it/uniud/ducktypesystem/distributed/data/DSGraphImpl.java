package it.uniud.ducktypesystem.distributed.data;

import org.graphstream.graph.implementations.DefaultGraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

/***
 * DSGraphImpl:
 * Implements the required method for the system
 * providing a wrapper for the graphstream `DefaultGraph' low level implementation.
 */
public class DSGraphImpl implements DSGraph {
    private DefaultGraph graph;

    // FIXME:
    public DSGraphImpl() {
        graph = new DefaultGraph("g");
    }

    @Override
    public Node getNode(int i) {
        return new Node(graph.getNode(i).getId());
    }
    @Override
    public ArrayList<Node> getNodes() {
        ArrayList<Node> nodes = new ArrayList<>(graph.getNodeCount());
        int i = 0;
        for (org.graphstream.graph.Node n : graph.getEachNode()) {
            nodes.set(i++, new Node(n.getId()));
        }
        return nodes;
    }

    @Override
    public ArrayList<Node> adjNodes(Node node) {
        ArrayList<Node> nodes = new ArrayList<>();
        int i = 0;
        Iterator it = graph.getNode(node.label).getNeighborNodeIterator();
        while (it.hasNext()) {
            org.graphstream.graph.Node n = (org.graphstream.graph.Node) it.next();
            nodes.add(i++, new Node(n.getId()));
        }
        return nodes;
    }

    @Override
    public void addNode(Node node) {
        graph.addNode(node.label);
    }

    @Override
    public void removeNode(Node node) {
        graph.removeNode(node.label);
    }

    @Override
    public void removeEdge(Node n1, Node n2) {
        graph.removeEdge(n1.label, n2.label);
    }

    @Override
    public void shrinkRedundancies() {
        Stack<Integer> tbr = new Stack<Integer>();
        for (org.graphstream.graph.Node n : graph.getEachNode())
            if (n.getDegree() == 0)
                tbr.push(n.getIndex());
        for (Integer i : tbr)
            graph.removeNode(i);
        assert(!isRedundant());
    }

    @Override
    public Node chooseNext(Node current) {
     // TODO: consider whether to use some heuristic in finding the most informative one...
     return null;
    }

    @Override
    public boolean isEmpty() { return graph.getNodeCount() == 0; }

    @Override
    public boolean isRedundant() {
        for (org.graphstream.graph.Node n : graph.getEachNode())
            if (n.getDegree() == 0) return false;
        return true;
    }

    @Override
    public Object getGraph() {
        return graph;
    }
}
