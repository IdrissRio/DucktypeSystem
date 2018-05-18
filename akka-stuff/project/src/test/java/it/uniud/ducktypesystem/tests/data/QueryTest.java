package it.uniud.ducktypesystem.tests.data;

import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DSGraphImpl;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import org.junit.Assert;
import org.junit.Test;

public class QueryTest {
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
        this.graph.addEdge("D", "F");
        this.graph.addEdge("F", "G");
        this.graph.addEdge("E", "F");
        this.graph.addEdge("E", "H");
        this.graph.addEdge("F", "H");
        this.graph.addEdge("G", "I");
        this.graph.addEdge("I", "H");

        System.out.println("Main Graph:\n"+ graph.toString() + "\n");
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
        System.out.println("=== query02 ===");
        DSGraph view = graph.getViewFromNode("D");
        System.out.println("View from D:\n" + view.toString());

        DSGraph expected = new DSGraphImpl();
        expected.addNode("A");
        expected.addNode("B");
        expected.addNode("C");
        expected.addNode("D");
        expected.addNode("F");
        expected.addEdge("D", "A");
        expected.addEdge("D", "B");
        expected.addEdge("D", "C");
        expected.addEdge("D", "F");
        Assert.assertTrue(expected.isEqual(view));

        DSQuery q1 = new DSQuery();
        q1.addNode("A");
        q1.addNode("D");
        q1.addNode("C");
        q1.addEdge("A", "D");
        q1.addEdge("D", "C");
        Assert.assertEquals(q1.checkAndReduce(view, "D"), DSQuery.QueryStatus.MATCH);
        Assert.assertTrue(q1.isEmpty());

        DSQuery q2 = new DSQuery();
        q2.addNode("A");
        q2.addNode("D");
        q2.addNode("E");
        q2.addEdge("A", "D");
        q2.addEdge("D", "E");
        Assert.assertEquals(q2.checkAndReduce(view, "D"), DSQuery.QueryStatus.FAIL);


        DSQuery q3 = new DSQuery();
        Assert.assertEquals(q3.checkAndReduce(view, "D"), DSQuery.QueryStatus.MATCH);
        Assert.assertTrue(q3.isEmpty());


        DSQuery q4 = new DSQuery();
        q4.addNode("A");
        q4.addNode("D");
        q4.addNode("B");
        q4.addEdge("A", "D");
        q4.addEdge("D", "B");
        q4.addEdge("A", "B");
        Assert.assertEquals(q4.checkAndReduce(view, "D"), DSQuery.QueryStatus.NEW);

        DSQuery q5 = new DSQuery();
        q5.addNode("A");
        q5.addNode("B");
        q5.addNode("C");
        q5.addEdge("A", "B");
        q5.addEdge("B", "C");
        Assert.assertEquals(q5.checkAndReduce(view, "D"), DSQuery.QueryStatus.DONTKNOW);
    }

    @Test
    public void query03() throws CloneNotSupportedException {
        System.out.println("=== query03 ===");
        DSGraph view = graph.getViewFromNode("F");
        System.out.println("View from F:\n" + view.toString());

        DSGraph expected = new DSGraphImpl();
        expected.addNode("F");
        expected.addNode("D");
        expected.addNode("G");
        expected.addNode("E");
        expected.addNode("H");
        expected.addEdge("F", "E");
        expected.addEdge("F", "D");
        expected.addEdge("F", "G");
        expected.addEdge("F", "H");
        Assert.assertTrue(expected.isEqual(view));

        DSQuery q1 = new DSQuery();
        q1.addNode("A");
        q1.addNode("D");
        q1.addNode("C");
        q1.addEdge("A", "D");
        q1.addEdge("D", "C");
        Assert.assertEquals(q1.checkAndReduce(view, "F"), DSQuery.QueryStatus.DONTKNOW);

        DSQuery q2 = new DSQuery();
        q2.addNode("F");
        q2.addNode("D");
        q2.addNode("E");
        q2.addNode("G");
        q2.addNode("H");
        q2.addEdge("F", "D");
        q2.addEdge("F", "E");
        q2.addEdge("F", "H");
        q2.addEdge("D", "E");
        q2.addEdge("D", "G");
        Assert.assertEquals(q2.checkAndReduce(view, "F"), DSQuery.QueryStatus.NEW);
        DSQuery expectedNew = new DSQuery();
        expectedNew.addNode("D");
        expectedNew.addNode("E");
        expectedNew.addNode("G");
        expectedNew.addEdge("D", "E");
        expectedNew.addEdge("D", "G");
        Assert.assertTrue(q2.isEqual(expectedNew));


        DSQuery q3 = new DSQuery();
        Assert.assertEquals(q3.checkAndReduce(new DSGraphImpl(), "F"), DSQuery.QueryStatus.MATCH);
        Assert.assertTrue(q3.isEmpty());


        DSQuery q4 = new DSQuery();
        q4.addNode("A");
        q4.addNode("D");
        q4.addNode("F");
        q4.addNode("H");
        q4.addNode("G");
        q4.addEdge("A", "D");
        q4.addEdge("D", "F");
        q4.addEdge("F", "H");
        q4.addEdge("F", "G");
        DSQuery q5 = new DSQuery(q4);

        Assert.assertEquals(q4.checkAndReduce(graph.getViewFromNode("D"), "D"), DSQuery.QueryStatus.NEW);
        Assert.assertEquals(q4.checkAndReduce(graph.getViewFromNode("F"), "F"), DSQuery.QueryStatus.MATCH);

        Assert.assertEquals(q5.checkAndReduce(graph.getViewFromNode("F"), "F"), DSQuery.QueryStatus.NEW);
        Assert.assertEquals(q5.checkAndReduce(graph.getViewFromNode("D"), "D"), DSQuery.QueryStatus.MATCH);
    }

    @Test
    public void query04() throws CloneNotSupportedException {
        System.out.println("=== query04 ===");

        DSQuery q1 = new DSQuery();
        q1.addNode("A");
        q1.addNode("B");
        q1.addNode("D");
        q1.addNode("F");
        q1.addNode("H");
        q1.addEdge("A", "B");
        q1.addEdge("B", "D");
        q1.addEdge("D", "F");
        q1.addEdge("F", "H");

        DSQuery.QueryStatus status = DSQuery.QueryStatus.DONTKNOW;

        for (String n : graph.getNodes()) {
            status = q1.checkAndReduce(graph.getViewFromNode(n), n);
            if (status == DSQuery.QueryStatus.FAIL
                    || status == DSQuery.QueryStatus.MATCH) break;
        }

        Assert.assertEquals(status, DSQuery.QueryStatus.MATCH);
    }
}
