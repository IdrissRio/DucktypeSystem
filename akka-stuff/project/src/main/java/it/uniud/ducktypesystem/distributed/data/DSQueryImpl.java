package it.uniud.ducktypesystem.distributed.data;

import it.uniud.ducktypesystem.errors.SystemError;

public class DSQueryImpl extends DSGraphImpl implements DSQuery {
    private String version;
    private int nr;

    public DSQueryImpl() {
        super();
        version = null;
        nr = 0;
    }
    public DSQueryImpl(DSGraph g) {
        super(g);
        version = null;
        nr = 0;
    }
    public DSQueryImpl(DSQuery q) {
        super(q);
        version = q.getVersion();
        nr = q.getVersionNr();
    }

    public static DSQuery createQueryFromFile(String filePath) throws SystemError {
        DSQuery q = new DSQueryImpl(DSGraphImpl.createGraphFromFile(filePath));
        filePath = filePath.replaceFirst("(.*)/","");
        filePath = filePath.replaceFirst("[.][^.]+$", "");
        q.setVersion(filePath);
        return q;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    public String getVersion() {
        return version;
    }
    public void incrementVersionNr() { this.nr++; }
    public int getVersionNr() {return this.nr; }

    public DSQuery.QueryStatus checkAndReduce(DSGraph myView, String myNode) {
        assert(!hasUnconnectedNodes());
        boolean newHypothesis = false;

        for (String qN : getNodes()) {
            if (!myView.hasNode(qN)) continue;
            // If the knowledge about `qN' is complete and more edges are required, then query fails.
            if (qN.equals(myNode) && !myView.adjNodes(qN).containsAll(adjNodes(qN)))
                return QueryStatus.FAIL;
            // Remove verified edges
            for (String qN2 : adjNodes(qN)) {
                if (!myView.hasNode(qN2) || !myView.areAdj(qN, qN2)) continue;
                removeEdge(qN, qN2);
                newHypothesis = true;
            }
        }
        removeUnconnectedNodes();

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
