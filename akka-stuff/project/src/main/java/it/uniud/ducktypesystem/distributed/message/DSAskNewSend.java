package it.uniud.ducktypesystem.distributed.message;

import java.io.Serializable;

public class DSAskNewSend implements Serializable {
    private String deadPath;

    public DSAskNewSend(String deadPath) {
        this.deadPath = deadPath;
    }

    public String getDeadPath() {
        return this.deadPath;
    }
}
