package it.uniud.ducktypesystem.distributed.data;

public class DSQueryResult {
    private DSQuery query;
    private String stillToVerify;

    public DSQueryResult(DSQuery query, String stillToVerify) {
        this.query = query;
        this.stillToVerify = stillToVerify;
    }

    public DSQuery getQuery() {
        return query;
    }

    public void setQuery(DSQuery query) {
        this.query = query;
    }

    public String getStillToVerify() {
        return stillToVerify;
    }

    public void setStillToVerify(String stillToVerify) {
        this.stillToVerify = stillToVerify;
    }
}
