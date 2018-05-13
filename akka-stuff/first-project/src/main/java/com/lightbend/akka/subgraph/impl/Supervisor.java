package com.lightbend.akka.subgraph.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.lightbend.akka.subgraph.messages.EndCriticalWork;
import com.lightbend.akka.subgraph.messages.StartCriticalWork;

public class Supervisor extends AbstractActor {
    public enum Flag {
        CRITICAL,
        NONCRITICAL
    }
    private Flag status;
    private ActorRef supervised;
    private ActorRef askTo;

    public Supervisor() {
        // FIXME askTo = Father ?
        // default askTo is Father who owns the whole query... ??
    }
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // FIXME .match(RegisterSupervised.class, x-> { this.supervised = sender }
                // FIXME .match(DeadSupervised.class (??) x-> { restart + if(CRITICAL) askTo.tell(new AskNewSend(); }
                .match(StartCriticalWork.class, x -> {
                    status = Flag.CRITICAL;
                    askTo = x.getAskQueryTo();
                })
                .match(EndCriticalWork.class, x -> {
                    status = Flag.NONCRITICAL;
                })
                .build();
    }
}
