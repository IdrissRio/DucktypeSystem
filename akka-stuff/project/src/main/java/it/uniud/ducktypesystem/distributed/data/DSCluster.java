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
    private final Config config = ConfigFactory.load("akka.conf");
    private static DSCluster clusters = null;
    private ArrayList<ActorSystem> actorSystemArray;
    private ArrayList<ActorRef> robotMainActorArray;
    private ArrayList<ActorRef> clusterManagerArray;
    private Integer numRobots;
    private int portSeed = 2551;
    private static final int maxRecovery = 5;
    private int actualRecovery = 0;
    private ArrayList<HashMap> activeQueries = new ArrayList<>();
    private int numHost = 0;

    private void actorSystemInitialization(){
        try {
            for (int i = 0; i < numRobots; ++i)
                actorSystemArray.add(ActorSystem.create("ClusterSystem",
                        ConfigFactory.parseString("akka.remote.netty.tcp.port=" + (portSeed + i)).withFallback(config)));
            view.showInformationMessage("AKKA: Every Robot is connected");
            robotMainActorInitialization();
        } catch (ChannelException e) {
            exceptionFound();
            portSeed += numRobots;
            for (int i = 0; i < actorSystemArray.size(); ++i) actorSystemArray.remove(i);
            actorSystemInitialization();
        }
    }

    private void robotMainActorInitialization() {
        // FIXME assert(facade.getOccupied().size() == (actorSystemArray.size()) );
        int i = 0;
        for (String localNode : facade.getOccupied()) {
            DSGraph localView = facade.getMap().getViewFromNode(localNode);
            robotMainActorArray.add(actorSystemArray.get(i)
                    .actorOf(DSRobot.props(localView, localNode, "Robot"+i), "ROBOT"));
            ++i;
        }
    }

    public static void akkaEnvironment(DataFacade facade, DSAbstractView view, DSApplication app){
        if(clusters==null)
            clusters=new DSCluster(facade, view, app);
    }

    public static DSCluster getInstance() {
       return clusters;
    }

    private DSCluster(DataFacade facade, DSAbstractView view, DSApplication app) {
        this.facade = facade;
        this.view = view;
        this.application = app;
        this.numRobots = facade.getOccupied().size();
        // Initialize capacity considering the first connected host.
        this.actorSystemArray = new ArrayList<ActorSystem>(numRobots + 1 );
        this.robotMainActorArray = new ArrayList<ActorRef>(numRobots);

        actorSystemInitialization();

        // Connect First Host.
        this.clusterManagerArray = new ArrayList<ActorRef>(1);
        connectNewHost();
    }

    public int connectNewHost() {
        actorSystemArray.add(ActorSystem.create("ClusterSystem",
                ConfigFactory.parseString("akka.remote.netty.tcp.port=" + (portSeed + numRobots + numHost)).withFallback(config)));
        this.clusterManagerArray.add(actorSystemArray.get(numRobots + numHost)
                .actorOf(DSClusterManagerActor.props(numHost, numRobots), "CLUSTERMANAGER"+numHost));
        this.activeQueries.add(new HashMap());
        return this.numHost++;
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

    public String startNewComputation(int host, DSQuery query) {
        if (host >= numHost) return null; // FIXME
        HashMap hostQueries = activeQueries.get(host);
        // If there is already a query with the same name, change its name.
        if (hostQueries.get(query.getVersion()) != null) {
            String attemptName = query.getVersion(); int i = 1;
            while (hostQueries.get(attemptName+"-"+i) != null) ++i;
            query.setVersion(attemptName+"-"+i);
        }
        hostQueries.put(query.getVersion(), new DSQueryWrapper(query, null));
        DSCreateChild tmp = new DSCreateChild(facade.getNumSearchGroups(),
                 facade.getNumRobot() - 1, query.serializeToString(),
                host, query.getVersion(), query.getVersionNr());
        clusterManagerArray.get(host).tell(tmp, ActorRef.noSender());
        return query.getVersion();
    }

    public DSAbstractView getView() {
        return this.view;
    }

    public void retryQuery(int host, String version) {
        if (host >= numHost) return; // FIXME
        HashMap hostQueries = activeQueries.get(host);
        DSQueryWrapper wrapper = ((DSQueryWrapper) hostQueries.get(version));
        wrapper.getQuery().incrementVersionNr();
        DSCreateChild tmp = new DSCreateChild(facade.getNumSearchGroups(),
                facade.getNumRobot() - 1, wrapper.getStillToVerify(),
                host, version , wrapper.getQuery().getVersionNr());
        clusterManagerArray.get(host).tell(tmp, ActorRef.noSender());
    }

    public void makeMove(int host) {
        clusterManagerArray.get(host).tell(new DSMove(), ActorRef.noSender());
    }

    public HashMap getActiveQueries(int host) {
        if (host >= numHost) return null; // FIXME
        return this.activeQueries.get(host);
    }

    public void endedQuery(int host, String version, String stillToVerify) {
        ((DSQueryWrapper) activeQueries.get(host).get(version)).setStillToVerify(stillToVerify);
    }
}
