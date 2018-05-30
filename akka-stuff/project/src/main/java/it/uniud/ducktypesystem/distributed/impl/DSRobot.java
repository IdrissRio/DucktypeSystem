package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DSGraphImpl;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import it.uniud.ducktypesystem.distributed.data.DataFacade;
import it.uniud.ducktypesystem.distributed.messages.*;
import it.uniud.ducktypesystem.distributed.errors.DSSystemError;
import it.uniud.ducktypesystem.distributed.errors.DSSystemFailureSimulation;

import java.util.ArrayList;
import java.util.HashMap;

public class DSRobot extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

    private enum QCStatus {
        WAITING,
        CRITICAL,
        DONE
    }
    private class QCMonitor {
        ActorRef queryChecker;
        QCStatus status;
        QCMonitor(ActorRef queryChecker) {
            this.queryChecker = queryChecker;
            this.status = QCStatus.WAITING;
        }
    }

    private String myName;
    private DSGraph myView;
    private String myNode;
    private String lastStep;
    private HashMap activeQueryCheckers;

    public DSRobot(int index) {
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        this.myName = "Robot" + index;
        try {
            this.myNode = DataFacade.getInstance().getOccupied().get(index);
        } catch (DSSystemError dsSystemError) {
            dsSystemError.printStackTrace();
            return;
        }
        this.myView = new DSGraphImpl();
        this.myView.obtainView(myNode);
        this.lastStep = null;
        this.activeQueryCheckers = new HashMap();
        log.info(myName + " created on " + myNode + " with view " + myView.toString());
    }


    @Override
    public void postRestart(Throwable reason) {
        mediator.tell(new DistributedPubSubMediator.Publish("CLUSTERINFO",
                new DSRobotFailureOccurred(myNode)), getSelf());
        mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                new DSRobotFailureOccurred(myNode), false), ActorRef.noSender());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DSMove.class, x -> {
                    // Simulate Robot Death.
                    if (DataFacade.getInstance().shouldFailMove())
                        throw new DSSystemFailureSimulation(myName + " DIED.");

                    String tmp = myNode;
                    myNode = myView.obtainNewView(myNode, lastStep);
                    lastStep = tmp;

                    /* Note: This is just for presentation purposes:
                     * We DON'T claim to know the exact position of every robot. */
                    ArrayList<String> occupied = DataFacade.getInstance().getOccupied();
                    for (int i = occupied.size(); i-- > 0; )
                        if (occupied.get(i).equals(lastStep)) {
                            occupied.set(i, myNode);
                            break;
                        }
                })
                .match(DSStartCriticalWork.class, msg -> {
                    // log.info("Un mio figlio "+ getSender() + "è in critical work...");
                    QCMonitor qcMonitor = (QCMonitor) activeQueryCheckers.get(msg.getQueryId().getPath());
                    if (qcMonitor != null)
                        qcMonitor.status = QCStatus.CRITICAL;
                })
                .match(DSEndCriticalWork.class, msg -> {
                    // log.info("Un mio figlio "+ getSender()+" ha finito la critical work...");
                    QCMonitor qcMonitor = (QCMonitor) activeQueryCheckers.get(msg.getQueryId().getPath());
                    if (qcMonitor != null)
                        qcMonitor.status = QCStatus.DONE;
                })
                .match(DSEndQuery.class, msg -> {
                    QCMonitor qcMonitor = (QCMonitor) activeQueryCheckers.get(msg.getQueryId().getPath());
                    if (qcMonitor == null) return;
                    ActorRef qChecker = qcMonitor.queryChecker;
                    getContext().unwatch(qChecker);
                    getContext().stop(qChecker);
                    activeQueryCheckers.remove(msg.getQueryId().getPath());
                })
                .match(Terminated.class, x -> {
                    String deadPath = x.actor().path().name();
                    if (activeQueryCheckers.get(deadPath) != null
                            && ((QCMonitor)activeQueryCheckers.get(deadPath)).status == QCStatus.CRITICAL) {
                        // log.info(deadPath + " is DEAD during CRITICAL WORK!!!!");
                        DSQuery.QueryId qId = new DSQuery.QueryId(deadPath);
                        mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                                new DSEndQuery(qId), false), getSelf());
                        mediator.tell(new DistributedPubSubMediator.Send("/user/CLUSTERMANAGER"+ qId.getHost(),
                                new DSRetryQuery(qId), false), getSelf());
                    }
                    else if (activeQueryCheckers.get(deadPath) != null
                            && ((QCMonitor)activeQueryCheckers.get(deadPath)).status == QCStatus.WAITING) {
                        // log.info(deadPath + " is DEAD in WAITING!!!!");
                        log.info("Recreating QueryChecker...");
                        ActorRef child = getContext().actorOf(DSQueryChecker.props(this.myView, this.myNode,
                                new DSQuery.QueryId(deadPath)), deadPath);
                        getContext().watch(child);
                        activeQueryCheckers.put(deadPath, new QCMonitor(child));
                    }
                    else
                        log.info("QueryChecker on "+ deadPath + " is DEAD in peace.");
                })
                .match(DSCreateQueryChecker.class, in -> {
                    ActorRef child = getContext().actorOf(DSQueryChecker.props(this.myView, this.myNode,
                            in.getQueryId()), in.getQueryId().getPath());
                    getContext().watch(child);
                    activeQueryCheckers.put(in.getQueryId().getPath(), new QCMonitor(child));
                    Thread.sleep(500);

                    // Simulate QueryChecker's Death in WAITING.
                    if (DataFacade.getInstance().shouldDieInWaiting()) {
                        log.info("QueryChecker DEATH in WAITING.");
                        getContext().stop(child);
                    }
                })
                .match(DSRobotFailureOccurred.class, in -> {
                    log.info(myName + " killing all my sons.");
                    for (ActorRef each : getContext().getChildren()) {
                        getContext().unwatch(each);
                        getContext().stop(each);
                    }
                })
                .build();
    }
    static public Props props(int index) {
        return Props.create(DSRobot.class, index);
    }
}