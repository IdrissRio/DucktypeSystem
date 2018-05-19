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

    // remove nodes whose adj info are empty.
    void shrinkRedundancies();

    String chooseNext(String id);
    String chooseNext(int n);

    boolean isEmpty();
    boolean isRedundant();

    DSGraph getViewFromNode(String id);
    DSGraph getViewFromNode(int n);

    void mergeView(DSGraph newView, int memory);

    Object getGraphImpl();

    boolean isEqual(DSGraph graph);

    String serializeToString();
    void loadFromSerializedString(String serialized);
}
