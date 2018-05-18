package it.uniud.ducktypesystem.distributed.controller;

import akka.actor.*;
import it.uniud.ducktypesystem.distributed.data.DSCluster;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DataFacade;
import it.uniud.ducktypesystem.distributed.impl.DSRobot;
import it.uniud.ducktypesystem.logger.DSAbstractLog;

import java.awt.*;


public class DSInterface implements DSAbstractInterface {

    private DSAbstractLog logger;
    private DSGraph graph;
    private ActorSystem actorSystemInstance[];
    private ActorRef robotMainActorInstance[];
    private Integer proc_number;
    private Integer portSeed;
    private Color greenForest= new Color(11,102,35);

    public class hello {
        public String msg;

        public hello(String x) {
            msg = x;
        }
    }

    public DSInterface(DataFacade facade) {
        logger = facade.getLogger();
        graph = facade.getMap();
        actorSystemInstance=DSCluster.getInstance().getActorSystemArray();
        robotMainActorInstance=DSCluster.getInstance().getRobotMainActorArray();
        ActorRef sender = actorSystemInstance[0].actorOf(DSRobot.props(null, "Sender"), "sender");
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