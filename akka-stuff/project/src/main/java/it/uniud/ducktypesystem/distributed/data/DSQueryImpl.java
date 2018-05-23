package it.uniud.ducktypesystem.distributed.data;

import it.uniud.ducktypesystem.errors.SystemError;

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
    public DSQueryImpl(DSQuery q) {
        super(q);
        id = new QueryId(q.getId());
    }

    public static DSQuery createQueryFromFile(String filePath) throws SystemError {
        DSQuery q = new DSQueryImpl(DSGraphImpl.createGraphFromFile(filePath));
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

    public DSQuery.QueryStatus checkAndReduce(DSGraph myView, String myNode) {
        assert(!hasUnconnectedNodes());

        for (String qN : getNodes()) {
            if (!myView.hasNode(qN)) continue;
            // If the knowledge about `qN' is complete and more edges are required, then query fails.
            if (qN.equals(myNode) && !myView.adjNodes(qN).containsAll(adjNodes(qN)))
                return QueryStatus.FAIL;
            // Remove verified edges
            for (String qN2 : adjNodes(qN)) {
                if (!myView.hasNode(qN2) || !myView.areAdj(qN, qN2)) continue;
                removeEdge(qN, qN2);
            }
        }
        removeUnconnectedNodes();

        // `myView' verified it all.
        if (isEmpty())
            return QueryStatus.MATCH;

        // `myView' had nothing to say about it.
        return QueryStatus.DONTKNOW;
    }
}
