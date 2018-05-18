package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSub$;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.uniud.ducktypesystem.distributed.controller.DSInterface;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DSGraphImpl;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import it.uniud.ducktypesystem.distributed.message.*;

import java.awt.*;

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
                .match(DSInterface.hello.class, in -> {
                    boolean localAffinity = false;
                    mediator.tell(new DistributedPubSubMediator.Send("/user/ROBOT", myNode,
                            localAffinity), getSelf());
                })
                .match(DSCreateChild.class, in ->{
                    if(in.getFlag()==false && getSender()==getSelf() ) {
                        log.info("Io: " + myName +" Sono stato incaricato da Idriss il grande per aumentare la popolazione globale (del cluster)." );
                        in.setFlag(true);
                        mediator.tell(new DistributedPubSubMediator.SendToAll("/user/ROBOT", in, false), getSelf());

                    }
                    else
                        log.info("OK FACCIO UN FIGLIO, O ALMENTO CI PROVO: " + myName);
                })

                // FIXME: .match(Subscibe.class, x -> { subscribe() }
                // FIXME: .match(EndTimerAck.class, x -> { create new cluster send the x.version currentQuery })
                .build();
    }
    static public Props props(DSGraph view, String node, String name) {
        return Props.create(DSRobot.class, () -> new DSRobot(view, node,name));
    }
}