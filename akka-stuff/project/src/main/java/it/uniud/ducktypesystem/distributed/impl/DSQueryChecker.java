package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import it.uniud.ducktypesystem.distributed.message.*;

public class DSQueryChecker extends AbstractActor {
    // CHECKME: DSQueryChecker and MainRobot are on the same node: they can share resources.
    // Reference to the MainRobot view: the MainRobot must not clone its own view in its Checkers when constructing them.
    private DSGraph myView;
    private String myNode;
    private String version;
    private DSQuery query;
    private ActorRef mediator;
    private ActorRef robot;

    private enum QueryStatus {
        MATCH,
        FAIL,
        NEW,
        DONTKNOW
    }

    public DSQueryChecker(DSGraph myView, String myNode, String version) {
        this.myView = myView;
        this.myNode = myNode;
        this.version = version;
        this.mediator = DistributedPubSub.get(getContext().system()).mediator();
        this.mediator.tell(new DistributedPubSubMediator.Subscribe(
                version, getSelf()), getSelf());
    }

    // Graph manage methods
    private QueryStatus checkAndReduce() {
        assert(!query.isRedundant());
        boolean newHypothesis = false;

        for (String qN : query.getNodes()) {
            if (!myView.hasNode(qN)) continue;
            // If my knowledge about `qN' is complete and more edges are required, then query fails.
            if (qN.equals(myNode) && !myView.adjNodes(qN).containsAll(query.adjNodes(qN)))
                return QueryStatus.FAIL;
            // Remove verified edges
            for (String qN2 : query.adjNodes(qN)) {
                if (!myView.hasNode(qN2)) continue;
                query.removeEdge(qN, qN2);
                newHypothesis = true;
            }
        }
        query.shrinkRedundancies();

        // If I was able to verify it all, then query mathces.
        if (query.isEmpty())
            return QueryStatus.MATCH;

        // My knowledge let me simplify the query.
        if (newHypothesis)
            return QueryStatus.NEW;

        // I had nothing to say about it.
        return QueryStatus.DONTKNOW;
    }

    // Communication methods
    private void publishMatch() { /* TODO: */ }
    private void publishFail() { /* TODO: */ }
    private void forwardQuery(boolean unsubscribe) { /* TODO:
        if (unsubscribe) unsubscribe group "query.getVersion()"
        Send to that group this.query.  */
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DSTryNewQuery.class, msg -> {
                    // FIXME: is this clone necessary? Can I steal the messages resources?
                    query = msg.query.clone();
                    // robot.tell(new DSStartCriticalWork(msg.sender), ActorRef.noSender());
                    msg.sender.tell(new DSAck(), ActorRef.noSender());
                    switch (checkAndReduce()) {
                        case FAIL:
                            publishFail(); break;
                        case MATCH:
                            publishMatch(); break;
                        case NEW:
                            forwardQuery(true); break;
                        default: break; // case DONTKNOW
                    }
                    // robot.tell(new DSEndCriticalWork(), ActorRef.noSender());
                })
                .match(DSAskNewSend.class, x -> {
                    forwardQuery(false);
                })
                // FIXME: .match(EndTimerAck.class, x -> { create new cluster send the x.version currentQuery })
                .build();
    }
}
