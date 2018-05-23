package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorKilledException;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import it.uniud.ducktypesystem.distributed.data.DSQueryImpl;
import it.uniud.ducktypesystem.distributed.message.*;

import java.util.concurrent.ThreadLocalRandom;

public class DSQueryChecker extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    // CHECKME: DSQueryChecker and MainRobot are on the same node: they can share resources.
    // Reference to the MainRobot view: the MainRobot must not clone its own view in its Checkers when constructing them.
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
        log.info("QC: SONO NATO: "+getSelf().path()+ " con vista: "+myView.toString());
    }

    // Communication methods
    private void publishQueryResult(DSQuery.QueryStatus status) {
        getContext().getParent().tell(new DSEndCriticalWork(this.queryId), getSelf());
        log.info((status == DSQuery.QueryStatus.MATCH) ? "MATCH da: "+ myNode
                : (status == DSQuery.QueryStatus.FAIL) ? "FAIL da: " : "DONTKNOW da: " + myNode);
        mediator.tell(new DistributedPubSubMediator.Send("/user/CLUSTERMANAGER"+this.queryId.getHost(),
                new DSMissionAccomplished(this.queryId, this.query.serializeToString(), status),
                false), getSelf());
        mediator.tell(new DistributedPubSubMediator.Remove("/user/ROBOT/"+this.queryId.getPath()), getSelf());

        mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                new DSEndQuery(this.queryId), false), getSelf());
    }

    private void forwardQuery(int ttl) {
        log.info("FORWARDING da "+myNode+": query: "+query.toString());
        mediator.tell(new DistributedPubSubMediator.Remove("/user/ROBOT/"+this.queryId.getPath()), getSelf());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediator.tell(new DistributedPubSubMediator.Send("/user/ROBOT/"+this.queryId.getPath(),
                new DSTryNewQuery(this.query.serializeToString(), ttl-1) , false), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DSTryNewQuery.class, msg -> {
                    this.query = new DSQueryImpl();
                    this.query.loadFromSerializedString(msg.getSerializedQuery());
                    getContext().getParent().tell(new DSStartCriticalWork(this.queryId), getSelf());

                    DSQuery.QueryStatus status = query.checkAndReduce(myView, myNode);

                    // Simulate casual death during critical work
                    boolean shouldIDie = (ThreadLocalRandom.current().nextInt(0, 5) == 0);
                    if (shouldIDie) throw new ActorKilledException("CASSUU!!!");

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
        return Props.create(DSQueryChecker.class, () -> new DSQueryChecker(myView, myNode, qId));
    }
}
