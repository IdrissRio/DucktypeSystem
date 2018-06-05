package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.uniud.ducktypesystem.distributed.system.DSCluster;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import it.uniud.ducktypesystem.distributed.messages.*;

/**
 * This is the Actor running on the host.
 * It provides an access point to the cluster for the user, having access to the static module `DSCluster'
 * and, consequently, to the `DSView' methods.
 * It can ask to start a new Query and may be asked to retry a query which ended after internal failure;
 * it is informed about the query final result and about robots' failures,
 * and has to inform back the user.
 */
public class DSClusterInterfaceActor extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    private int numRobots;
    private int host;

    public DSClusterInterfaceActor(int host, int numRobots) {
        this.host = host;
        this.numRobots = numRobots;
        getContext().system().eventStream().subscribe(getSelf(), DeadLetter.class);
        // It listens on "/user/CLUSTERMANAGER`host'" path, for the results of its submitted queries,
        // and on "CLUSTERINFO" subject, for the messages about robot's failures.
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        mediator.tell(new DistributedPubSubMediator.Subscribe("CLUSTERINFO", getSelf()), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
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
                    log.info("Retrying query " + msg.getQueryId().getName() + "...");
                    DSCluster.getInstance().retryQuery(msg.getQueryId());
                })
                .match(DSMissionAccomplished.class, msg -> {
                    DSCluster.getInstance().endedQuery(msg.getQueryId(), msg.getSerializedQuery(), msg.getStatus());
                    DSCluster.getInstance().getView().updateQuery(msg.getQueryId(), msg.getStatus());
                })
                .match(DSEndQuery.class, in -> {
                    log.info("HOST "+ host + " forces query "+ in.getQueryId().getName() + " stop.");
                    mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                            in, false), getSelf());
                })
                .match(DSMove.class, msg -> {
                    log.info("Moving robots...");
                    mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                            new DSMove(), true), getSelf());
                    Thread.sleep(2000);
                    // Note: again, this is just for debugging purposes.
                    DSCluster.getInstance().getView().updateRobotsPosition();
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
                .build();
    }

    static public Props props(int host, int numRobots) {
        return Props.create(DSClusterInterfaceActor.class, host, numRobots);
    }
}
