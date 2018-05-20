package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DataFacade;
import it.uniud.ducktypesystem.distributed.message.*;

import java.util.ArrayList;

public class DSRobot extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

    // private ActorRef supervisor;
    private String myName;
    private DSGraph myView;
    private String myNode;
    private String lastStep;

    public DSRobot(DSGraph view, String node, String name) {
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        this.myName = name;
        this.myView = view;
        this.myNode = node;
        this.lastStep = null;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DSTryNewQuery.class, x->{

                })
                .match(DSMove.class, x -> {
                    lastStep = myNode;
                    myNode = myView.obtainNewView(myNode, lastStep);
                    /* FIXME: This is just for debugging purposes:
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
                /***********************************/
                /*.match(String.class, x -> {
                    log.info("From: {}   ----- To:"+myView.getNodes()+myNode, x);
                    boolean localAffinity = false;
                    mediator.tell(new DistributedPubSubMediator.Remove("/user/ROBOT"),getSelf());
                    Thread.sleep(1000);
                    mediator.tell(new DistributedPubSubMediator.CountSubscribers("/user/ROBOT"), getSelf());
                    mediator.tell(new DistributedPubSubMediator.Send("/user/ROBOT", myNode, localAffinity), getSelf());
                })*/
                .match(DSCreateChild.class, in -> {
                    context().actorOf(DSQueryChecker.props(this.myView, this.myNode, in.getVersion()),
                            in.getVersion());
                    log.info("Figli creati:" + myName);
                    // Thread.sleep(1000);
                    // mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT/prova", new String("benvenuti figli miei"), false), getSelf());
                })
                .build();
    }
    static public Props props(DSGraph view, String node, String name) {
        return Props.create(DSRobot.class, () -> new DSRobot(view, node,name));
    }
}