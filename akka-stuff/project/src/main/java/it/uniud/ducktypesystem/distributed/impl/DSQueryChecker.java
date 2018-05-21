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
    private int nr;
    private String path;
    private DSQuery query;
    private ActorRef mediator;

    public DSQueryChecker(DSGraph myView, String myNode, String version, int nr) {
        this.myView = myView;
        this.myNode = myNode;
        this.version = version;
        this.nr = nr;
        this.path = ""+version+"."+nr;
        this.mediator = DistributedPubSub.get(getContext().system()).mediator();
        this.mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        log.info("QC: SONO NATO: "+getSelf().path()+ " con vista: "+myView.toString());
    }

    // Communication methods
    private void publishQueryResult(DSQuery.QueryStatus status) {
        log.info((status == DSQuery.QueryStatus.MATCH) ?
                "MATCH da: "+ myNode : "FAIL da: "+ myNode);
        mediator.tell(new DistributedPubSubMediator.Remove("/user/ROBOT/"+this.path), getSelf());
        mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT/"+this.path,
                new DSMissionAccomplished(this.version, null, status), true), getSelf());
        mediator.tell(new DistributedPubSubMediator.Send("/user/CLUSTERMANAGER",
                new DSMissionAccomplished(this.version, null, status), false), getSelf());
    }

    private void forwardQuery(boolean unsubscribe, int left) {
        log.info("FORWARDING da "+myNode+": query: "+query.toString());
        if (unsubscribe) {
            mediator.tell(new DistributedPubSubMediator.Remove("/user/ROBOT/"+this.path), getSelf());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        DSTryNewQuery msg = new DSTryNewQuery();
        msg.sender = getSelf();
        msg.serializedQuery = this.query.serializeToString();
        msg.left = unsubscribe ? left - 1 : left;
        mediator.tell(new DistributedPubSubMediator.Send("/user/ROBOT/"+this.path,
                msg , false), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DSTryNewQuery.class, msg -> {
                    this.query = new DSQueryImpl();
                    this.query.loadFromSerializedString(msg.serializedQuery);
                    // robot.tell(new DSStartCriticalWork(msg.sender), ActorRef.noSender());
                    // msg.sender.tell(new DSAck(), ActorRef.noSender());
                    DSQuery.QueryStatus status = query.checkAndReduce(myView, myNode);
                    switch (status) {
                        case FAIL:
                        case MATCH:
                            publishQueryResult(status); break;
                        default: // case NEW or DONTKNOW
                            if (msg.left == 0) {
                                mediator.tell(new DistributedPubSubMediator.Remove("/user/ROBOT/"+this.path), getSelf());
                                // FIXME: let mainActor kill its child instead?
                                mediator.tell(new DistributedPubSubMediator.Send("/user/CLUSTERMANAGER",
                                        new DSMissionAccomplished(this.version, this.query.serializeToString(), DSQuery.QueryStatus.DONTKNOW), false), getSelf());
                            }
                            else
                                forwardQuery(true, msg.left);
                    }
                    // robot.tell(new DSEndCriticalWork(), ActorRef.noSender());
                })
                /*.match(DSAskNewSend.class, x -> {
                    forwardQuery(false);
                })*/
                .match(DSMissionAccomplished.class, x -> {
                    log.info(myNode + ": qualcuno ha finito: mi disinscrivo.");
                    mediator.tell(new DistributedPubSubMediator.Remove("/user/ROBOT/"+this.version), getSelf());
                    // FIXME : kill myself?
                })
                // FIXME: .match(EndTimerAck.class, x -> { create new cluster send the x.version currentQuery })
                .build();
    }

    static public Props props(DSGraph myView, String myNode, String version, int nr) {
        return Props.create(DSQueryChecker.class, () -> new DSQueryChecker(myView, myNode, version, nr));
    }
}
