package it.uniud.ducktypesystem.distributed.data;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.uniud.ducktypesystem.controller.DSApplication;
import it.uniud.ducktypesystem.distributed.impl.DSRobot;
import it.uniud.ducktypesystem.logger.DSAbstractLog;
import it.uniud.ducktypesystem.view.DSAbstractView;
import org.jboss.netty.channel.ChannelException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DSCluster {
    private DataFacade facade;
    private static DSCluster cluster = null;
    private ArrayList<ActorSystem> actorSystemArray;
    private ArrayList<ActorRef> robotMainActorArray;
    private ActorRef initzializer;
    private Integer proc_number;
    private Integer portSeed;
    private DSAbstractView view;
    private Integer maxRecovery;
    private Integer actualRecovery;
    private DSApplication application;


    private void actorSystemInitialization(ArrayList<ActorSystem> actorSystemTmp, Config conf){
        try {
            for (int i = 0; i < proc_number; ++i)
                actorSystemTmp.add(ActorSystem.create("ClusterSystem",
                        ConfigFactory.parseString("akka.remote.netty.tcp.port=" + (portSeed + i)).withFallback(conf)));
            view.showInformationMessage("AKKA: Every node is connected");
            robotMainActorInitialization(actorSystemArray);
        }catch(ChannelException e){
            exceptionFound();
            portSeed+=proc_number;
            for(int i=0;i<actorSystemTmp.size();++i) actorSystemTmp.remove(i);
            actorSystemInitialization(actorSystemTmp, conf);
        }
    }

    private void robotMainActorInitialization(ArrayList<ActorSystem> actorSystemTmp) {
        int i = 0;
        for (String localNode : facade.getOccupied()) {
            DSGraph localView = facade.getMap().getViewFromNode(localNode);
            robotMainActorArray.add(actorSystemTmp.get(i).actorOf(DSRobot.props(localView, localNode, "Robot"+i), "ROBOT"));
            ++i;
        }
    }

    public static void akkaEnvironment(DataFacade facade, DSAbstractView view, DSApplication app){
        if(cluster==null)
            cluster=new DSCluster(facade, view, app);
    }

    public static DSCluster getInstance() {
        return cluster;
    }

    private DSCluster(DataFacade facade, DSAbstractView view, DSApplication app) {
        this.application=app;
        this.maxRecovery=5; //Maybe this should be in the setting.
        this.portSeed = 2551;//Maybe also this.
        this.actualRecovery=0;
        this.view=view;
        this.facade = facade;
        proc_number = facade.getOccupied().size();
        actorSystemArray = new ArrayList<ActorSystem>(proc_number);
        robotMainActorArray = new ArrayList<ActorRef>(proc_number);

        final Config config = ConfigFactory.load("akka.conf");

        actorSystemInitialization(actorSystemArray, config);

    }
    private void exceptionFound(){
        if (this.maxRecovery<actualRecovery) {
            JOptionPane.showMessageDialog(view.getMainFrame(),
                    "The error recovery procedure failed.\n" +
                            " The system is corrupt. Self-destruction activated!",
                    "Adios !",
                    JOptionPane.ERROR_MESSAGE);
            application.exit();
        }
        if(actualRecovery==0)
            view.showErrorMessage("AKKA: Error in cluster initialization. Starting recovery mode.");
        ++actualRecovery;
    }
    public ArrayList<ActorRef> getRobotMainActorArray(){
        return robotMainActorArray;
    }

    public ArrayList<ActorSystem> getActorSystemArray(){
        return actorSystemArray;
    }


}
