package it.uniud.ducktypesystem.distributed.data;

import it.uniud.ducktypesystem.errors.SystemError;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.Sink;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;

import java.util.ArrayList;

/***
 * DataFacade:
 * Allows for a single access to the static `map' representing the whole graph, read from the given file,
 * with the current robots position and the replication level of the system.
 *
 * Note: `occupied' is just an abstraction useful in debugging and graphical representation:
 * it has no impact on the algorithm (i.e., generally we don't claim to know the robots positions).
 */
public class DataFacade {
    private DSGraph map;
    private ArrayList<DSGraph.Node> occupied;
    private int numSearchGroups;

    // Returns a singleton instance of DataFacade given the graph file path.
    public static DataFacade create(String filePath) throws SystemError {
        return new DataFacade(filePath);
    }

    private DataFacade(String filePath) throws SystemError {
        try {
            map = new DSGraphImpl();
            FileSource fs = FileSourceFactory.sourceFor(filePath);
            fs.addSink((Sink) map.getGraph());
            try {
                fs.readAll(filePath);
            } catch (Throwable t) {
                throw new SystemError(t);
            } finally {
                fs.removeSink((Sink) map.getGraph());
            }
            occupied = new ArrayList<>();
            numSearchGroups = 3;
        } catch (Throwable t) {
            throw new SystemError(t);
        }
    }

    public void setNumSearchGroups(int numSearchGroups) {
        this.numSearchGroups = numSearchGroups;
    }

    // Overloaded setters for `occupied'.
    public void setOccupied(int numRobot) {
        this.occupied = new ArrayList<>(numRobot);
        // TODO: randomly initialize occupied vector from map.getNodes()
        for (int i = numRobot; i-- > 0; ) {
            this.occupied.add(map.getNode(i));
        }
    }
    public void setOccupied(ArrayList<DSGraph.Node> occupied) {
        this.occupied = occupied;
    }

    public DSGraph getMap() {
        return map;
    }
    public ArrayList<DSGraph.Node> getOccupied() {
        return occupied;
    }
    public int getNumSearchGroups() {
        return numSearchGroups;
    }
}
