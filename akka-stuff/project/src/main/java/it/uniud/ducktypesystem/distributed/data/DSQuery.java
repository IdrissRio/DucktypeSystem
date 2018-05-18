package it.uniud.ducktypesystem.distributed.data;

public class DSQuery extends DSGraphImpl {
    private String version;

    public DSQuery clone() {
        DSQuery q = (DSQuery) super.clone();
        q.version = this.version;
        return null;
    }

    public String getVersion() {
        return version;
    }
}
