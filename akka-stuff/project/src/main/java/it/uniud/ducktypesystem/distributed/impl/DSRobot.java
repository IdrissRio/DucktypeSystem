package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.uniud.ducktypesystem.distributed.system.DSDataFacade;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DSGraphImpl;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import it.uniud.ducktypesystem.distributed.messages.*;
import it.uniud.ducktypesystem.distributed.errors.DSSystemError;
import it.uniud.ducktypesystem.distributed.errors.DSSystemFailureSimulation;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is the main actor on the Robot ActorSystem.
 * It loads in `myView' the view of the main graph from `myNode' position;
 * creates a son `DSQueryChecker' for each new query path id;
 * holds an array of `activeQueryCheckers' for its active sons and is responsible of their failures;
 * it can move updating its view knowledge;
 * it can fail and be recreated on the same node: it is responsible of informing the other robots about this.
 */
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
    // The view from its occupied node is loaded from `DSDataFacade'
    // (simulating the use of its physical sensors).
    private DSGraph myView;
    private String myNode;
    // This parameter is useful in moving heuristics.
    private String lastStep;
    /* Hash map holding the active sons with their status:
     * - key   = queryPath (String),
     * - value = child (QCMonitor) */
    private HashMap activeQueryCheckers;

    public DSRobot(int index) {
        this.myName = "Robot" + index;
        try {
            this.myNode = DSDataFacade.getInstance().getOccupied().get(index);
        } catch (DSSystemError dsSystemError) {
            dsSystemError.printStackTrace();
            return;
        }
        this.myView = new DSGraphImpl();
        this.myView.obtainView(myNode);
        this.lastStep = null;
        this.activeQueryCheckers = new HashMap();
        // It listens on "/user/ROBOT/" path, receiving orders.
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        log.info(myName + " created on " + myNode + " with view " + myView.toString());
    }


    /* Method invoked after Robot's death.  */
    @Override
    public void postRestart(Throwable reason) {
        // Inform all the host's interfaces about the failure.
        mediator.tell(new DistributedPubSubMediator.Publish("CLUSTERINFO",
                new DSRobotFailureOccurred(myNode)), getSelf());
        // Make all robot stop active queries.
        mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                new DSRobotFailureOccurred(myNode), false), ActorRef.noSender());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DSMove.class, x -> {
                    // Simulate Robot Death.
                    if (DSDataFacade.getInstance().shouldFailMove())
                        throw new DSSystemFailureSimulation(myName + " DIED.");

                    String tmp = myNode;
                    myNode = myView.obtainNewView(myNode, lastStep);
                    lastStep = tmp;

                    /* Note: This is just for presentation purposes:
                     * We DON'T claim to know the exact position of every robot. */
                    ArrayList<String> occupied = DSDataFacade.getInstance().getOccupied();
                    for (int i = occupied.size(); i-- > 0; )
                        if (occupied.get(i).equals(lastStep)) {
                            occupied.set(i, myNode);
                            break;
                        }
                })
                .match(DSStartCriticalWork.class, msg -> {
                    // NOISY LOG: log.info("My son "+ getSender() + "is in critical work...");
                    QCMonitor qcMonitor = (QCMonitor) activeQueryCheckers.get(msg.getQueryId().getPath());
                    if (qcMonitor != null)
                        qcMonitor.status = QCStatus.CRITICAL;
                })
                .match(DSEndCriticalWork.class, msg -> {
                    // NOISY LOG: log.info("My son "+ getSender()+" is done...");
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
                    if (activeQueryCheckers.get(deadPath) == null) return;
                    QCStatus deadStatus = ((QCMonitor)activeQueryCheckers.get(deadPath)).status;

                    /* If the queryChecker died in critial work, the query must be retried.
                     * Make the other queryCheckers (for the same query) be terminated too. */
                    if ( deadStatus == QCStatus.CRITICAL) {
                        DSQuery.QueryId qId = new DSQuery.QueryId(deadPath);
                        mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                                new DSEndQuery(qId), false), getSelf());
                        mediator.tell(new DistributedPubSubMediator.Send("/user/CLUSTERMANAGER"+ qId.getHost(),
                                new DSRetryQuery(qId), false), getSelf());
                    }
                    /* If the queryChecker died before having seen the query, it must be recreated. */
                    else if (deadStatus == QCStatus.WAITING) {
                        log.info("Recreating QueryChecker...");
                        ActorRef child = getContext().actorOf(DSQueryChecker.props(this.myView, this.myNode,
                                new DSQuery.QueryId(deadPath)), deadPath);
                        getContext().watch(child);
                        activeQueryCheckers.put(deadPath, new QCMonitor(child));
                    }
                    else
                     /* The queryChecker died after having seen the query. It can be left dead. */
                        log.info("QueryChecker on "+ deadPath + " is DEAD in peace.");
                })
                .match(DSCreateQueryChecker.class, in -> {
                    /* Create a new queryChecker for the passed queryId.
                     * Note: The queryChecker holds a *copy* of the robot's view,
                     * so that check phase is protected from concurrent updates of the view,
                     * due to move requests. */
                    ActorRef child = getContext().actorOf(DSQueryChecker.props(
                            new DSGraphImpl(myView), this.myNode,
                            in.getQueryId()), in.getQueryId().getPath());
                    getContext().watch(child);
                    activeQueryCheckers.put(in.getQueryId().getPath(), new QCMonitor(child));

                    // Simulate QueryChecker's Death in WAITING.
                    Thread.sleep(500);
                    if (DSDataFacade.getInstance().shouldDieInWaiting()) {
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