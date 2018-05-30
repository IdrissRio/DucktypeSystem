package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.DeadLetter;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.uniud.ducktypesystem.distributed.data.*;
import it.uniud.ducktypesystem.distributed.errors.DSSystemError;
import it.uniud.ducktypesystem.distributed.messages.*;

public class DSQueryChecker extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private DSGraph myView;
    private String myNode;
    private DSQuery.QueryId queryId;
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

    // Communication methods
    private void publishQueryResult(DSQuery.QueryStatus status) {
        log.info((status == DSQuery.QueryStatus.MATCH) ? "MATCH form: "+ myNode
                : (status == DSQuery.QueryStatus.FAIL) ? "FAIL from: " : "DONTKNOW da: " + myNode);
        mediator.tell(new DistributedPubSubMediator.Send("/user/CLUSTERMANAGER"+this.queryId.getHost(),
                new DSMissionAccomplished(this.queryId, this.query.serializeToString(), status),
                false), getSelf());
        mediator.tell(new DistributedPubSubMediator.Remove("/user/ROBOT/"+this.queryId.getPath()), getSelf());

        mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                new DSEndQuery(this.queryId), false), getSelf());
    }

    private void forwardQuery(int ttl) throws DSSystemError {
        log.info("FORWARDING from "+myNode+": query: "+query.toString());
        mediator.tell(new DistributedPubSubMediator.Remove("/user/ROBOT/"+this.queryId.getPath()), getSelf());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediator.tell(new DistributedPubSubMediator.Send("/user/ROBOT/"+this.queryId.getPath(),
                new DSTryNewQuery(this.query.serializeToString(), ttl-1, queryId) , false), getSelf());

        // Simulate QueryChecker's Death in DONE
        if (DataFacade.getInstance().shouldDieInWaiting()) {
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
                    getContext().getParent().tell(new DSStartCriticalWork(this.queryId), getSelf());

                    DSQuery.QueryStatus status = query.checkAndReduce(myView, myNode);

                    // Simulate QueryChecker's Death during critical work
                    if (DataFacade.getInstance().shouldFailInCriticalWork()) {
                        log.info("QueryChecker DEATH in CRITICAL WORK.");
                        getContext().stop(getSelf());
                        return;
                    }

                    getContext().getParent().tell(new DSEndCriticalWork(this.queryId), getSelf());

                    switch (status) {
                        case FAIL:
                        case MATCH:
                            publishQueryResult(status); break;
                        default: // DONTKNOW
                            if (msg.getTTL() == 0) {
                                // Query ended with DONTKNOW.
                                publishQueryResult(status);
                            }
                            else
                                forwardQuery(msg.getTTL());
                    }
                })
                .build();
    }

    static public Props props(DSGraph myView, String myNode, DSQuery.QueryId qId) {
        return Props.create(DSQueryChecker.class, myView, myNode, qId);
    }
}
