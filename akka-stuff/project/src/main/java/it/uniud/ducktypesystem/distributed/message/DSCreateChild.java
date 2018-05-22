package it.uniud.ducktypesystem.distributed.message;

import java.io.Serializable;

public class DSCreateChild implements Serializable {
    private Integer numChild;
    private String serializedQuery;
    private Integer numRobot;
    private int host;
    private String version;
    private Integer nr;
    private String path;

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

    public DSCreateChild(Integer numChild, Integer numRobot, String serializedQuery, int host, String version, int nr) {
        this.numChild = numChild;
        this.serializedQuery = serializedQuery;
        this.numRobot = numRobot;
        this.version = version;
        this.nr = nr;
        this.host = host;
        this.path = "Host" + host + version + "." + nr;

    }

    public Integer getNumRobot() {
        return numRobot;
    }
    public String getVersion() {
        return version;
    }
    public Integer getNr() {
        return nr;
    }

    public String getPath() {
        return this.path;
    }

    public int getHost() {
        return this.host;
    }
}
