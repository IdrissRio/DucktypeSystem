package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.AbstractActor;
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

public class DSQueryChecker extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    // CHECKME: DSQueryChecker and MainRobot are on the same node: they can share resources.
    // Reference to the MainRobot view: the MainRobot must not clone its own view in its Checkers when constructing them.
    private DSGraph myView;
    private String myNode;
    private String version;
    private DSQuery query;
    private ActorRef mediator;
    private ActorRef robot;

    public DSQueryChecker(DSGraph myView, String myNode, String version) {
        this.myView = myView;
        this.myNode = myNode;
        this.version = version;
        this.mediator = DistributedPubSub.get(getContext().system()).mediator();
        this.mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        log.info("QC: SONO NATO: "+getSelf().path()+ " con vista: "+myView.toString());
    }

    // Communication methods
    private void publishMatch() {
        log.info("MATCH da: "+ myNode);
    }
    private void publishFail() {
        log.info("FAIL da: "+ myNode);
    }
    private void forwardQuery(boolean unsubscribe) { /* TODO:
        if (unsubscribe) unsubscribe group "query.getVersion()"
        Send to that group this.query.  */
        // if (unsubscribe) mediator.tell(new DistributedPubSubMediator.Remove("/user/ROBOT/prova"), getSelf());

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DSTryNewQuery.class, msg -> {
                    // FIXME: is this clone necessary? Can I steal the messages resources?
                    query = new DSQueryImpl(msg.query);

                    // robot.tell(new DSStartCriticalWork(msg.sender), ActorRef.noSender());
                    // msg.sender.tell(new DSAck(), ActorRef.noSender());
                    switch (query.checkAndReduce(myView, myNode)) {
                        case FAIL:
                            publishFail(); break;
                        case MATCH:
                            publishMatch(); break;
                        default: // case NEW or DONTKNOW
                            forwardQuery(true);
                    }
                    // robot.tell(new DSEndCriticalWork(), ActorRef.noSender());
                })
                .match(DSAskNewSend.class, x -> {
                    forwardQuery(false);
                })
                .match(DSQueryImpl.class, x-> {
                    // TRY FORWARDING:
                    mediator.tell(new DistributedPubSubMediator.Remove("/user/ROBOT/prova"), getSelf());
                    Thread.sleep(1000);
                    log.info(myNode + "ho ricevuto: " + x.toString());
                    mediator.tell(new DistributedPubSubMediator.Send("/user/ROBOT/prova",
                            new DSQueryImpl(x), false), getSelf());
                })
                // FIXME: .match(EndTimerAck.class, x -> { create new cluster send the x.version currentQuery })
                .build();
    }

    static public Props props(DSGraph myView, String myNode, String version) {
        return Props.create(DSQueryChecker.class, () -> new DSQueryChecker(myView, myNode,version));
    }
}
