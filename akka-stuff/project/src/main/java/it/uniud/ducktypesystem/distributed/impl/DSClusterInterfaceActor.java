package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.uniud.ducktypesystem.distributed.data.DSCluster;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import it.uniud.ducktypesystem.distributed.messages.*;

public class DSClusterInterfaceActor extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    private int numRobots;
    private int host;

    public DSClusterInterfaceActor(int host, int numRobots) {
        this.host = host;
        this.numRobots = numRobots;
        getContext().system().eventStream().subscribe(getSelf(), DeadLetter.class);
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        mediator.tell(new DistributedPubSubMediator.Subscribe("CLUSTERINFO", getSelf()), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DSMissionAccomplished.class, msg -> {
                    DSCluster.getInstance().endedQuery(msg.getQueryId(), msg.getSerializedQuery(), msg.getStatus());
                    DSCluster.getInstance().getView().updateQuery(msg.getQueryId(), msg.getStatus());
                })
                .match(DSMove.class, msg -> {
                    log.info("Moving...");
                    mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                            new DSMove(), true), getSelf());
                    Thread.sleep(2000);
                    // Note: again, this is just for debugging purposes.
                    DSCluster.getInstance().getView().updateRobotsPosition();
                })
                .match(DSStartQueryCheck.class, msg -> {
                    // Start a new Query.
                    mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                            new DSCreateQueryChecker(msg.getQueryId()), true), getSelf());
                    Thread.sleep(2000);
                    mediator.tell(new DistributedPubSubMediator.Send("/user/ROBOT/"+msg.getQueryId().getPath(),
                            new DSTryNewQuery(msg.getSerializedQuery(), msg.getTTL(), msg.getQueryId()),
                            false), getSelf());
                })
                .match(DSRetryQuery.class, msg -> {
                    log.info("Retrying query...");
                    DSCluster.getInstance().retryQuery(msg.getQueryId());
                })
                .match(DeadLetter.class, deadLetter -> {
                    log.info("Dead Letter encountered. Some critical failure combination occurred!");
                    if (deadLetter.message().getClass() == DSTryNewQuery.class) {
                        DSQuery.QueryId id = ((DSTryNewQuery) deadLetter.message()).getId();
                        mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                                new DSEndQuery(id), false), getSelf());
                    }

                })
                .match(DSRobotFailureOccurred.class, in -> {
                    log.info("CLUSTER"+ host + " was informed that robot in " + in.getDeadNode() + " died.");
                    DSCluster.getInstance().getView()
                            .showErrorMessage("Host < "+ host +" >: Robot died. It was recreated in " + in.getDeadNode());
                })
                .match(DSEndQuery.class, in -> {
                    log.info("CLUSTER"+ host + " stopping query "+ in.getQueryId().getName());
                    mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                            in, false), getSelf());
                })
                .build();
    }

    static public Props props(int host, int numRobots) {
        return Props.create(DSClusterInterfaceActor.class, host, numRobots);
    }
}
