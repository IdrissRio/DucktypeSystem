package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import it.uniud.ducktypesystem.distributed.message.DSEndCriticalWork;
import it.uniud.ducktypesystem.distributed.message.DSStartCriticalWork;

public class DSSupervisor extends AbstractActor {
    public enum Flag {
        CRITICAL,
        NONCRITICAL
    }
    private Flag status;
    private ActorRef supervised;
    private ActorRef askTo;

    public DSSupervisor() {
        // FIXME askTo = Father ?
        // default askTo is Father who owns the whole query... ??
    }
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // FIXME .match(RegisterSupervised.class, x-> { this.supervised = sender }
                // FIXME .match(DeadSupervised.class (??) x-> { restart + if(CRITICAL) askTo.tell(new AskNewSend(); }
                .match(DSStartCriticalWork.class, x -> {
                    status = Flag.CRITICAL;
                    askTo = x.getAskQueryTo();
                })
                .match(DSEndCriticalWork.class, x -> {
                    status = Flag.NONCRITICAL;
                })
                .build();
    }
}
