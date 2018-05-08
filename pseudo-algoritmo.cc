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


// Punto di accesso al cluster di Sensori
// disribuisce a tutti lo stesso comando
// listener di fallimenti
class Father {
  // -> Cluster : creazione - popola random
  // -> Cluster : move
  // -> Cluster : termina ricerca.
  // -> Printer : failure detected
};

// Riceve eventi dal cluster relativi alla query
// (è il pub sub stesso??)
class Query_Event_Distpatcher {
  // <- Cluster : found or fail : dobbiamo tenere conto che possono essercene due consecutivi.
  //              mandare al printer solo una volta.
  // -> Printer : query match
  // -> Printer : query fail
};

class Printer {
  // ...
};

class Command_loader {
  // -> Father : con quanti inizializzare.
  // -> Father : move a tutti
  // -> Father : aggiungi nuovo sensore
};


/*
 CHECK ME:
 PubSub coda di messaggi per ogni attore?
 Se riceve messaggio di terminazione, deve ancora smaltire la coda rimasta?
 Coda ordinata?

 Quando rimette in circolo query, al massimo la rilegge lui stesso una sola volta:
 garanzia che il proprio messaggio non prenda il posto sempre degli altri.
 */
