package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.DeadLetter;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.uniud.ducktypesystem.distributed.data.DSCluster;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import it.uniud.ducktypesystem.distributed.message.DSCreateChild;
import it.uniud.ducktypesystem.distributed.message.DSMissionAccomplished;
import it.uniud.ducktypesystem.distributed.message.DSMove;
import it.uniud.ducktypesystem.distributed.message.DSTryNewQuery;

public class DSClusterManagerActor extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    private int numRobots;

    public DSClusterManagerActor(int numRobots) {
        this.numRobots = numRobots;
        getContext().system().eventStream().subscribe(getSelf(), DeadLetter.class);
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DSMissionAccomplished.class, msg -> {
                    DSCluster.getInstance().endedQuery(msg.getVersion(), msg.getSerializedQuery());
                    DSCluster.getInstance().getView().updateQuery(msg.getVersion(), msg.getStatus());
                })
                .match(DSMove.class, msg -> {
                    log.info("Moving...");
                    mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                            new DSMove(), true), getSelf());
                    Thread.sleep(2000);
                    // FIXME: again, this is just for debugging purposes.
                    DSCluster.getInstance().getView().updateRobotsPosition();
                })
                .match(DSCreateChild.class, create -> {
                    // Start a new Query.
                    mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT", create, true), getSelf());
                    Thread.sleep(3000);
                    DSTryNewQuery msg = new DSTryNewQuery();
                    msg.sender = getSelf();
                    msg.left = create.getNumRobot();
                    msg.serializedQuery = create.getSerializedQuery();
                    mediator.tell(new DistributedPubSubMediator.Send("/user/ROBOT/"+create.getVersion()+"."+create.getNr(),
                                msg, false), getSelf());
                })
                .match(DeadLetter.class, deadLetter -> {
                    log.info("DEAD LETTER");
                })
                .build();
    }

    static public Props props(int numRobots) {
        return Props.create(DSClusterManagerActor.class, () -> new DSClusterManagerActor(numRobots));
    }
}
