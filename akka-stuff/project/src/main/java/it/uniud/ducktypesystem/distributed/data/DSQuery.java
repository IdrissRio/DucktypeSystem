package it.uniud.ducktypesystem.distributed.data;

import it.uniud.ducktypesystem.errors.SystemError;

public class DSQuery extends DSGraphImpl {
    private String version;

    public enum QueryStatus {
        MATCH,
        FAIL,
        NEW,
        DONTKNOW
    }

    public static DSQuery createQueryFromFile(String filePath) throws SystemError {
        DSQuery q = new DSQuery();
        q.loadGraphFromFile(filePath);
        return q;
    }

    public DSQuery clone() {
        DSQuery q = (DSQuery) super.clone();
        q.version = this.version;
        return q;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    public String getVersion() {
        return version;
    }

    public QueryStatus checkAndReduce(DSGraph myView, String myNode) {
        // assert(!isRedundant());
        boolean newHypothesis = false;

        for (String qN : getNodes()) {
            if (!myView.hasNode(qN)) continue;
            // If the knowledge about `qN' is complete and more edges are required, then query fails.
            if (qN.equals(myNode) && !myView.adjNodes(qN).containsAll(adjNodes(qN)))
                return QueryStatus.FAIL;
            // Remove verified edges
            for (String qN2 : adjNodes(qN)) {
                if (!myView.hasNode(qN2)) continue;
                removeEdge(qN, qN2);
                newHypothesis = true;
            }
        }
        shrinkRedundancies();

        // `myView' verified it all.
        if (isEmpty())
            return QueryStatus.MATCH;

        // `myView' knowledge simplified the query.
        if (newHypothesis)
            return QueryStatus.NEW;

        // `myView' had nothing to say about it.
        return QueryStatus.DONTKNOW;
    }
}
