package it.uniud.ducktypesystem.distributed.message;

import it.uniud.ducktypesystem.distributed.data.DSQuery;

import java.io.Serializable;

public class DSRetryQuery implements Serializable {
    private DSQuery.QueryId path;

    public DSRetryQuery(DSQuery.QueryId path) {
        this.path = path;
    }

    public DSQuery.QueryId getQueryId() {
        return path;
    }
}
