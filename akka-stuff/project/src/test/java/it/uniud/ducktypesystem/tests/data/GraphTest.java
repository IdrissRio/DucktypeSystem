package it.uniud.ducktypesystem.tests.data;

import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DSGraphImpl;
import it.uniud.ducktypesystem.distributed.data.DataFacade;
import it.uniud.ducktypesystem.errors.SystemError;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GraphTest {
    private DataFacade facade;
    private DSGraph graph;
    private DefaultGraph realGraph;
    private String fileName;

    public GraphTest() throws SystemError, IOException {
        String basePath = new File("").getAbsolutePath();
        fileName = basePath + "/src/test/resources/graphTest01.DGS";
        facade = DataFacade.create(fileName);
        graph = facade.getMap();

        realGraph = new DefaultGraph("g");
        FileSource fs = FileSourceFactory.sourceFor(fileName);
        fs.addSink(realGraph);
        try {
            fs.readAll(fileName);
        } catch (Throwable t) {
            throw new SystemError(t);
        } finally {
            fs.removeSink(realGraph);
        }
    }

    @Test
    public void initFacade01() {
        facade = null;
        try {
            facade = DataFacade.create(fileName);
            facade.setOccupied(2);
            facade.setNumSearchGroups(5);

            Assert.assertEquals(facade.getOccupied().size(), 2);
            Assert.assertEquals(facade.getNumSearchGroups(), 5);
        } catch (Throwable systemError) {
            systemError.printStackTrace();
        }
    }

    @Test
    public void initFacade02() throws SystemError {
        facade = DataFacade.create(fileName);
        graph = facade.getMap();

        Assert.assertEquals(facade.getNumSearchGroups(), 3);
        Assert.assertEquals(facade.getOccupied().size(), 0);
    }

    @Test
    public void graphEquals() {
        DSGraph graph = facade.getMap();
        Assert.assertEquals(graph.numNodes(), realGraph.getNodeCount());
        for (int i = graph.numNodes(); i-- > 0; ) {
            Assert.assertEquals(graph.getNode(i), realGraph.getNode(i).getId());
            Assert.assertEquals(graph.numAdjNodes(i), realGraph.getNode(i).getDegree());
            for (int j = graph.numNodes(); j-- > 0; ) {
                Assert.assertEquals(graph.areAdj(i, j), realGraph.getNode(i).hasEdgeBetween(j));
            }
        }
    }

    @Test
    public void graph01() {
        Assert.assertEquals(graph.numNodes(), realGraph.getNodeCount());
        for (int i = graph.numNodes(); i-- > 0; ) {
            String n = graph.getNode(i);
            System.out.println(n);
            Assert.assertEquals(n, realGraph.getNode(i).getId());
        }
    }

    @Test
    public void graph02() {
        List<String> nodes = graph.getNodes();
        for (String n : nodes) {
            System.out.println(n);
        }
    }

    @Test
    public void graph03() {
        graph.addNode("D");
        realGraph.addNode("D");
        graphEquals();
    }

    @Test
    public void graph04() {
        graph.removeNode("A");
        realGraph.removeNode("A");
        graphEquals();
    }

    @Test
    public void graph05() {
        Assert.assertFalse(graph.removeNode("F"));
        Assert.assertFalse(graph.addNode("A"));
        Assert.assertFalse(graph.removeEdge("F", "G"));
        Assert.assertFalse(graph.addEdge(5, 7));
        Assert.assertFalse(graph.addEdge("F", "G"));
        graphEquals();
    }

    @Test
    public void graph06() {
        graph.addNode("D");
        graph.addNode("E");
        graph.addEdge("D", "E");
        realGraph.addNode("D");
        realGraph.addNode("E");
        realGraph.addEdge("DE", "D", "E");
        graphEquals();
    }

    @Test
    public void graph07() {
        for (String id : graph.getNodes())
            graph.removeNode(id);
        Assert.assertTrue(graph.isEmpty());

        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("A", "C");
        Assert.assertFalse(graph.addEdge("C", "A"));
        Assert.assertTrue(graph.removeEdge("C", "A"));
        Assert.assertTrue(graph.removeEdge("B", "A"));

        Assert.assertTrue(graph.isRedundant());
        graph.shrinkRedundancies();
        Assert.assertFalse(graph.isRedundant());
        Assert.assertFalse(graph.isEmpty());
    }

    @Test
    public void graph08() {
        List<String> nodes = graph.getNodes();
        List<Integer> indices = graph.getNodesIndexes();
        Assert.assertEquals(nodes.size(), indices.size());
        Assert.assertEquals(nodes.size(), graph.numNodes());

        graph.addNode("R");
        Assert.assertTrue(nodes.size() < graph.numNodes());
        Assert.assertEquals(graph.getNodesIndexes().size(), graph.numNodes());
    }

    @Test
    public void graph09() {
        for (String id : graph.getNodes())
            graph.removeNode(id);
        Assert.assertTrue(graph.isEmpty());

        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("A", "C");

        for (String s : graph.adjNodes("B"))
            System.out.println(s);

        graph.addNode("D");

        for (Integer i : graph.getNodesIndexes()) {
            if (i.equals(graph.getNodeIndex("D"))) continue;
            graph.addEdge(i, graph.getNodeIndex("D"));
        }
        System.out.println();
        for (String s : graph.adjNodes("D"))
            System.out.println(s);
    }

    @Test
    public void graph10() {
        DSGraph g = new DSGraphImpl();
        g.addNode("A");
        g.addNode("B");
        Assert.assertEquals(g.numNodes(), 2);
        g.addEdge("A", "B");
        g.addEdge("B", "A");
        Assert.assertTrue(g.areAdj("A", "B"));
        g.removeEdge("B", "A");
        Assert.assertTrue(g.isRedundant());
    }
}
