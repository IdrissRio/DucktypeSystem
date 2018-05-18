package it.uniud.ducktypesystem.distributed.message;

import akka.actor.Props;
import it.uniud.ducktypesystem.distributed.data.DSQuery;

import java.io.Serializable;


public class DSCreateChild implements Serializable {
    private Integer numChild;
    private DSQuery query;
    private Boolean flag;


    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public Integer getNumChild() {
        return numChild;
    }

    public void setNumChild(Integer numChild) {
        this.numChild = numChild;
    }

    public DSQuery getQuery() {
        return query;
    }

    public void setQuery(DSQuery query) {
        this.query = query;
    }

    public DSCreateChild(Integer numChild, DSQuery query) {
        this.numChild = numChild;
        this.query = query;
        this.flag=false;
    }

}
