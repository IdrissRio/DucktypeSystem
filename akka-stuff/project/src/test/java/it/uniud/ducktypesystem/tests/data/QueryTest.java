package it.uniud.ducktypesystem.tests.data;

import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DSGraphImpl;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import org.junit.Assert;
import org.junit.Test;

public class QueryTest {
    private DSQuery query;
    private DSGraph graph;

    public QueryTest() {
        this.graph = new DSGraphImpl();
        this.graph.addNode("A");
        this.graph.addNode("B");
        this.graph.addNode("C");
        this.graph.addNode("D");
        this.graph.addNode("E");
        this.graph.addNode("F");
        this.graph.addNode("G");
        this.graph.addNode("H");
        this.graph.addNode("I");

        this.graph.addEdge("A", "B");
        this.graph.addEdge("A", "D");
        this.graph.addEdge("B", "D");
        this.graph.addEdge("C", "D");
        this.graph.addEdge("C", "G");
        this.graph.addEdge("F", "G");
        this.graph.addEdge("E", "F");
        this.graph.addEdge("E", "H");
        this.graph.addEdge("F", "H");
        this.graph.addEdge("G", "I");
        this.graph.addEdge("I", "H");
    }

    @Test
    public void query01() {
        System.out.println("=== query01 ===");
        DSQuery q = new DSQuery();
        System.out.println(q.toString());
        Assert.assertTrue(q.isEmpty());
        q.addNode("A");
        q.addNode("A");
        q.addNode("B");
        q.addNode("C");
        q.addEdge(q.getNodeIndex("A"), q.getNodeIndex("B"));
        System.out.println("After add edge:\n" + q.toString());
        q.removeEdge("B", "A");
        System.out.println("After remove edge:\n" + q.toString());
        Assert.assertTrue(q.isRedundant());
        q.shrinkRedundancies();
        System.out.println("After shrink:\n" + q.toString());
        Assert.assertTrue(q.isEmpty());
    }

    @Test
    public void query02() {

    }
}
