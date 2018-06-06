package it.uniud.ducktypesystem.distributed.system;

import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DSGraphImpl;
import it.uniud.ducktypesystem.distributed.errors.DSSystemError;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/***
 * DSDataFacade:
 * Allows for a single access to the static `map' representing the whole graph, read from the given file,
 * with the current robots position and the replication level of the system.
 * It holds the failure probability set by the user of the simulation.
 *
 * Note: `occupied' is just an abstraction useful in debugging and graphical representation:
 * it has no impact on the algorithm (i.e., generally we don't claim to know the robots positions).
 */
public class DSDataFacade {
    private static DSDataFacade instance = null;

    private int MOVEFAIL = 1;
    private int CRITICALFAIL = 1;
    private int WAITINGFAIL = 1;

    private DSGraph map;
    private ArrayList<String> occupied;
    private int numRobot;

    /** Returns a singleton instance of DSDataFacade. */
    public static DSDataFacade getInstance() throws DSSystemError {
        if (instance != null) return instance;
        throw new DSSystemError("Invalid access to uninitialized DSDataFacade.");
    }

    // Static invocation of the ctor, given a fileName.
    public static DSDataFacade create(String filePath) throws DSSystemError {
        if (instance != null) return instance;
        return instance = new DSDataFacade(filePath);
    }

    private DSDataFacade(String filePath) throws DSSystemError {
        map = DSGraphImpl.createGraphFromFile(filePath);
        occupied= new ArrayList<>();
    }

    /** Randomly initialize the `occupied' vector.
     * Note that more than one robot can be placed on the same node. */
    public void setOccupied(int numRobot) {
        this.numRobot = numRobot;
        this.occupied = new ArrayList<>(numRobot);
        int n = map.numNodes();
        for (int i = numRobot; i-- > 0; ) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, n);
            this.occupied.add(map.getNode(randomNum));
        }
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
    public int getMOVEFAIL() {
        return MOVEFAIL;
    }
    public void setMOVEFAIL(int MOVEFAIL) {
        this.MOVEFAIL = MOVEFAIL;
    }
    public int getCRITICALFAIL() {
        return CRITICALFAIL;
    }
    public void setCRITICALFAIL(int CRITICALFAIL) {
        this.CRITICALFAIL = CRITICALFAIL;
    }
    public int getWAITINGFAIL() {
        return WAITINGFAIL;
    }
    public void setWAITINGFAIL(int WAITINGFAIL) {
        this.WAITINGFAIL = WAITINGFAIL;
    }

    public boolean shouldFailMove() {
       return (ThreadLocalRandom.current().nextInt(
               0, MOVEFAIL ) == 1);
    }
    public boolean shouldFailInCriticalWork() {
        return (ThreadLocalRandom.current().nextInt(
                0, CRITICALFAIL ) == 1);
    }
    public boolean shouldDieInWaiting() {
        return (ThreadLocalRandom.current().nextInt(
                0, WAITINGFAIL ) == 1);
    }
}
