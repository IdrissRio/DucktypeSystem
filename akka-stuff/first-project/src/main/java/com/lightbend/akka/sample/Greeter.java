package com.lightbend.akka.sample;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.lightbend.akka.sample.Printer.Greeting;

//#greeter-messages
public class Greeter extends AbstractActor {
//#greeter-messages
  static public Props props(String name, String message, ActorRef nextActor) {
    return Props.create(Greeter.class, () -> new Greeter(name, message, nextActor));
  }

  //#greeter-messages
  static public class WhoToGreet {
    public final ActorRef who;

    public WhoToGreet(ActorRef who) {
        this.who = who;
    }
  }

  static public class Greet {
    public Greet() {
    }
  }
  //#greeter-messages

  private final String name;
  private final String message;
  private final WhoToGreet nextActor;
  private String greetingmsg;

  public Greeter(String name, String message, ActorRef nextActor) {
    this.name = name;
    this.message = message;
    this.nextActor = new WhoToGreet(nextActor);
    this.greetingmsg = "I am " + name ;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Greeting.class, greeting -> {
          //#greeter-send-message
          nextActor.who.tell(new Greeting(greeting.message + " - " + greetingmsg), getSelf());
          //#greeter-send-message
        })
        .build();
  }
//#greeter-messages
}
//#greeter-messages
