package it.uniud.ducktypesystem.distributed.message;

import it.uniud.ducktypesystem.distributed.data.DSQuery;

import java.io.Serializable;

public class DSCreateQueryChecker implements Serializable {
    private DSQuery.QueryId queryId;

    public DSCreateQueryChecker(DSQuery.QueryId queryId) {
        this.queryId = queryId;
    }

    public DSQuery.QueryId getQueryId() {
        return queryId;
    }
}
