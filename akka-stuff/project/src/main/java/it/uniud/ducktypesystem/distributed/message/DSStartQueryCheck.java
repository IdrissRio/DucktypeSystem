package it.uniud.ducktypesystem.distributed.message;

import it.uniud.ducktypesystem.distributed.data.DSQuery;

import java.io.Serializable;

public class DSStartQueryCheck implements Serializable {
    private String serializedQuery;
    private DSQuery.QueryId queryId;
    private int ttl;

    public DSStartQueryCheck(String serializedQuery, DSQuery.QueryId queryId, int ttl) {
        this.serializedQuery = serializedQuery;
        this.queryId = queryId;
        this.ttl = ttl;
    }

    public String getSerializedQuery() {
        return serializedQuery;
    }

    public DSQuery.QueryId getQueryId() {
        return queryId;
    }

    public int getTTL() {
        return ttl;
    }
}
