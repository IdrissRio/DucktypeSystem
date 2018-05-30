package it.uniud.ducktypesystem.distributed.data;

public class DSQueryResult {
    private DSQuery query;
    private String stillToVerify;
    private DSQuery.QueryStatus status;

    public DSQueryResult(DSQuery query, String stillToVerify) {
        this.query = query;
        this.stillToVerify = stillToVerify;
        this.status = DSQuery.QueryStatus.NEW;
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

    public void setStatus(DSQuery.QueryStatus status) {
        this.status = status;
    }

    public DSQuery.QueryStatus getStatus() {return status;}
}
