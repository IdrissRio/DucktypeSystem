package it.uniud.ducktypesystem.tests.data;

import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DataFacade;
import it.uniud.ducktypesystem.errors.SystemError;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GraphTest1 {
    private DataFacade facade;
    private DefaultGraph realGraph;
    private String fileName;

    public GraphTest1() throws SystemError, IOException {
        String basePath = new File("").getAbsolutePath();
        fileName = basePath + "/src/test/resources/graphTest01.DGS";
        facade = DataFacade.create(fileName);

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

        Assert.assertEquals(facade.getNumSearchGroups(), 3);
        Assert.assertEquals(facade.getOccupied().size(), 0);
    }

    @Test
    public void graphEquals() {
        DSGraph graph = facade.getMap();
        Assert.assertEquals(graph.numNodes(), realGraph.getNodeCount());
        for (int i = graph.numNodes(); i-- > 0; ) {
            Assert.assertEquals(graph.getNode(i).getLabel(), realGraph.getNode(i).getId());
            Assert.assertEquals(graph.numAdjNodes(graph.getNode(i)), realGraph.getNode(i).getDegree());
            for (int j = graph.numNodes(); j-- > 0; ) {
                Assert.assertEquals(graph.areAdj(graph.getNode(i), graph.getNode(j)),
                        realGraph.getNode(i).hasEdgeBetween(j));
            }
        }
    }

    @Test
    public void graph01() {
        DSGraph graph = facade.getMap();
        DSGraph.Node n;
        Assert.assertEquals(graph.numNodes(), realGraph.getNodeCount());
        for (int i = graph.numNodes(); i-- > 0; ) {
            n = graph.getNode(i);
            System.out.println(n.getLabel());
            Assert.assertEquals(n.getLabel(), realGraph.getNode(i).getId());
        }
    }

    @Test
    public void graph02() {
        DSGraph graph = facade.getMap();
        ArrayList<DSGraph.Node> nodes = graph.getNodes();
        for (DSGraph.Node n : nodes) {
            System.out.println(n.getLabel());
            Assert.assertNotNull(realGraph.getNode(n.getLabel()));
        }
    }


    @Test
    public void graph03() {
        DSGraph graph = facade.getMap();
        ArrayList<DSGraph.Node> nodes = graph.getNodes();
        graphEquals();
    }
}
