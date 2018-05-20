package it.uniud.ducktypesystem.distributed.message;

import it.uniud.ducktypesystem.distributed.data.DSQuery;

import java.io.Serializable;

public class DSMissionAccomplished implements Serializable {
    private DSQuery.QueryStatus status;
    private String version;
    private String serializedQuery;

    public DSMissionAccomplished(String version, String serializedQuery, DSQuery.QueryStatus status) {
        this.status = status;
        this.version = version;
        this.serializedQuery = serializedQuery;
    }

    public DSQuery.QueryStatus getStatus() {
        return status;
    }

    public String getVersion() {
        return this.version;
    }

    public String getSerializedQuery() {
        return serializedQuery;
    }
}
