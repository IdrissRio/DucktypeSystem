package it.uniud.ducktypesystem.distributed.data;

import it.uniud.ducktypesystem.distributed.errors.DSSystemError;

public class DSQueryImpl extends DSGraphImpl implements DSQuery {
    private QueryId id;

    public DSQueryImpl() {
        super();
        id = new QueryId(0, "unknownname");
    }
    public DSQueryImpl(DSGraph g) {
        super(g);
        id = new QueryId(0, "unknownname");
    }
    public DSQueryImpl(DSGraph g, Integer host) {
        super(g);
        id = new QueryId(host, "unknownname");
    }
    public DSQueryImpl(DSQuery q) {
        super(q);
        id = new QueryId(q.getId());
    }

    public static DSQuery createQueryFromFile(String filePath, Integer host) throws DSSystemError {
        DSQuery q = new DSQueryImpl(DSGraphImpl.createGraphFromFile(filePath),host);
        // Get version name from fileName without extensions and previous path.
        filePath = filePath.replaceFirst("(.*)/","");
        filePath = filePath.replaceFirst("[.][^.]+$", "");
        q.setName(filePath);
        return q;
    }

    public QueryId getId() {
        return id;
    }
    public String getName() {
        return id.getName();
    }
    public int getHost() {
        return id.getHost();
    }

    public int getAttemptNr() {
        return 0;
    }
    public String getVersion() {
        return id.getVersion();
    }
    public void setId(QueryId id) {
        this.id = new QueryId(id);
    }
    public void setName(String name) {
        id.setName(name);
    }
    public void setHost(int host) {
        id.setHost(host);
    }
    public void incrementAttemptNr() {
        id.incrementAttemptNr();
    }

    /**
     * This is the main algorithm used by the QueryCheckers.
     * Precondition:
     *   The full view from `myNode' inside the (unknown) main graph, is a subgraph of `myView'.
     * Results:
     *  - MATCH    : if `this' is a subgraph of `myView'; `this' results to be empty.
     *  - FAIL     : if `this' has an edge from `myNode' which is not present in `myView'.
     *  - DONTKNOW : in any other case; `this' results not to be empty.
     */
    public DSQuery.QueryStatus checkAndReduce(DSGraph myView, String myNode) {

        for (String qN : getNodes()) {
            // If nothing can be said about `qN', skip it.
            if (!myView.hasNode(qN)) continue;

            // If the knowledge about `qN' is complete (i.e., the robot is on `qN')
            // but more edges are required by the query, then it FAILS.
            if (qN.equals(myNode) && !myView.adjNodes(qN).containsAll(adjNodes(qN)))
                return QueryStatus.FAIL;

            // Remove verified edges
            for (String qN2 : adjNodes(qN)) {
                if (!myView.hasNode(qN2) || !myView.areAdj(qN, qN2)) continue;
                removeEdge(qN, qN2);
            }
        } // end loop on query nodes.

        // Remove nodes whose edges have been entirely verified.
        removeUnconnectedNodes();

        // `myView' verified it all.
        if (isEmpty())
            return QueryStatus.MATCH;

        // `myView' couldn't verify it all.
        return QueryStatus.DONTKNOW;
    }
}
