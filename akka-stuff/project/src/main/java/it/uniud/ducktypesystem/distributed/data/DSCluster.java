package it.uniud.ducktypesystem.distributed.data;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.uniud.ducktypesystem.controller.DSApplication;
import it.uniud.ducktypesystem.distributed.impl.DSClusterInterfaceActor;
import it.uniud.ducktypesystem.distributed.impl.DSRobot;
import it.uniud.ducktypesystem.distributed.messages.DSEndQuery;
import it.uniud.ducktypesystem.distributed.messages.DSMove;
import it.uniud.ducktypesystem.distributed.messages.DSStartQueryCheck;
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
    private ArrayList<ActorRef> clusterInterfaceArray;
    private Integer numRobots;
    private int portSeed = 2551;
    private static final int maxRecovery = 5;
    private int actualRecovery = 0;
    private ArrayList<HashMap> activeQueries = new ArrayList<>();
    private int numHost = 0;

    private void actorSystemInitialization() {
        try {
            // Create Robot's ActorSystem.
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
        for (int i = numRobots; i-- > 0; )
            actorSystemArray.get(i).actorOf(DSRobot.props(i), "ROBOT");
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

        actorSystemInitialization();

        // Connect First Host.
        this.clusterInterfaceArray = new ArrayList<ActorRef>(1);
        connectNewHost();
    }

    public int connectNewHost() {
        // Add a new ActorSystem for the new host.
        this.actorSystemArray.add(ActorSystem.create("ClusterSystem",
                ConfigFactory.parseString("akka.remote.netty.tcp.port=" + (portSeed + numRobots + numHost)).withFallback(config)));
        // Add the clusterInterface Actor for the new host.
        this.clusterInterfaceArray.add(actorSystemArray.get(numRobots + numHost)
                .actorOf(DSClusterInterfaceActor.props(numHost, numRobots), "CLUSTERMANAGER"+numHost));
        this.activeQueries.add(new HashMap());
        return this.numHost++;
    }

    public void disconnectHost(int i) {
        // FIXME: how to kill ActorSystem?
        this.activeQueries.get(i).clear();
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

    public String startNewComputation(int host, DSQuery query) {
        if (host >= numHost) return null; // FIXME
        HashMap hostQueries = activeQueries.get(host);
        // If there is already a query with the same name, change its name.
        if (hostQueries.get(query.getVersion()) != null) {
            int i = 0; String attemptName = query.getName();
            while (hostQueries.get(query.getVersion()) != null) {
                ++i; query.setName(attemptName + i);
            }
        }
        hostQueries.put(query.getVersion(), new DSQueryResult(query, query.serializeToString()));
        DSStartQueryCheck tmp = new DSStartQueryCheck(query.serializeToString(),
                query.getId(), facade.getNumRobot() - 1);
        clusterInterfaceArray.get(host).tell(tmp, ActorRef.noSender());
        return query.getVersion();
    }

    public DSAbstractView getView() {
        return this.view;
    }

    public void retryQuery(DSQuery.QueryId id) {
        int host = id.getHost();
        String version = id.getVersion();
        if (host >= numHost) return; // FIXME
        HashMap hostQueries = activeQueries.get(host);
        DSQueryResult qres = ((DSQueryResult) hostQueries.get(version));
        qres.getQuery().incrementAttemptNr();
        DSStartQueryCheck tmp = new DSStartQueryCheck(qres.getStillToVerify(),
                qres.getQuery().getId(), facade.getNumRobot() - 1);
        clusterInterfaceArray.get(host).tell(tmp, ActorRef.noSender());
    }

    public void makeMove(int host) {
        clusterInterfaceArray.get(host).tell(new DSMove(), ActorRef.noSender());
    }

    public HashMap getActiveQueries(int host) {
        if (host >= numHost) return null; // FIXME
        return this.activeQueries.get(host);
    }

    public void endedQuery(DSQuery.QueryId id, String stillToVerify, DSQuery.QueryStatus status) {
        int host = id.getHost();
        String version = id.getVersion();
        ((DSQueryResult) activeQueries.get(host).get(version)).setStatus(status);
        if (status == DSQuery.QueryStatus.DONTKNOW)
            ((DSQueryResult) activeQueries.get(host).get(version)).setStillToVerify(stillToVerify);
    }

    public void killQuery(DSQuery.QueryId id) {
        int host = id.getHost();
        String version = id.getVersion();
        temporaryQueryStop(id);
        DSCluster.getInstance().getActiveQueries(host).remove(version);
    }

    public void temporaryQueryStop(DSQuery.QueryId id){
        int host = id.getHost();
        DSEndQuery endQuery = new DSEndQuery(id);
        clusterInterfaceArray.get(host).tell(endQuery, ActorRef.noSender());
    }
}
