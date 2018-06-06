package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.uniud.ducktypesystem.distributed.data.*;
import it.uniud.ducktypesystem.distributed.errors.DSSystemError;
import it.uniud.ducktypesystem.distributed.messages.*;
import it.uniud.ducktypesystem.distributed.system.DSDataFacade;

import static it.uniud.ducktypesystem.distributed.data.DSQuery.QueryStatus.FAIL;
import static it.uniud.ducktypesystem.distributed.data.DSQuery.QueryStatus.MATCH;

/**
 * This is an Actor created by a DSRobot for a specific `queryId' verification.
 * It receives the query to be checked and reduces it;
 * then it publishes the obtained result or forwards the query still to be verified;
 * it can fail before having seen the query (it must be recreated by its DSRobot);
 * it can fail during the checking phase (its DSRobot is responsible of stopping and retrying the query);
 * it can fail after having seen the query.
 */
public class DSQueryChecker extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    // It is created with a copy of its robot's view,
    // so that the check phase is protected from cuncurrent updates of the view, due to move orders.
    private DSGraph myView;
    private String myNode;
    // It is created with a `queryId' representing the path on which it listens.
    private DSQuery.QueryId queryId;
    // It does not have the actual `query' until it receives it from other DSQueryCheckers.
    private DSQuery query;
    private ActorRef mediator;

    public DSQueryChecker(DSGraph myView, String myNode, DSQuery.QueryId queryId) {
        this.myView = myView;
        this.myNode = myNode;
        this.queryId = queryId;
        this.query = null;
        this.mediator = DistributedPubSub.get(getContext().system()).mediator();
        this.mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        log.info("QueryChecker created on " + myNode + " for query "+ queryId.getPath());
    }

    private void publishQueryResult(DSQuery.QueryStatus status) {
        log.info((status == MATCH) ? "MATCH form: "+ myNode
                : (status == FAIL) ? "FAIL from: " : "DONTKNOW from: " + myNode);
        mediator.tell(new DistributedPubSubMediator.Send("/user/CLUSTERMANAGER"+this.queryId.getHost(),
                new DSMissionAccomplished(this.queryId, this.query.serializeToString(), status),
                false), getSelf());
        mediator.tell(new DistributedPubSubMediator.Remove("/user/ROBOT/"+this.queryId.getPath()), getSelf());

        mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                new DSEndQuery(this.queryId), false), getSelf());
    }

    private void forwardQuery(int ttl) throws DSSystemError, InterruptedException {
        log.info("FORWARDING from "+ myNode +": query: "+ query.toString());
        // Declares not to be interested in this path anymore:
        // This avoids that the same query is seen twice by the same queryChecker.
        mediator.tell(new DistributedPubSubMediator.Remove("/user/ROBOT/"+this.queryId.getPath()), getSelf());

        Thread.sleep(1000);
        mediator.tell(new DistributedPubSubMediator.Send("/user/ROBOT/"+this.queryId.getPath(),
                new DSTryNewQuery(this.query.serializeToString(), ttl-1, queryId) , false), getSelf());

        // Simulate QueryChecker's Death in DONE
        if (DSDataFacade.getInstance().shouldDieInWaiting()) {
            log.info("QueryChecker DEATH in DONE.");
            getContext().stop(getSelf());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DSTryNewQuery.class, msg -> {
                    this.query = new DSQueryImpl();
                    this.query.loadFromSerializedString(msg.getSerializedQuery());

                    // Inform its DSRobot that its status is now CRITICAL.
                    getContext().getParent().tell(new DSStartCriticalWork(this.queryId), getSelf());

                    // Actually check the query and remove the verified edges.
                    DSQuery.QueryStatus status = query.checkAndReduce(myView, myNode);

                    // Simulate QueryChecker's Death during critical work
                    if (DSDataFacade.getInstance().shouldFailInCriticalWork()) {
                        log.info("QueryChecker DEATH in CRITICAL WORK.");
                        getContext().stop(getSelf());
                        return;
                    }

                    // Inform its DSRobot that its status is now DONE.
                    getContext().getParent().tell(new DSEndCriticalWork(this.queryId), getSelf());

                    if (status == MATCH || status == FAIL) {
                        publishQueryResult(status);
                        return;
                    }
                    // Here its query verification ended with DONTKNOW
                    if (msg.getTTL() == 0)
                        // There are no more queryChecker to forward to.
                        // The whole query ends with DONTKNOW.
                        publishQueryResult(status);
                    else
                        // Forward to the other queryChecker still waiting to see the query.
                        forwardQuery(msg.getTTL());
                })
                .build();
    }

    static public Props props(DSGraph myView, String myNode, DSQuery.QueryId qId) {
        return Props.create(DSQueryChecker.class, myView, myNode, qId);
    }
}
