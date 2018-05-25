package it.uniud.ducktypesystem.distributed.message;

import java.io.Serializable;

public class DSRobotFailureOccurred implements Serializable {
    private String deadNode;
    public DSRobotFailureOccurred(String deadNode) {
        this.deadNode = deadNode;
    }

    public String getDeadNode() {
        return deadNode;
    }
}
