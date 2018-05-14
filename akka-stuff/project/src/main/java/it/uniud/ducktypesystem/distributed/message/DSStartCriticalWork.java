package it.uniud.ducktypesystem.distributed.message;

import akka.actor.ActorRef;

public class DSStartCriticalWork {
    private ActorRef askQueryTo;

    public DSStartCriticalWork(ActorRef askQueryTo) {
        this.askQueryTo = askQueryTo;
    }

    public ActorRef getAskQueryTo() {
        return askQueryTo;
    }

    public void setAskQueryTo(ActorRef askQueryTo) {
        this.askQueryTo = askQueryTo;
    }
}
