package it.uniud.ducktypesystem.distributed.controller;

import akka.actor.*;
import it.uniud.ducktypesystem.distributed.data.*;
import it.uniud.ducktypesystem.distributed.impl.DSQueryChecker;
import it.uniud.ducktypesystem.distributed.impl.DSRobot;
import it.uniud.ducktypesystem.distributed.message.DSCreateChild;
import it.uniud.ducktypesystem.errors.SystemError;
import it.uniud.ducktypesystem.logger.DSAbstractLog;
import it.uniud.ducktypesystem.view.DSAbstractView;
import it.uniud.ducktypesystem.view.DSView;

import java.awt.*;
import java.util.ArrayList;


public class DSInterface implements DSAbstractInterface {

    private DSGraph graph;
    private ArrayList<ActorSystem> actorSystemInstance;
    private ArrayList<ActorRef> robotMainActorInstance;
    private Integer proc_number;
    private Integer portSeed;
    private Color greenForest= new Color(11,102,35);
    private DSAbstractView view;


    public DSInterface(DSAbstractView view, DSQuery query) throws SystemError {
        this.view=view;
        graph = DataFacade.getInstance().getMap();
        actorSystemInstance=DSCluster.getInstance().getActorSystemArray();
        robotMainActorInstance=DSCluster.getInstance().getRobotMainActorArray();
        DSCreateChild tmp = new DSCreateChild(DataFacade.getInstance().getNumSearchGroups(),
                query.serializeToString());
        robotMainActorInstance.get(1).tell(tmp,robotMainActorInstance.get(1));
    }





}