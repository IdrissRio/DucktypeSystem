package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.uniud.ducktypesystem.distributed.controller.DSInterface;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import it.uniud.ducktypesystem.distributed.data.DSQueryImpl;
import it.uniud.ducktypesystem.distributed.message.*;

public class DSRobot extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

    // private ActorRef supervisor;
    private String myName;
    private DSGraph myView;
    private String myNode;
    private int memory;

    public DSRobot(DSGraph view, String node, String name) {
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        this.myName = name;
        this.myView = view;
        this.myNode = node;
        this.memory = 2;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DSTryNewQuery.class, x->{

                })
                .match(DSMove.class, x -> {
                    // FIXME: should access to the main graph through static facade.getInstance()?
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
                .match(DSInterface./*hello*/class, in -> {
                    boolean localAffinity = false;
                    mediator.tell(new DistributedPubSubMediator.Send("/user/ROBOT", myNode,
                            localAffinity), getSelf());
                })
                .match(DSCreateChild.class, in ->{
                    if(in.getFlag()==false && getSender()==getSelf() ) {
                        log.info("ORDINE: creare figli;" );
                        in.setFlag(true);
                        mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT", in, true), getSelf());
                        log.info("Figli creati:" + myName);
                        context().actorOf(DSQueryChecker.props(this.myView, this.myNode, "This is my version"),
                                "prova");
                        Thread.sleep(2000);
                        // PRIMA SEND:
                        DSQuery q = new DSQueryImpl(in.getQuery());
                        mediator.tell(new DistributedPubSubMediator.Send("/user/ROBOT/prova",
                                q, false), getSelf());
                        log.info(myNode + " ho fatto la prima send di "+q.toString());
                    }
                    else {
                        context().actorOf(DSQueryChecker.props(this.myView, this.myNode, "This is my version"),
                                "prova");
                        log.info("Figli creati:" + myName);
                        Thread.sleep(1000);
                        // mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT/prova", new String("benvenuti figli miei"), false), getSelf());
                    }
                })

                // FIXME: .match(Subscibe.class, x -> { subscribe() }
                // FIXME: .match(EndTimerAck.class, x -> { create new cluster send the x.version currentQuery })
                .build();
    }
    static public Props props(DSGraph view, String node, String name) {
        return Props.create(DSRobot.class, () -> new DSRobot(view, node,name));
    }
}