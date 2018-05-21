package it.uniud.ducktypesystem.distributed.data;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.uniud.ducktypesystem.controller.DSApplication;
import it.uniud.ducktypesystem.distributed.impl.DSClusterManagerActor;
import it.uniud.ducktypesystem.distributed.impl.DSRobot;
import it.uniud.ducktypesystem.distributed.message.DSCreateChild;
import it.uniud.ducktypesystem.distributed.message.DSMove;
import it.uniud.ducktypesystem.view.DSAbstractView;
import org.jboss.netty.channel.ChannelException;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DSCluster {
    private DataFacade facade;
    private DSAbstractView view;
    private DSApplication application;
    private static DSCluster cluster = null;
    private ArrayList<ActorSystem> actorSystemArray;
    private ArrayList<ActorRef> robotMainActorArray;
    private ActorRef clusterManager;
    private Integer procNumber;
    private int portSeed = 2551;
    private static final int maxRecovery = 5;
    private int actualRecovery = 0;
    private HashMap activeQueries = new HashMap();

    private void actorSystemInitialization(ArrayList<ActorSystem> actorSystemTmp, Config conf){
        try {
            for (int i = 0; i < procNumber; ++i)
                actorSystemTmp.add(ActorSystem.create("ClusterSystem",
                        ConfigFactory.parseString("akka.remote.netty.tcp.port=" + (portSeed + i)).withFallback(conf)));
            actorSystemTmp.add(ActorSystem.create("ClusterSystem",
                    ConfigFactory.parseString("akka.remote.netty.tcp.port=" + (portSeed + procNumber)).withFallback(conf)));
            view.showInformationMessage("AKKA: Every node is connected");
            robotMainActorInitialization(actorSystemArray);
        }catch(ChannelException e){
            exceptionFound();
            portSeed+=procNumber;
            for(int i=0;i<actorSystemTmp.size();++i) actorSystemTmp.remove(i);
            actorSystemInitialization(actorSystemTmp, conf);
        }
    }

    private void robotMainActorInitialization(ArrayList<ActorSystem> actorSystemTmp) {
        assert(facade.getOccupied().size() == (actorSystemTmp.size()-1) );
        int i = 0;
        for (String localNode : facade.getOccupied()) {
            DSGraph localView = facade.getMap().getViewFromNode(localNode);
            robotMainActorArray.add(actorSystemTmp.get(i).actorOf(DSRobot.props(localView, localNode, "Robot"+i), "ROBOT"));
            ++i;
        }
        this.clusterManager = actorSystemTmp.get(i).actorOf(DSClusterManagerActor.props(procNumber), "CLUSTERMANAGER");
        robotMainActorArray.add(clusterManager);
    }

    public static void akkaEnvironment(DataFacade facade, DSAbstractView view, DSApplication app){
        if(cluster==null)
            cluster=new DSCluster(facade, view, app);
    }

    public static DSCluster getInstance() {
        return cluster;
    }

    private DSCluster(DataFacade facade, DSAbstractView view, DSApplication app) {
        this.facade = facade;
        this.view = view;
        this.application = app;
        this.procNumber = facade.getOccupied().size();
        this.actorSystemArray = new ArrayList<ActorSystem>(procNumber + 1 );
        this.robotMainActorArray = new ArrayList<ActorRef>(procNumber + 1);

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

    public void startNewComputation(DSQuery query) {
        // TODO: set query version;
        query.setVersion("versioneProva");
        activeQueries.put(query.getVersion(), new DSQueryWrapper(query, null));
        DSCreateChild tmp = new DSCreateChild(facade.getNumSearchGroups(),
                 facade.getNumRobot() - 1, query.serializeToString(), query.getVersion(), query.getVersionNr());
        clusterManager.tell(tmp, ActorRef.noSender());
    }

    public DSAbstractView getView() {
        return this.view;
    }

    public void retryQuery(String version) {
        DSQueryWrapper wrapper = ((DSQueryWrapper) activeQueries.get(version));
        wrapper.getQuery().incrementVersionNr();
        DSCreateChild tmp = new DSCreateChild(facade.getNumSearchGroups(),
                facade.getNumRobot() - 1, wrapper.getStillToVerify(),
                version , wrapper.getQuery().getVersionNr());
        clusterManager.tell(tmp, ActorRef.noSender());
    }

    public void makeMove() {
        clusterManager.tell(new DSMove(), ActorRef.noSender());
    }

    public HashMap getActiveQueries() {
        return this.activeQueries;
    }

    public void endedQuery(String version, String stillToVerify) {
        ((DSQueryWrapper) activeQueries.get(version)).setStillToVerify(stillToVerify);
    }
}
