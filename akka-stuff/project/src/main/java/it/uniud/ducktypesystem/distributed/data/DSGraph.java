package it.uniud.ducktypesystem.distributed.data;

public class DSGraph implements DSAbstractGraph{
    // Load from general_graph.
    public DSGraph() { /* TODO: */ }
    public DSGraph(Node node) { /* TODO: */ }

    @Override
    public Nodes getNodes() { /* TODO: */ return null; }

    @Override
    public Nodes adjNodes(Node node) { /* TODO: */ return null; }

    @Override
    public void addNode(Node node) { /* TODO: */ }

    @Override
    public void removeNode(Node node) { /* TODO: */ }

    @Override
    public void removeEdge(Node n1, Node n2) { /* TODO: */ }

    @Override
    public void shrinkRedundancies() { /* TODO: */ }

    @Override
    public Node chooseNext(Node current) { /* TODO: */ return null; }

    @Override
    public boolean isEmpty() { /* TODO: */ return false; }

    @Override
    public boolean isRedundant() { /* TODO: */ return false; }
}