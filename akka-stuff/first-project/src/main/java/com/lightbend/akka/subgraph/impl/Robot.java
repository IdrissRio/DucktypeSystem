package com.lightbend.akka.subgraph.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.lightbend.akka.subgraph.data.AbstractGraph;
import com.lightbend.akka.subgraph.data.Graph;
import com.lightbend.akka.subgraph.data.Query;
import com.lightbend.akka.subgraph.messages.*;

public class Robot extends AbstractActor {
    private ActorRef supervisor;

    private Graph myGraph;
    private AbstractGraph.Node myNode;
    /* FIXME:
     * we need to save MORE than one current Query if it has to hold the current Query
     * until it it correctly forwarded. This could be a List<Query> indexed by version hash nr?
     */
    private Query currentQuery;
    private enum QueryStatus {
        MATCH,
        FAIL,
        NEW,
        DONTKNOW
    }

    // Comunication methods
    private void subscribe() { /* TODO: */ }
    private void secureSend(Query.Version version) { /* TODO:
     // FIXME : it can't wait inside onReceive!
     * Send to version cluster
     * timeout.tell(Start(version))
     */
    }
    private void publishMatch() { /* TODO: */ }
    private void publishFail() { /* TODO: */ }

    // Graph manage methods
    private QueryStatus checkAndReduce() {
        // FIXME: select right currentQuery from version?
        Query newQuery = currentQuery.cloneQuery();

        assert(!newQuery.isRedundant());
        boolean newHypothesis = false;

        /* FIXME PSEUDOCODE:
        for (auto qN : currentQuery.getNodes()) {
            // Ordered search to improve efficiency
            if (binary_search(qN, myGraph.getNodes(), myN)) {
                if (myN.isEqual(myNode)
                  && !subset(currentQuery.adjNodes(n), myGraph.adjNodes(j))) {
                    return QueryStatus.FAIL;
                }
                // Remove verified edges
                for (auto qN2 : currentQuery.adjNodes(qN)) {
                    if (binary_search(qN2, myGraph.adjNodes(myN))) {
                        newQuery.removeEdge(qN, qN2);
                        newHypothesis = true;
                    }
                }
            }
        }
        */

        if (newHypothesis)
            newQuery.shrinkRedundancies();
        if (newQuery.isEmpty())
            return QueryStatus.MATCH;
        currentQuery = newQuery;
        return QueryStatus.NEW;
    }

    public Robot(AbstractGraph.Node myNode) {
        this.myNode = myNode;
        this.myGraph = new Graph(myNode);
        // FIXME: this.supervisor = system.ActorOf(Supervisor.class) ?? è lui che lo deve creare?
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TryNewQuery.class, msg -> {
                    // FIXME: select correct currentQuery from version?
                    currentQuery = msg.query;
                    supervisor.tell(new StartCriticalWork(msg.sender), ActorRef.noSender());
                    msg.sender.tell(new Ack(), ActorRef.noSender());
                    switch (checkAndReduce()) {
                        case FAIL:
                            publishFail(); break;
                        case MATCH:
                            publishMatch(); break;
                        case NEW:
                            secureSend(currentQuery.version); break;
                        default: // throw MyFaultProjectError ?
                    }
                    supervisor.tell(new EndCriticalWork(), ActorRef.noSender());
                })
                .match(Move.class, x -> {
                    myGraph.addNode(myGraph.chooseNext(myNode));
                })
                .match(AskNewSend.class, x -> {
                    secureSend(x.version);
                })
                // FIXME: .match(Subscibe.class, x -> { subscribe() }
                // FIXME: .match(EndTimerAck.class, x -> { create new cluster send the x.version currentQuery })
                .build();
    }
}
