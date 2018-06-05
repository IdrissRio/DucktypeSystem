package it.uniud.ducktypesystem.distributed.system;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.uniud.ducktypesystem.controller.DSApplication;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import it.uniud.ducktypesystem.distributed.data.DSQueryResult;
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

/**
 * DSCluster:
 * Creates the ActorSystems for the cluster of robots and simulates the connection of new hosts.
 * Simulates the segregation between hosts:
 * dispatches the requests from the unique DSView on the correct host's ActorSystem,
 * receives results from the hosts' ActorSystems and informs the view.
 */
public class DSCluster {
    private DSDataFacade facade;
    private DSAbstractView view;
    private DSApplication application;
    private final Config config = ConfigFactory.load("akka.conf");
    private int portSeed = 2551;
    private int actualRecovery = 0;
    private static final int maxRecovery = 5;
    private static DSCluster clusters = null;

    private int numHost = 0;
    private ArrayList<ActorSystem> actorSystemArray;
    private ArrayList<ActorRef> clusterInterfaceArray;
    private ArrayList<HashMap> activeQueries = new ArrayList<>();
    private Integer numRobots;

    /** Accessing to the DSCluster. */
    public static void akkaEnvironment(DSDataFacade facade, DSAbstractView view, DSApplication app){
        if(clusters==null)
            clusters=new DSCluster(facade, view, app);
    }
    public static DSCluster getInstance() {
       return clusters;
    }
    private DSCluster(DSDataFacade facade, DSAbstractView view, DSApplication app) {
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

    /** Initialization methods and recovery mode handling. */
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
        for (int i = numRobots; i-- > 0; )
            actorSystemArray.get(i).actorOf(DSRobot.props(i), "ROBOT");
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

    /** Simulates the connection of a new Host, creating its ActorSystem with a new clusterInterfaceActor */
    public int connectNewHost() {
        // Add a new ActorSystem for the new host.
        this.actorSystemArray.add(ActorSystem.create("ClusterSystem",
                ConfigFactory.parseString("akka.remote.netty.tcp.port=" + (portSeed + numRobots + numHost)).withFallback(config)));
        // Add the clusterInterface Actor for the new host.
        this.clusterInterfaceArray.add(actorSystemArray.get(numRobots + numHost)
                .actorOf(DSClusterInterfaceActor.props(numHost, numRobots), "CLUSTERMANAGER"+numHost));
        this.activeQueries.add(new HashMap());
        this.view.showInformationMessage("Host < " +numHost+" > connected");
        return this.numHost++;
    }


    /** Handling queries methods.*/

    /* Obtain a name for the query which is unique between the ones submitted from the same host,
     * and then make the clusterInterfaceActor of the right host start the new computation. */
    public String startNewComputation(int host, DSQuery query) {
        if (host >= numHost) return null;
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
    /* Retry query from the last obtained result. */
    public void retryQuery(DSQuery.QueryId id) {
        int host = id.getHost();
        String version = id.getVersion();
        if (host >= numHost) return;
        HashMap hostQueries = activeQueries.get(host);
        DSQueryResult qres = ((DSQueryResult) hostQueries.get(version));
        qres.getQuery().incrementAttemptNr();
        DSStartQueryCheck tmp = new DSStartQueryCheck(qres.getStillToVerify(),
                qres.getQuery().getId(), facade.getNumRobot() - 1);
        clusterInterfaceArray.get(host).tell(tmp, ActorRef.noSender());
    }
    /* Update the activeQueryResult information. */
    public void endedQuery(DSQuery.QueryId id, String stillToVerify, DSQuery.QueryStatus status) {
        int host = id.getHost();
        String version = id.getVersion();
        ((DSQueryResult) activeQueries.get(host).get(version)).setStatus(status);
        if (status == DSQuery.QueryStatus.DONTKNOW)
            ((DSQueryResult) activeQueries.get(host).get(version)).setStillToVerify(stillToVerify);
    }
    public void makeMove(int host) {
        clusterInterfaceArray.get(host).tell(new DSMove(), ActorRef.noSender());
    }

    /** These methods are invoked from the view. */
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


    public int getNumHost(){
        return numHost;
    }
    public DSAbstractView getView() {
        return this.view;
    }
    public HashMap getActiveQueries(int host) {
        if (host >= numHost) return null;
        return this.activeQueries.get(host);
    }
}
