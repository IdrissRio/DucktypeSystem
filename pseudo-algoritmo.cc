/******* Strutture dati: ********/

// Contiene il grafo: classe a cui si delega interamente l'accesso alla struttura centralizzata.
// vi accede per caricare nuovi nodi nel costruttore e nel metodo add_node.
class Graph {
private:
  // Array *ordinato* di nodi presenti.
  // nodes[i] = x
  int*  nodes;
  // Matrice di adiacenza ordinata
  // adj_matrix[i][j] = 1 <--> general_graph_adj_matrix[nodes[j],nodes[i]] = 1
  int** adj_matrix;
public:
  // Load from general_graph.
  Graph();
  Graph(int* nodes);
  Graph(int node);
  // ...

  int* get_nodes();
  int* adj_nodes(i);
  void add_node(int i);
  void remove_node(int i);
  void remove_adj(int i, int j); // Don't remove trailing nodes!

  // remove nodes whose adj info are empty.
  void shrink_redundancies();

  int choose_next(int current) {
    return random_in(adj_nodes(current));
  }

  bool is_empty();

  int* get_region();
};

// Incapsulamento per switch dei messaggi
class Query {
public:
  Graph graph;
  // nodi etichettati: da restituire se trovata.
  // int* hypotized nodes;
};

class Move {
};




/******* Ruoli: *****************/

class Sensore {
private:
  int my_node;
  Graph my_graph;

  void subscribe() {
    int* regions = my_graph.get_regions();
    // deve controllare di non essere già iscritto?
    subscribe(regions);
  }
  void publish(Query new_query) {
    int* regions = new_query.graph.get_regions();
    publish(regions, new_query);
  }

  // in caso di query trovata (se tutta etichettata) o di fallimento riscontrato,
  // blocco la ricerca per tutti.
  void publish_match_found(Query q) {
    // TODO: HOW??????
  }
  void throw_query_failed() {
    // TODO: HOW??????
  }

  // Questa è la parte centrale dell'algoritmo:
  bool check_and_reduce_query(Query q, Query new_query) {
    assert(!q.graph.redundant());
    bool new_hypothesis = false;
    for (auto i : q.graph.get_nodes()) {
      // QUERY TUTTA ETICHETTATA.
      // ricerca ordinata O(log k) con k = max arietà adj
      if (binary_search(i, my_graph.get_nodes(), j)) {
          // il nodo della query in pos i è presente nella conoscenza del sensore in pos j
          // controlla se lista di adiacenza è coerente.
        if (i == my_node && !subset(q.graph.adj_nodes(i), my_graph.adj_nodes(j))) {
          // viene richiesta una lista di adiacenza più grande della conosciuta.
          // se ho la certezza di sapere tutto di quel nodo (se ci sono sopra)
          // segnalo inconsistenza.
          throw_query_failed();
          return false;
        }
        // So qualcosa del nodo, rimuovo dalla query archi che conosco.
        for (auto k : q.graph.adj_nodes(i)) {
          if (binary_search(k, my_graph.graph.adj_nodes(i))) {
            new_query.graph.remove_adj(i, k);
            new_hypothesis = true;
          }
        }
      }
      // end if nodo presente
    }
    if (new_hypothesis)
      new_query.graph.shrink_redundancies();
    return new_hypothesis;
  }

  bool check_inv() {
    binary_search(my_node, my_graph.get_nodes());
    // ...
  }

public:
  Sensore(int node) {
    my_node = node;
    my_graph = new Graph(node);
    subscribe();
  }
  void onReceive(int message) {
    switch(message) {
    case Query query:
      Query new_query(query);
      if (!check_and_reduce_query(query, new_query))
        return;
      if (new_query.graph.is_empty()) {
        publish_match_found(new_query);
        return;
      }
      // here new_query holds a *new* hypotesis to be checked
      publish(new_query);
      return;
    case Move x:
      int next = choose_next();
      my_graph.add_node(next);
      subscribe();
    }
  }
};


/******************************************************/
// Ruoli schema messaggi:

class Sensor {
  // -> Cluster_X : register
  // <- Cluster_X : Try_query() : -> Sender : ACK ricezione
  //                          + -> Supervisor : Cache query + cluster_x
  //                          + elabora new query
  //                          + remove from cluster_x
  //                          + -> Supervisor : ACK fine lavoro critica            <------------------
  //                          + if (match_query)  -> Query_Event_Dispatcher : match!
  //                            elsif (failed_query) -> Query_Event_Dispatcher : failed!
  //                            else -> Cluster_X : do SEND(new_query)             <------------------
  //                                                while wait ACK / timeout
  // <- Father : move
};

class One_to_one_Supervisor {
  // <- Supervised : i'm dead : supervised.restart
  //                          + supervised.register_again
  //                          + if (flag == CRITICO) -> Cluster_X : SEND(cached_query)
  // <- Supervised : Cached_query + quale cluster : flag = CRITICO
  // <- Supervised : Ack : flag = NON_CRITICO
};

// Punto di accesso al cluster di Sensori
// disribuisce a tutti lo stesso comando.
class Father {
  // -> Cluster : popola grafo in posizioni random
  // -> Cluster : move
  // <- Loader : inizia nuova query:
  //             init k Cluster : per ognuno : -> Cluster_X SEND(query)
  // -> Query_Event_Dispathcer : termina ricerca : azzera ogni Cluster_X
};

// Riceve eventi dal cluster relativi alla query
class Query_Event_Dispatcher {
  // <- Cluster_X : MATCH / FAIL / DONTKNOW (= i'm empty)
  //              -> Father : termina ricerca.
  //              -> Printer : query MATCH / FAIL / DONTKNOW
};

class Printer {
  // A terminale:
  // Searching.....
  // MATCH
  // FAIL
  // DONTKNOW
};

class Command_loader {
  // Da terminale:
  // -> Father : con quanti inizializzare / decisone k
  // -> Father : move a tutti
  // -> Father : aggiungi nuovo sensore
};


/*
  Cose da dire: requisiti non funzionali:
  *****************
  SCALABILITÀ:
  Sistema P2P: i ruoli centralizzati sono solo ruoli che permettono
  la comunicazione all'intero cluster di una nuova query o che
  raccolgono informazioni relative alla riuscita o fallimento della stessa.
  NON viene ricostruita alcuna conoscenza centralizzata del grafo totale,
  ovvero al gestione della conoscenza è interamente distribuita.
  Questo rende il sistema scalabile e flessibile.
  Si può dinamicamente aggiungere un nuovo sensore o far spostare
  quelli presenti nella rete, senza dover riconfigurare nulla.
  L'acquisizione di nuova conoscenza però ha effetti limitati se avviene
  nel periodo in cui la query è stata già innescata: infatti la nuova
  conoscenza può essere sfruttata solo se acquisita dal sensore
  *prima* che la query che passa sequenzialmente tra i sensori attivi,
  abbia chiesto il suo contributo.
  Se un sensore acquisisce nuova conoscenza dopo aver elaborato la query,
  questa non tornerà nuovamente dallo stesso sensore, dunque non potrà utilizzare,
  per quella particolare ricerca la nuova conoscenza.
  L'effetto di questo è solo un'eventuale risposta DONTKNOW,
  senza rischiare di dare falsi MATCH o falsi FAIL della query.

  *****************
  LOAD BALANCING e COMPLESSITÀ del lavoro:
  Poiché la soluzione in ogni caso richiede l'esplorazione dei nodi
  in modo sequenziale, NON è una limitazione in termini di efficienza
  avere un solo nodo attivo alla volta. Ovvero NON possiamo comunque
  sfruttare il parallelismo di un sistema distribuito.
  Il percorso di esplorazione della query, progressivamente snellita,
  non influisce nella sua determinazione: una qualunque delle n! possibili
  permutazioni -- qualunque sia l'ordine in cui la query attraversa i nodi --
  trova la stessa soluzione, che richiede l'intervento SEQUENZIALE di
  tutti i nodi che possono dire qualcosa sulla query stessa.
  Dunque, dal punto di vista della correttezza dell'algoritmo,
  a meno di failure, possiamo far procedere una query sola alla volta.

  *****************
  FAILURE MODEL:
  Essendo tasks potenzialmente molto lunghi da affrontare,
  e comunque da gestirsi in modo sequenziale,
  siamo disposti ad aggiungere messaggi informativi per il recupero della query parziale,
  evitando di perdere il lavoro eseguito fino a quel punto.
  Aggiunta di - messaggi di ACK al mittente
              - sistema di caching della query attuale al supervisor
              - ridondanza della ricerca.
  Si garantisce che i casi non gestiti di fallimenti del sistema comunque,
  NON portano a falsi MATCH o falsi FAIL della query, al massimo il sistema
  risponde DONTKNOW e l'utente può manualmente reiniziare la query.

  Motivazioni:

  In caso di fallimenti della rete:
  Si può perdere il messaggio di Send al cluster_x con la query corrente.
  Il mittente aspetta l'ack del ricevente con un timeout.

  In caso di fallimenti dei sensori:
  la ricerca è "affected" (come cacchio si dice in italiano?)
  quando muore il sensore durante il suo periodo di attività,
  ovvero nel momento in cui ha la query attiva.
  Possiamo ridurre inizialmente la finestra temporale in cui può
  avvenire tale fallimento critico, copiando la query attiva
  nel supervisore one-to-one del processo, non appena questa
  è stata ricevuta dal processo stesso.
  In caso di fallimento, il supervisore che ha ricevuto la copia,
  ovvero è informato che il processo è in una fase critica del lavoro,
  si occuperà, oltre di fare il restart del processo e di reiscriverlo
  al cluster, di fare una nuova SEND della query al cluster.
  Se invece il processo fallisce in una fase *non* critica del lavoro,
  i.e. quando NON ha la query attiva, verrà semplicemente riattivato
  dal supervisore.

  Nonostante la finestra temporale in cui il fallimento critico può avvenire
  sia stata ridotta,
  possiamo ulteriormente proteggere il sistema facendo procedere
  un numero k costante (3 di default?) di query contemporaneamente,
  ognuna con il proprio cluster di processi.
  In questo modo si ottiene una forma di ridondanza: nel caso una
  query venga persa a causa di una fallimento critico,
  il suo cluster risulta semplicemente inattivo.
  Sperabilmente, almeno una delle k query parallele riesce ad
  esplorare tutto il cluster: di conseguenza, il messaggio di
  match_query / fail_query termina la ricerca delle altre versioni,
  sbloccando i cluster eventualmente rimasti inattivi.

  Criticità rimaste:
  - Rischio di doppio invio di Send per timeout expired (rete perde il messaggio di ack)
  - Query persa (morte del processo prima che il supervisor abbia il flag == CRITICO)
  In ogni caso, il comportamento del sistema è di ritornare un DONTKNOW quando invece
  si sarebbe stati in grado di avere un match o un fail certi della query.
  NON rischiamo di avere falsi match o falsi fail della query a causa di fallimenti del sistema.

  ***************
  NAMING E LOCATION TRANSPARENCY:
  Il sistema di Cluster e di Send svincola il sistema dalla conoscenza
  della posizione dei vari sensori.
 */
