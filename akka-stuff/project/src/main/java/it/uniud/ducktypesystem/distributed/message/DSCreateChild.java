package it.uniud.ducktypesystem.distributed.message;

import java.io.Serializable;

public class DSCreateChild implements Serializable {
    private Integer numChild;
    private String serializedQuery;
    private Boolean haveToForward;


    public Boolean haveToForward() {
        return haveToForward;
    }

    public void setHaveToForward(Boolean flag) {
        this.haveToForward = flag;
    }

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

    public DSCreateChild(Integer numChild, String serializedQuery) {
        this.numChild = numChild;
        this.serializedQuery = serializedQuery;
        this.haveToForward = true;
    }

}
