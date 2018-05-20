package it.uniud.ducktypesystem.tests.data;

import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DSGraphImpl;
import it.uniud.ducktypesystem.distributed.data.DataFacade;
import org.junit.Assert;
import org.junit.Test;

public class MoveTest {
    private DataFacade facade;
    private DSGraph graph;

    public MoveTest() {

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
        this.graph.addEdge("A", "F");
        this.graph.addEdge("A", "G");
        this.graph.addEdge("B", "C");
        this.graph.addEdge("B", "F");
        this.graph.addEdge("C", "E");
        this.graph.addEdge("D", "E");
        this.graph.addEdge("D", "F");
        this.graph.addEdge("D", "I");
        this.graph.addEdge("E", "F");
        this.graph.addEdge("E", "G");
        this.graph.addEdge("E", "H");
        this.graph.addEdge("H", "I");

        this.facade = new DataFacade(graph);

        System.out.println("Main Graph:\n"+ graph.toString() + "\n");
    }

    @Test
    public void move01() {
        System.out.println("=== move01 ===");
        DSGraph viewA = graph.getViewFromNode("A");
        System.out.println(viewA.toString());

        Assert.assertEquals("F", viewA.obtainNewView("A",null));
        System.out.println("After first move: ");
        System.out.println(viewA.toString());

        Assert.assertEquals("E", viewA.obtainNewView("F","A"));
        System.out.println("After second move: ");
        System.out.println(viewA.toString());

        Assert.assertEquals("D", viewA.obtainNewView("E","F"));
        System.out.println("After third move: ");
        System.out.println(viewA.toString());
    }

    @Test
    public void move02() {
        System.out.println("=== move02 ===");
        DSGraph viewI = graph.getViewFromNode("I");
        System.out.println(viewI.toString());

        Assert.assertEquals("D", viewI.obtainNewView("I",null));
        System.out.println("After first move: ");
        System.out.println(viewI.toString());

        Assert.assertEquals("E", viewI.obtainNewView("D","I"));
        System.out.println("After second move: ");
        System.out.println(viewI.toString());

        Assert.assertEquals("F", viewI.obtainNewView("E","D"));
        System.out.println("After third move: ");
        System.out.println(viewI.toString());
    }
}
