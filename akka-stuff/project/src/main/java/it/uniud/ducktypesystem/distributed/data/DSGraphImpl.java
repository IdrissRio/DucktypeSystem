package it.uniud.ducktypesystem.distributed.data;

import it.uniud.ducktypesystem.errors.SystemError;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;
import scala.Serializable;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/***
* DSGraphImpl:
* Implements the required method for the system
* providing a wrapper for the graphstream `DefaultGraph' low level implementation.
*/
public class DSGraphImpl implements DSGraph {
    private DefaultGraph impl;

    public DSGraphImpl() {
        impl = new DefaultGraph("g");
    }
    public DSGraphImpl(DSGraph graph) {
        this();
        for (String s : graph.getNodes())
            addNode(s);
        for (String s1 : graph.getNodes())
            for (String s2 : graph.adjNodes(s1))
                addEdge(s1, s2);
    }

    public static DSGraph createGraphFromFile(String filePath) throws SystemError {
        DSGraph g = new DSGraphImpl();
        g.loadGraphFromFile(filePath);
        return g;
    }
    
    @Override
    public void loadGraphFromFile(String filePath) throws SystemError {
        try {
            FileSource fs = FileSourceFactory.sourceFor(filePath);
            fs.addSink(impl);
            try {
                fs.readAll(filePath);
            } catch (Throwable t) {
                throw new SystemError(t);
            } finally {
                fs.removeSink(impl);
            }
        } catch(Throwable t) {
            throw new SystemError(t);
        }
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
        } catch (EdgeRejectedException e) {
            return false;
        }
    }

    @Override
    public boolean addEdge(int n1, int n2) {
        try {
            if (n1 >= numNodes() || n2 >= numNodes()) return false;
            impl.addEdge(getNode(n1)+getNode(n2), n1, n2);
            return true;
        } catch (EdgeRejectedException e) {
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
        ArrayList<String> tbr = new ArrayList<>();
        for (Node n : impl.getNodeSet())
            if (n.getDegree() == 0)
                tbr.add(n.getId());
        for (String s : tbr)
            removeNode(s);
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
    public DSGraph getViewFromNode(String id) {
        if (!hasNode(id)) return null;
        DSGraphImpl view = new DSGraphImpl();
        view.addNode(id);
        for (String n : adjNodes(id)) {
            view.addNode(n);
            view.addEdge(id, n);
        }
        return view;
    }
    @Override
    public DSGraph getViewFromNode(int n) {
        if (n >= numNodes()) return null;
        return getViewFromNode(getNode(n));
    }

    @Override
    public void mergeView(DSGraph newView, int memory) {
        for (String s : newView.getNodes())
            addNode(s);
        // FIXME: Dummy implementation... could be done better?
        for (String s1 : newView.getNodes())
            for (String s2 : newView.getNodes())
                if (newView.areAdj(s1, s2))
                    addEdge(s1, s2);
        // FIXME: forget old nodes.. how to choose?
    }

    @Override
    public Object getGraphImpl() {
        return impl;
    }

    @Override
    public boolean isEqual(DSGraph graph) {
        if (graph == null) return false;
        if (!getNodes().containsAll(graph.getNodes())) return false;
        if (!graph.getNodes().containsAll(getNodes())) return false;
        for (String s1 : getNodes())
            for (String s2 : adjNodes(s1))
                if (!graph.areAdj(s1, s2)) return false;
        for (String s1 : graph.getNodes())
            for (String s2 : graph.adjNodes(s1))
                if (!areAdj(s1, s2)) return false;
        return true;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Graph: numNodes: ");
        b.append(numNodes());
        b.append("\n");
        for (String s : getNodes()) {
            b.append(s + ":");
            for (String adj : adjNodes(s)) {
                b.append(" " + adj);
            }
            b.append("\n");
        }
        return b.toString();
    }

    @Override
    public String serializeToString() {
        StringBuilder b = new StringBuilder();
        for (String n : getNodes())
            b.append(n+"\t");
        b.append("\n");
        for (String n1 : getNodes())
            for (String n2 : adjNodes(n1))
                b.append(n1+" "+n2+"\t");
        return b.toString();
    }

    @Override
    public void loadFromSerializedString(String serialized) {
        String[] ne = serialized.split("\n");
        String[] nodes = ne[0].split("\t");
        String[] edges = ne[1].split("\t");
        for (String n : nodes) {
            addNode(n);
        }
        for (String e : edges) {
            String[] n1n2 = e.split(" ");
            addEdge(n1n2[0], n1n2[1]);
        }
    }
}
