package it.uniud.ducktypesystem.distributed.message;

import it.uniud.ducktypesystem.distributed.data.DSQuery;

import java.io.Serializable;

public class DSMissionAccomplished implements Serializable {
    private DSQuery.QueryStatus status;
    public DSMissionAccomplished(DSQuery.QueryStatus status) {
        this.status = status;
    }

    public DSQuery.QueryStatus getStatus() {
        return status;
    }
}
