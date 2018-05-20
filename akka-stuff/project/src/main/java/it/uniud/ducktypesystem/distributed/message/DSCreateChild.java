package it.uniud.ducktypesystem.distributed.message;

import java.io.Serializable;

public class DSCreateChild implements Serializable {
    private Integer numChild;
    private String serializedQuery;
    private Integer numRobot;
    private String version;

    public Integer getNumChild() {
        return numChild;
    }

    public void setNumChild(Integer numChild) {
        this.numChild = numChild;
    }

    public String getSerializedQuery() {
        return serializedQuery;
    }

    public void setSeralizedQuery(String serializedQuery) {
        this.serializedQuery = serializedQuery;
    }

    public DSCreateChild(Integer numChild, Integer numRobot, String serializedQuery, String version) {
        this.numChild = numChild;
        this.serializedQuery = serializedQuery;
        this.numRobot = numRobot;
        this.version = version;
    }

    public Integer getNumRobot() {
        return numRobot;
    }

    public void setNumRobot(Integer numRobot) {
        this.numRobot = numRobot;
    }

    public String getVersion() {
        return version;
    }
}
