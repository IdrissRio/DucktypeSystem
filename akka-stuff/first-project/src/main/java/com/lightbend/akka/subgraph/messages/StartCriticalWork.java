package com.lightbend.akka.subgraph.messages;

import akka.actor.ActorRef;

public class StartCriticalWork {
    private ActorRef askQueryTo;

    public StartCriticalWork(ActorRef askQueryTo) {
        this.askQueryTo = askQueryTo;
    }

    public ActorRef getAskQueryTo() {
        return askQueryTo;
    }

    public void setAskQueryTo(ActorRef askQueryTo) {
        this.askQueryTo = askQueryTo;
    }
}
