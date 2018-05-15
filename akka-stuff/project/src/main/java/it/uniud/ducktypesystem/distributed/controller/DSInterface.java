package it.uniud.ducktypesystem.distributed.controller;


import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.uniud.ducktypesystem.distributed.impl.DSRobot;
import it.uniud.ducktypesystem.logger.DSAbstractLog;
import org.graphstream.graph.Graph;

import java.awt.*;

public class DSInterface implements DSAbstractInterface {
    private Integer seedPortNumber;

    public DSInterface(DSAbstractLog logger, Graph graph, Graph query) {
        this(logger, graph, query, 5);
    }
    public class hello{
        public String msg;
        public hello(String x){
            msg=x;
        }
    }
    public DSInterface(DSAbstractLog logger, Graph graph, Graph query, Integer proc_number) {
        logger.log("Created new computation", Color.RED);
        final Config config = ConfigFactory.parseString(
                        "akka.actor.provider=cluster\n" +
                        "akka.remote.netty.tcp.port=2551\n" +
                        "akka.remote.netty.tcp.host=127.0.0.1\n" +
                        "akka.cluster.seed-nodes = [ \"akka.tcp://ClusterSystem@127.0.0.1:2551\"]\n");
        ActorSystem node1 = ActorSystem.create("ClusterSystem", config);
        ActorSystem node2 = ActorSystem.create("ClusterSystem",
                ConfigFactory.parseString("akka.remote.netty.tcp.port=2552").withFallback(config));
        ActorSystem node3 = ActorSystem.create("ClusterSystem",
                ConfigFactory.parseString("akka.remote.netty.tcp.port=2553").withFallback(config));
        ActorSystem node4 = ActorSystem.create("ClusterSystem",
                ConfigFactory.parseString("akka.remote.netty.tcp.port=2554").withFallback(config));

        ActorRef x = node1.actorOf(DSRobot.props("Nodo1"), "destination");

        //another node
        node2.actorOf(DSRobot.props("Nodo2"), "destination");
        //another node
        node3.actorOf(DSRobot.props("Nodo3"), "destination");
        //another node
        node4.actorOf(DSRobot.props("Nodo4"), "destination");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ActorRef sender = node1.actorOf(DSRobot.props("Sender"), "sen3456der");
// after a while the destinations are replicated
        sender.tell(new hello(sender.path().name()), sender);
    }

}