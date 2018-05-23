package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import it.uniud.ducktypesystem.distributed.data.DataFacade;
import it.uniud.ducktypesystem.distributed.message.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

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

    // private ActorRef supervisor;
    private String myName;
    private DSGraph myView;
    private String myNode;
    private String lastStep;
    private HashMap activeQueryCheckers;

    public DSRobot(DSGraph view, String node, String name) {
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        this.myName = name;
        this.myView = view;
        this.myNode = node;
        this.lastStep = null;
        this.activeQueryCheckers = new HashMap();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DSMove.class, x -> {
                    String tmp = myNode;
                    myNode = myView.obtainNewView(myNode, lastStep);
                    lastStep = tmp;
                    /* Note: This is just for debugging purposes:
                     * We DON'T claim to know the exact position of every robot:
                     * in a real distributed context this static access would be illegal,
                     * but we do it here just to improve the visualization and to ease the debugging phase.
                     */
                    ArrayList<String> occupied = DataFacade.getInstance().getOccupied();
                    for (int i = occupied.size(); i-- > 0; )
                        if (occupied.get(i).equals(lastStep)) {
                            occupied.set(i, myNode);
                            break;
                        }
                })
                .match(DSStartCriticalWork.class, msg -> {
                    // log.info("Un mio figlio "+ getSender() + "è in critical work...");
                    ((QCMonitor) activeQueryCheckers.get(msg.getQueryId().getPath())).status = QCStatus.CRITICAL;
                })
                .match(DSEndCriticalWork.class, msg -> {
                    // log.info("Un mio figlio "+ getSender()+" ha finito la critical work...");
                    if (activeQueryCheckers.get(msg.getQueryId().getPath()) != null)
                        ((QCMonitor) activeQueryCheckers.get(msg.getQueryId().getPath())).status = QCStatus.DONE;
                })
                .match(DSEndQuery.class, msg -> {
                    ActorRef qChecker = ((QCMonitor) activeQueryCheckers.get(msg.getQueryId().getPath())).queryChecker;
                    getContext().stop(qChecker);
                    activeQueryCheckers.remove(msg.getQueryId().getPath());
                })
                .match(Terminated.class, x -> {
                    String deadPath = x.actor().path().name();
                    if (activeQueryCheckers.get(deadPath) != null
                            && ((QCMonitor)activeQueryCheckers.get(deadPath)).status == QCStatus.CRITICAL) {
                        log.info(deadPath + " is DEAD during CRITICAL WORK!!!!");
                        DSQuery.QueryId qId = new DSQuery.QueryId(deadPath);
                        mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT",
                                new DSEndQuery(qId), false), getSelf());
                        mediator.tell(new DistributedPubSubMediator.Send("/user/CLUSTERMANAGER"+ qId.getHost(),
                                new DSRetryQuery(qId), false), getSelf());
                    }
                    else if (activeQueryCheckers.get(deadPath) != null
                            && ((QCMonitor)activeQueryCheckers.get(deadPath)).status == QCStatus.WAITING) {
                        log.info(deadPath + " is DEAD in WAITING!!!!");
                        // Recreate it:
                        ActorRef child = getContext().actorOf(DSQueryChecker.props(this.myView, this.myNode,
                                new DSQuery.QueryId(deadPath)), deadPath);
                        getContext().watch(child);
                        activeQueryCheckers.put(deadPath, new QCMonitor(child));
                    }
                    else
                        log.info(deadPath+" is DEAD in peace.");
                })
                .match(DSCreateQueryChecker.class, in -> {
                    ActorRef child = getContext().actorOf(DSQueryChecker.props(this.myView, this.myNode,
                            in.getQueryId()), in.getQueryId().getPath());
                    getContext().watch(child);
                    activeQueryCheckers.put(in.getQueryId().getPath(), new QCMonitor(child));
                    Thread.sleep(500);

                    // Simulate death of a QueryChecker in WAITING.
                    boolean shouldIKill = (ThreadLocalRandom.current().nextInt(0, 10) == -1);
                    if (shouldIKill) { log.info("I'M KILLING MY SON"); child.tell("Die!", getSelf()); }
                })
                .build();
    }
    static public Props props(DSGraph view, String node, String name) {
        return Props.create(DSRobot.class, () -> new DSRobot(view, node,name));
    }
}