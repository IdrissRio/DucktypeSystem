package it.uniud.ducktypesystem.distributed.data;

import it.uniud.ducktypesystem.errors.SystemError;

public interface DSQuery extends DSGraph {
    enum QueryStatus {
        MATCH,
        FAIL,
        NEW,
        DONTKNOW
    }

    void setVersion(String version);
    String getVersion();

    QueryStatus checkAndReduce(DSGraph myView, String myNode);
}
