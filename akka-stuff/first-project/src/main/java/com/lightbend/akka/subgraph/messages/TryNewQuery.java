package com.lightbend.akka.subgraph.messages;

import akka.actor.ActorRef;
import com.lightbend.akka.subgraph.data.Query;

public class TryNewQuery {
    public Query query;
    public ActorRef sender;
}
