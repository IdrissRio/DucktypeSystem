package it.uniud.ducktypesystem.distributed.data;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.uniud.ducktypesystem.distributed.impl.DSRobot;
import it.uniud.ducktypesystem.logger.DSAbstractLog;

import java.awt.*;
import java.util.ArrayList;

public class DSCluster {
    private DataFacade facade;
    private DSAbstractLog logger;
    private static DSCluster cluster = null;
    private ArrayList<ActorSystem> actorSystemArray;
    private ArrayList<ActorRef> robotMainActorArray;
    private Integer proc_number;
    private Integer portSeed;
    private Color greenForest= new Color(11,102,35);


    private void actorSystemInitialization(ArrayList<ActorSystem> actorSystemTmp, Config conf){
        actorSystemTmp.add(ActorSystem.create("ClusterSystem", conf));
        for (int i = 1; i < proc_number ; ++i)
            actorSystemTmp.add(ActorSystem.create("ClusterSystem",
                    ConfigFactory.parseString("akka.remote.netty.tcp.port="+(portSeed+i)).withFallback(conf)));
        showInformationMessage("AKKA: Every node is connected"); //FIXME: Supercazzola. Ma ci piace così.
    }

    private void robotMainActorInitialization(ArrayList<ActorSystem> actorSystemTmp) {
        int i = 0;
        for (String localNode : facade.getOccupied()) {
            DSGraph localView = facade.getMap().getViewFromNode(localNode);
            robotMainActorArray.add(actorSystemTmp.get(i).actorOf(DSRobot.props(localView, localNode), "ROBOT"));
            ++i;
        }
    }

    public static void akkaEnvironment(DataFacade facade){
        if(cluster==null)
            cluster=new DSCluster(facade);
    }

    public static DSCluster getInstance() {
        return cluster;
    }

    private DSCluster(DataFacade facade) {
        this.facade = facade;
        logger = facade.getLogger();
        proc_number = facade.getOccupied().size();
        actorSystemArray = new ArrayList<ActorSystem>(proc_number);
        robotMainActorArray = new ArrayList<ActorRef>(proc_number);
        final Config config = ConfigFactory.load("akka.conf");
        portSeed = 2551;
        actorSystemInitialization(actorSystemArray, config);
        robotMainActorInitialization(actorSystemArray);
    }

    public ArrayList<ActorRef> getRobotMainActorArray(){
        return robotMainActorArray;
    }

    public ArrayList<ActorSystem> getActorSystemArray(){
        return actorSystemArray;
    }


    private void showInformationMessage(String s){
        logger.log(s,greenForest);
    }
    private void showErrorMessage(String s) {
        logger.log(s, Color.RED);
    }
}
