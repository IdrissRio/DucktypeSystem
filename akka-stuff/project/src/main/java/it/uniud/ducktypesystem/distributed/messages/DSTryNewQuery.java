package it.uniud.ducktypesystem.distributed.messages;

import java.io.Serializable;


public class DSTryNewQuery implements Serializable {
    private String serializedQuery;
    private int ttl;

    public DSTryNewQuery(String serializedQuery, int ttl) {
        this.serializedQuery = serializedQuery;
        this.ttl = ttl;
    }

    public String getSerializedQuery() {
        return serializedQuery;
    }

    public int getTTL() {
        return ttl;
    }
}
