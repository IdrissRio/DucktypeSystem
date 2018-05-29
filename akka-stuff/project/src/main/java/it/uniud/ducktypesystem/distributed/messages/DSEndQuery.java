package it.uniud.ducktypesystem.distributed.messages;

import it.uniud.ducktypesystem.distributed.data.DSQuery;

import java.io.Serializable;

public class DSEndQuery implements Serializable {
    private DSQuery.QueryId queryId;

    public DSEndQuery(DSQuery.QueryId queryId) {
        this.queryId = queryId;
    }

    public DSQuery.QueryId getQueryId() {
        return queryId;
    }
}
