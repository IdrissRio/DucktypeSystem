package it.uniud.ducktypesystem.distributed.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.uniud.ducktypesystem.distributed.controller.DSInterface;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DSGraphImpl;
import it.uniud.ducktypesystem.distributed.data.DSQuery;
import it.uniud.ducktypesystem.distributed.message.*;

public class DSRobot extends AbstractActor {
    private String myName;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    private ActorRef supervisor;

    private DSGraph myGraph;
    private String myNode;
    /* FIXME:
     * we need to save MORE than one current Query if it has to hold the current Query
     * until it it correctly forwarded. This could be a List<Query> indexed by version hash nr?
     */
    private DSQuery currentQuery;
    private enum QueryStatus {
        MATCH,
        FAIL,
        NEW,
        DONTKNOW
    }

    // Comunication methods
    private void subscribe() { /* TODO: */ }
    private void secureSend(DSQuery.Version version) { /* TODO:
     // FIXME : it can't wait inside onReceive!
     * Send to version cluster
     * timeout.tell(Start(version))
     */
    }
    private void publishMatch() { /* TODO: */ }
    private void publishFail() { /* TODO: */ }

    // Graph manage methods
    private QueryStatus checkAndReduce() {
        // FIXME: select right currentQuery from version?
        DSQuery newQuery = currentQuery.cloneQuery();

        assert(!newQuery.isRedundant());
        boolean newHypothesis = false;

        /* FIXME PSEUDOCODE:
        for (auto qN : currentQuery.getNodes()) {
            // Ordered search to improve efficiency
            if (binary_search(qN, myGraph.getNodes(), myN)) {
                if (myN.isEqual(myNode)
                  && !subset(currentQuery.adjNodes(n), myGraph.adjNodes(j))) {
                    return QueryStatus.FAIL;
                }
                // Remove verified edges
                for (auto qN2 : currentQuery.adjNodes(qN)) {
                    if (binary_search(qN2, myGraph.adjNodes(myN))) {
                        newQuery.removeEdge(qN, qN2);
                        newHypothesis = true;
                    }
                }
            }
        }
        */

        if (newHypothesis)
            newQuery.shrinkRedundancies();
        if (newQuery.isEmpty())
            return QueryStatus.MATCH;
        currentQuery = newQuery;
        return QueryStatus.NEW;
    }
    public DSRobot(){
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());

    }

    public DSRobot(/*DSAbstractGraph.Node myNode*/String msg) {
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        this.myNode = myNode;
        this.myGraph = new DSGraphImpl();
        myName=msg;
        // FIXME: this.supervisor = system.ActorOf(Supervisor.class) ?? è lui che lo deve creare?

    }



    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DSTryNewQuery.class, msg -> {
                    // FIXME: select correct currentQuery from version?
                    currentQuery = msg.query;
                    supervisor.tell(new DSStartCriticalWork(msg.sender), ActorRef.noSender());
                    msg.sender.tell(new DSAck(), ActorRef.noSender());
                    switch (checkAndReduce()) {
                        case FAIL:
                            publishFail(); break;
                        case MATCH:
                            publishMatch(); break;
                        case NEW:
                            secureSend(currentQuery.version); break;
                        default: // throw MyFaultProjectError ?
                    }
                    supervisor.tell(new DSEndCriticalWork(), ActorRef.noSender());
                })
                .match(DSMove.class, x -> {
                    // myGraph.addNode(myGraph.chooseNext(myNode));
                })
                .match(DSAskNewSend.class, x -> {
                    secureSend(x.version);
                })
                /***********************************/
                .match(String.class, x -> {
                    log.info("From: {}   ----- To:"+myName, x);
                    boolean localAffinity = false;
                    //mediator.tell(new DistributedPubSubMediator.Unsubscribe("destination",getSelf()),ActorRef.noSender());
                    mediator.tell(new DistributedPubSubMediator.Remove("/user/destination"),getSelf());
                    Thread.sleep(1000);
                    mediator.tell(new DistributedPubSubMediator.Send("/user/destination", myName,
                            localAffinity), getSelf());
                })
                .match(DSInterface.hello.class, in -> {
                    boolean localAffinity = false;
                    mediator.tell(new DistributedPubSubMediator.Send("/user/destination", myName,
                            localAffinity), getSelf());
                })

                // FIXME: .match(Subscibe.class, x -> { subscribe() }
                // FIXME: .match(EndTimerAck.class, x -> { create new cluster send the x.version currentQuery })
                .build();
    }
    static public Props props(String message) {
        return Props.create(DSRobot.class, () -> new DSRobot(message));
    }
}