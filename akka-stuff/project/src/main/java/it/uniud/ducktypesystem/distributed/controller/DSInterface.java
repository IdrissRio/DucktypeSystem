package it.uniud.ducktypesystem.distributed.controller;

import akka.actor.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DataFacade;
import it.uniud.ducktypesystem.distributed.impl.DSRobot;
import it.uniud.ducktypesystem.logger.DSAbstractLog;
import org.graphstream.graph.Graph;

import java.awt.*;


public class DSInterface implements DSAbstractInterface {
    private DSAbstractLog logger;
    private DSGraph graph;
    private ActorSystem actorSystemArray[];
    private ActorRef robotMainActorArray[];
    private Integer proc_number;
    private Integer portSeed;
    private Color greenForest= new Color(11,102,35);

    public class hello {
        public String msg;

        public hello(String x) {
            msg = x;
        }
    }
    //Inizialization of each node of the cluster.
    private void actorSystemInitialization(ActorSystem[] actorSystemTmp, Config conf){
        actorSystemTmp[0]=ActorSystem.create("ClusterSystem", conf);
        for (int i =1;i<actorSystemTmp.length;++i)
            actorSystemTmp[i]= ActorSystem.create("ClusterSystem",
                    ConfigFactory.parseString("akka.remote.netty.tcp.port="+(portSeed+i)).withFallback(conf));

        showInformationMessage("AKKA: Every node is connected"); //Supercazzola. Ma ci piace così.
    }
    private void robotMainActorInitialization(ActorSystem[] actorSystemTmp){
        for (int i=0;i<actorSystemTmp.length;++i)
            robotMainActorArray[i]=actorSystemTmp[i].actorOf(DSRobot.props("Robot"+i),"ROBOT");
    }

    public DSInterface(DataFacade facade) {
        logger = facade.getLogger();
        graph = facade.getMap();
        proc_number = facade.getOccupied().size();
        actorSystemArray = new ActorSystem[proc_number];
        robotMainActorArray = new ActorRef[proc_number];
        final Config config = ConfigFactory.load("akka.conf");
        portSeed = 2551;
        actorSystemInitialization(actorSystemArray, config);
        robotMainActorInitialization(actorSystemArray);


        ActorRef sender = actorSystemArray[0].actorOf(DSRobot.props("Sender"), "sender");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
// after a while the destinations are replicated
        sender.tell(new hello(sender.path().name()), sender);
    }
    private void showInformationMessage(String s){
        logger.log(s,greenForest);
    }
    private void showErrorMessage(String s) {
        logger.log(s, Color.RED);
    }

}