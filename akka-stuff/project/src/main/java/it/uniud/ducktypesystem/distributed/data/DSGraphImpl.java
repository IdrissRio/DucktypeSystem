package it.uniud.ducktypesystem.distributed.data;

import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DSGraphImpl implements DSGraph {
    private DefaultGraph impl;

    public DSGraphImpl() {
        impl = new DefaultGraph("g");
    }
    public DSGraphImpl(DefaultGraph impl) {
        this.impl = impl;
    }

    @Override
    public int numNodes() {
        return impl.getNodeCount();
    }

    @Override
    public String getNode(int i) {
        return impl.getNode(i).getId();
    }

    @Override
    public int getNodeIndex(String id) {
        return impl.getNode(id).getIndex();
    }

    @Override
    public boolean hasNode(String id) {
        return (impl.getNode(id) != null);
    }

    @Override
    public List<String> getNodes() {
        List<String> nodes = new ArrayList<>(numNodes());
        for (Node n : impl.getNodeSet()) {
            nodes.add(n.getId());
        }
        return nodes;
    }

    @Override
    public List<Integer> getNodesIndexes() {
        List<Integer> nodes = new ArrayList<>(numNodes());
        for (Node n : impl.getNodeSet()) {
            nodes.add(n.getIndex());
        }
        return nodes;
    }

    @Override
    public int numAdjNodes(String id) {
        return impl.getNode(id).getDegree();
    }
    @Override
    public int numAdjNodes(int n) {
        return impl.getNode(n).getDegree();
    }

    @Override
    public List<String> adjNodes(String node) {
        Node n = impl.getNode(node);
        List<String> nodes = new ArrayList<>(n.getDegree());
        Iterator it = n.getNeighborNodeIterator();
        while (it.hasNext()) {
            Node m = (Node) it.next();
            nodes.add(m.getId());
        }
        return nodes;
    }

    @Override
    public List<String> adjNodes(int node) {
        return adjNodes(impl.getNode(node).getId());
    }

    @Override
    public List<Integer> adjNodesIndexes(String node) {
        Node n = impl.getNode(node);
        List<Integer> nodes = new ArrayList<>(n.getDegree());
        Iterator it = n.getNeighborNodeIterator();
        while (it.hasNext()) {
            Node m = (Node) it.next();
            nodes.add(m.getIndex());
        }
        return nodes;
    }

    @Override
    public List<Integer> adjNodesIndexes(int node) {
        return adjNodesIndexes(impl.getNode(node).getId());
    }

    @Override
    public boolean areAdj(String n1, String n2) {
        return impl.getNode(n1).hasEdgeBetween(n2);
    }

    @Override
    public boolean areAdj(int n1, int n2) {
        return impl.getNode(n1).hasEdgeBetween(n2);
    }

    @Override
    public boolean addNode(String id) {
        try {
            impl.addNode(id);
            return true;
        } catch (IdAlreadyInUseException e) {
            return false;
        }
    }

    @Override
    public boolean addEdge(String n1, String n2) {
        try {
            if (!hasNode(n1) || !hasNode(n2)) return false;
            impl.addEdge(n1+n2, n1, n2);
            return true;
        } catch (IdAlreadyInUseException e) {
            return false;
        }
    }

    @Override
    public boolean addEdge(int n1, int n2) {
        try {
            if (n1 >= numNodes() || n2 >= numNodes()) return false;
            impl.addEdge(getNode(n1)+getNode(n2), n1, n2);
            return true;
        } catch (IdAlreadyInUseException e) {
            return false;
        }
    }

    @Override
    public boolean removeNode(String id) {
        if (!hasNode(id)) return false;
        impl.removeNode(id);
        return true;
    }

    @Override
    public boolean removeEdge(String n1, String n2) {
        if (!hasNode(n1) || !hasNode(n2)) return false;
        if (!areAdj(n1, n2)) return false;
        impl.removeEdge(n1, n2);
        return true;
    }

    @Override
    public boolean removeEdge(int n1, int n2) {
        if (n1 >= numNodes() || n2 >= numNodes()) return false;
        return removeEdge(getNode(n1), getNode(n2));
    }

    @Override
    public void shrinkRedundancies() {
        for (Node n : impl.getNodeSet())
            if (n.getDegree() == 0)
                removeNode(n.getId());
    }

    @Override
    public String chooseNext(int current) {
        // FIXME: choose random or following some heuristics from getNodes.
        return current == 0 ? getNode(1) : getNode(0);
    }
    @Override
    public String chooseNext(String node) {
        return chooseNext(getNodeIndex(node));
    }

    @Override
    public boolean isEmpty() {
        return numNodes() == 0;
    }

    @Override
    public boolean isRedundant() {
        for (Node n : impl.getNodeSet())
            if (n.getDegree() == 0)
                return true;
        return false;
    }

    @Override
    public Object getGraphImpl() {
        return impl;
    }
}
