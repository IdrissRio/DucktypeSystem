package it.uniud.ducktypesystem.distributed.data;

import it.uniud.ducktypesystem.distributed.errors.DSSystemError;

import java.util.List;

public interface DSGraph {
    void loadGraphFromFile(String filePath) throws DSSystemError;

    /** Read only methods. */
    int numNodes();
    String getNode(int i);
    int getNodeIndex(String id);
    boolean hasNode(String id);
    List<String> getNodes();
    List<Integer> getNodesIndexes();
    int numAdjNodes(String id);
    int numAdjNodes(int n);
    List<String> adjNodes(String id);
    List<String> adjNodes(int n);
    List<Integer> adjNodesIndexes(String id);
    List<Integer> adjNodesIndexes(int n);
    boolean areAdj(String id1, String id2);
    boolean areAdj(int n1, int n2);
    boolean isEmpty();
    Object getGraphImpl();
    boolean isEqual(DSGraph graph);
    boolean hasUnconnectedNodes();

    /** Modifying graph methods. */
    boolean addNode(String id);
    boolean addEdge(String id1, String id2);
    boolean addEdge(int n1, int n2);
    boolean removeNode(String id);
    boolean removeEdge(String n1, String n2);
    boolean removeEdge(int n1, int n2);
    void clear();
    void removeUnconnectedNodes();

    /** Methods simulating the physical sensors use. */
    // These simulate the first acquiring of knowledge from the main graph.
    DSGraph getViewFromNode(String id);
    DSGraph getViewFromNode(int n);
    // These simulate the learning during move, possibly forgetting old nodes.
    void obtainView(String whereIAm);
    String obtainNewView(String whereIAm, String alreadyBeen);

    /** The following methods are required because the chosen low level implementation
     * may not implement the Serializable interface. */
    String serializeToString();
    void loadFromSerializedString(String serialized);
}
