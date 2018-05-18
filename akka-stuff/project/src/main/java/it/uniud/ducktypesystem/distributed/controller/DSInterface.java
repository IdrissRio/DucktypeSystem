package it.uniud.ducktypesystem.distributed.controller;

import akka.actor.*;
import it.uniud.ducktypesystem.distributed.data.DSCluster;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DataFacade;
import it.uniud.ducktypesystem.distributed.impl.DSRobot;
import it.uniud.ducktypesystem.logger.DSAbstractLog;
import it.uniud.ducktypesystem.view.DSAbstractView;
import it.uniud.ducktypesystem.view.DSView;

import java.awt.*;
import java.util.ArrayList;


public class DSInterface implements DSAbstractInterface {

    private DSAbstractLog logger;
    private DSGraph graph;
    private ArrayList<ActorSystem> actorSystemInstance;
    private ArrayList<ActorRef> robotMainActorInstance;
    private Integer proc_number;
    private Integer portSeed;
    private Color greenForest= new Color(11,102,35);
    private DSAbstractView view;

    public class hello {
        public String msg;

        public hello(String x) {
            msg = x;
        }
    }

    public DSInterface(DataFacade facade, DSAbstractView view) {
        this.view=view;
        logger = facade.getLogger();
        graph = facade.getMap();
        actorSystemInstance=DSCluster.getInstance().getActorSystemArray();
        robotMainActorInstance=DSCluster.getInstance().getRobotMainActorArray();
        ActorRef sender = actorSystemInstance.get(0).actorOf(DSRobot.props(null, "Sender"), "sender");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
// after a while the destinations are replicated
        sender.tell(new hello(sender.path().name()), sender);
    }



}