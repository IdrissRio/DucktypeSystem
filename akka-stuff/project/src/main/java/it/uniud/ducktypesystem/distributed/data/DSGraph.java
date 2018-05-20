package it.uniud.ducktypesystem.distributed.data;

import it.uniud.ducktypesystem.errors.SystemError;

import java.io.Serializable;
import java.util.List;

public interface DSGraph extends Serializable {
    void loadGraphFromFile(String filePath) throws SystemError;

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

    boolean addNode(String id);
    boolean addEdge(String id1, String id2);
    boolean addEdge(int n1, int n2);
    boolean removeNode(String id);
    boolean removeEdge(String n1, String n2);
    boolean removeEdge(int n1, int n2);

    // Remove nodes whose adj info are empty.
    void removeUnconnectedNodes();

    boolean isEmpty();
    boolean hasUnconnectedNodes();

    DSGraph getViewFromNode(String id);
    DSGraph getViewFromNode(int n);

    String obtainNewView(String whereIAm, String alreadyBeen);
    void mergeView(DSGraph newView);

    Object getGraphImpl();

    boolean isEqual(DSGraph graph);

    String serializeToString();
    void loadFromSerializedString(String serialized);
}
