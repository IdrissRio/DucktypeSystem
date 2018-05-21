package it.uniud.ducktypesystem.distributed.data;

public interface DSQuery extends DSGraph {
    enum QueryStatus {
        MATCH,
        FAIL,
        NEW,
        DONTKNOW
    }

    void setVersion(String version);
    String getVersion();

    void incrementVersionNr();
    int getVersionNr();

    QueryStatus checkAndReduce(DSGraph myView, String myNode);
}
