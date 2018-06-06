package it.uniud.ducktypesystem.distributed.messages;

import it.uniud.ducktypesystem.distributed.data.DSQuery;

import java.io.Serializable;

public class DSEndCriticalWork implements Serializable {
    private DSQuery.QueryId queryId;

    public DSEndCriticalWork(DSQuery.QueryId queryId) {
        this.queryId = queryId;
    }

    public DSQuery.QueryId getQueryId() {
        return queryId;
    }
}
