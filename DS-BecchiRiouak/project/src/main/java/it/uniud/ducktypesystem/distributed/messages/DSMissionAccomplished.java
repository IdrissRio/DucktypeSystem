package it.uniud.ducktypesystem.distributed.messages;

import it.uniud.ducktypesystem.distributed.data.DSQuery;

import java.io.Serializable;

public class DSMissionAccomplished implements Serializable {
    private DSQuery.QueryId queryId;
    private String serializedQuery;
    private DSQuery.QueryStatus status;

    public DSMissionAccomplished(DSQuery.QueryId queryId, String serializedQuery, DSQuery.QueryStatus status) {
        this.queryId = queryId;
        this.status = status;
        this.serializedQuery = serializedQuery;
    }

    public DSQuery.QueryId getQueryId() {
        return this.queryId;
    }
    public String getSerializedQuery() {
        return serializedQuery;
    }
    public DSQuery.QueryStatus getStatus() {
        return status;
    }
}
