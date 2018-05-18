package it.uniud.ducktypesystem.distributed.data;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.uniud.ducktypesystem.distributed.impl.DSRobot;
import it.uniud.ducktypesystem.logger.DSAbstractLog;

import java.awt.*;

public class DSCluster {
    private DSAbstractLog logger;
    private static DSCluster cluster = null;
    private ActorSystem actorSystemArray[];
    private ActorRef robotMainActorArray[];
    private Integer proc_number;
    private Integer portSeed;
    private Color greenForest= new Color(11,102,35);


    private void actorSystemInitialization(ActorSystem[] actorSystemTmp, Config conf){
        actorSystemTmp[0]=ActorSystem.create("ClusterSystem", conf);
        for (int i =1;i<actorSystemTmp.length;++i)
            actorSystemTmp[i]= ActorSystem.create("ClusterSystem",
                    ConfigFactory.parseString("akka.remote.netty.tcp.port="+(portSeed+i)).withFallback(conf));

        showInformationMessage("AKKA: Every node is connected"); //Supercazzola. Ma ci piace così.
    }

    private void robotMainActorInitialization(ActorSystem[] actorSystemTmp){
        for (int i=0;i<actorSystemTmp.length;++i)
            robotMainActorArray[i]=actorSystemTmp[i].actorOf(DSRobot.props(null,"Robot"+i),"ROBOT");
    }
public static void akkaEnvironment(DataFacade facade){
        if(cluster==null)
            cluster=new DSCluster(facade);
}

    public static DSCluster getInstance() {
        return cluster;
    }

    private DSCluster(DataFacade facade) {
        logger = facade.getLogger();
        proc_number = facade.getOccupied().size();
        actorSystemArray = new ActorSystem[proc_number];
        robotMainActorArray = new ActorRef[proc_number];
        final Config config = ConfigFactory.load("akka.conf");
        portSeed = 2551;
        actorSystemInitialization(actorSystemArray, config);
        robotMainActorInitialization(actorSystemArray);
    }

    public ActorRef[] getRobotMainActorArray(){
        return robotMainActorArray;
    }

    public ActorSystem[] getActorSystemArray(){
        return actorSystemArray;
    }


    private void showInformationMessage(String s){
        logger.log(s,greenForest);
    }
    private void showErrorMessage(String s) {
        logger.log(s, Color.RED);
    }
}
