package it.uniud.ducktypesystem.distributed.data;

import it.uniud.ducktypesystem.errors.SystemError;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/***
 * DataFacade:
 * Allows for a single access to the static `map' representing the whole graph, read from the given file,
 * with the current robots position and the replication level of the system.
 *
 * Note: `occupied' is just an abstraction useful in debugging and graphical representation:
 * it has no impact on the algorithm (i.e., generally we don't claim to know the robots positions).
 */
public class DataFacade {
    private static DataFacade instance = null;
    public static final int MOVEFAIL = 5;
    public static final int CRITICALFAIL = 10;
    public static final int WAITINGFAIL = 5;

    private DSGraph map;
    private ArrayList<String> occupied;
    private int numRobot;
    private boolean enabledFailure;

    // Returns a singleton instance of DataFacade given the graph file path.
    public static DataFacade create(String filePath) throws SystemError {
        if (instance != null ) return instance;
        return instance=new DataFacade(filePath);
    }

    public static DataFacade getInstance() throws SystemError {
        if (instance != null) return instance;
        throw new SystemError("Invalid access to uninitialized DataFacade.");
    }

    private DataFacade(String filePath) throws SystemError {
        map = DSGraphImpl.createGraphFromFile(filePath);
        occupied= new ArrayList<>();
        enabledFailure = true;
    }

    // Overloaded setters for `occupied'.
    public void setOccupied(int numRobot) {
        this.numRobot = numRobot;
        this.occupied = new ArrayList<>(numRobot);
        int n = map.numNodes();
        for (int i = numRobot; i-- > 0; ) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, n);
            this.occupied.add(map.getNode(randomNum));
        }
    }
    public void setOccupied(ArrayList<String> occupied) {
        this.numRobot = occupied.size();
        this.occupied = occupied;
    }
    public DSGraph getMap() {
        return map;
    }
    public ArrayList<String> getOccupied() {
        return occupied;
    }

    public int getNumRobot() {
        return numRobot;
    }

    public void disableFailure() {
        this.enabledFailure = false;
    }
    public void enableFailure() {
        this.enabledFailure = true;
    }

    public boolean shouldFailMove() {
       return (ThreadLocalRandom.current().nextInt(
               0, enabledFailure ? MOVEFAIL : 0 ) == 1);
    }
    public boolean shouldFailInCriticalWork() {
        return (ThreadLocalRandom.current().nextInt(
                0, enabledFailure ? CRITICALFAIL : 0 ) == 1);
    }
    public boolean shouldDieInWaiting() {
        return (ThreadLocalRandom.current().nextInt(
                0, enabledFailure ? WAITINGFAIL : 0 ) == 1);
    }
}
