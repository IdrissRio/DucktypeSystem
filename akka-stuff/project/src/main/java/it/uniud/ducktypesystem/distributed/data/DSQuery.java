package it.uniud.ducktypesystem.distributed.data;

import java.io.Serializable;

public interface DSQuery extends DSGraph {

    class QueryId implements Serializable {
        private String path;
        private int host;
        private String name;
        private int attemptNr;

        public QueryId(String path) {
            this.path = path;
            String[] decomposed = path.split("-");
            this.host = Integer.parseInt(decomposed[0]);
            this.name = decomposed[1];
            this.attemptNr = Integer.parseInt(decomposed[2]);
        }
        QueryId(int host, String name) {
            this.host = host;
            this.name = name;
            this.attemptNr = 0;
            this.path = "" + host + "-" + name + "-" + attemptNr;
        }
        QueryId(QueryId id) {
            this.path = id.getPath();
            this.name = id.getName();
            this.host = id.getHost();
            this.attemptNr = id.getAttemptNr();
        }
        public String getPath() {
            return "" + host + "-" + name + "-" + attemptNr;
        }
        public int getHost() {
            return host;
        }
        public String getName() {
            return name;
        }
        public int getAttemptNr() {
            return attemptNr;
        }
        public String getVersion() {
            return "" + host + "-" + name;
        }

        public void setPath(String path) {
            this.path = path;
            String[] decomposed = path.split("-");
            this.host = Integer.parseInt(decomposed[0]);
            this.name = decomposed[1];
            this.attemptNr = Integer.parseInt(decomposed[2]);
        }
        public void setHost(int host) {
            this.host = host;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void incrementAttemptNr() {
            ++this.attemptNr;
        }
    }

    enum QueryStatus {
        MATCH,
        FAIL,
        NEW,
        DONTKNOW
    }

    QueryId getId();
    String getName();
    int getHost();
    int getAttemptNr();
    String getVersion();
    void setId(QueryId id);
    void setName(String version);
    void setHost(int host);
    void incrementAttemptNr();

    QueryStatus checkAndReduce(DSGraph myView, String myNode);
}
