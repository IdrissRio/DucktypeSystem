package it.uniud.ducktypesystem.distributed.messages;

import it.uniud.ducktypesystem.distributed.data.DSQuery;

import java.io.Serializable;


public class DSTryNewQuery implements Serializable {
    private String serializedQuery;
    private int ttl;
    private DSQuery.QueryId id;

    public DSTryNewQuery(String serializedQuery, int ttl, DSQuery.QueryId id) {
        this.serializedQuery = serializedQuery;
        this.ttl = ttl;
        this.id = id;
    }

    public String getSerializedQuery() {
        return serializedQuery;
    }

    public int getTTL() {
        return ttl;
    }

    public DSQuery.QueryId getId() {
        return id;
    }
}
