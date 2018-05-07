package com.lightbend.akka.sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.lightbend.akka.sample.Greeter.*;
import com.lightbend.akka.sample.Printer.Greeting;

import java.io.IOException;

public class AkkaQuickstart {
  public static void main(String[] args) {
    final ActorSystem system = ActorSystem.create("helloakka");
    try {
      //#create-actors
      final ActorRef printerActor = 
        system.actorOf(Printer.props(), "printerActor");
      final ActorRef firstGreeter =
        system.actorOf(Greeter.props("Firstolo","First message", printerActor), "firstGreeter");
      final ActorRef secondGreeter =
        system.actorOf(Greeter.props("Secondolo","Second message", firstGreeter), "secondGreeter");
      final ActorRef thirdGreeter =
        system.actorOf(Greeter.props("Thirdolo","Third message", secondGreeter), "thirdGreeter");
      //#create-actors

      //#main-send-messages
      thirdGreeter.tell(new Greeting("Inizia!"), ActorRef.noSender());

      //#main-send-messages

      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (IOException ioe) {
    } finally {
      system.terminate();
    }
  }
}
