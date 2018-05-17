package it.uniud.ducktypesystem.distributed.data;

import it.uniud.ducktypesystem.errors.SystemError;
import it.uniud.ducktypesystem.logger.DSAbstractLog;
import it.uniud.ducktypesystem.logger.DSLog;

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
    private DSAbstractLog logger;
    private ArrayList<String> occupied;
    private int numSearchGroups;

    // Returns a singleton instance of DataFacade given the graph file path.
    public static DataFacade create(String filePath) throws SystemError {
        return new DataFacade(filePath);
    }

    private DataFacade(String filePath) throws SystemError {
        map = DSGraphImpl.createGraphFromFile(filePath);
        occupied= new ArrayList<>();
        numSearchGroups = 3;
        logger = null;
    }

    public void setNumSearchGroups(int numSearchGroups) {
        this.numSearchGroups = numSearchGroups;
    }

    // Overloaded setters for `occupied'.
    public void setOccupied(int numRobot) {
        this.occupied = new ArrayList<>(numRobot);
        // TODO: randomly initialize occupied vector from map.getNodes()
        int n = map.numNodes();
        for (int i = numRobot; i-- > 0; ) {
            this.occupied.add(map.getNode(i%n));
        }
    }
    public void setOccupied(ArrayList<String> occupied) {
        this.occupied = occupied;
    }
    public void setLogger(DSAbstractLog log){logger=log;}
    public DSAbstractLog getLogger(){return logger;}
    public DSGraph getMap() {
        return map;
    }
    public ArrayList<String> getOccupied() {
        return occupied;
    }
    public int getNumSearchGroups() {
        return numSearchGroups;
    }
}
