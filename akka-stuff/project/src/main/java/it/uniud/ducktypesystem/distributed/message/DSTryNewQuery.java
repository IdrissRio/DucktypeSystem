package it.uniud.ducktypesystem.distributed.message;

import akka.actor.ActorRef;
import it.uniud.ducktypesystem.distributed.data.DSQuery;

import java.io.Serializable;


public class DSTryNewQuery implements Serializable {
    public DSQuery query;
    public ActorRef sender;
}
