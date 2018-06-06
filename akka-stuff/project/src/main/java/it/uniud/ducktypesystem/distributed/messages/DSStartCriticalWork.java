package it.uniud.ducktypesystem.distributed.messages;

import it.uniud.ducktypesystem.distributed.data.DSQuery;

import java.io.Serializable;

public class DSStartCriticalWork implements Serializable {
    private DSQuery.QueryId queryId;

    public DSStartCriticalWork(DSQuery.QueryId queryId) {
        this.queryId = queryId;
    }

    public DSQuery.QueryId getQueryId() {
        return queryId;
    }
}
