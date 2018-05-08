
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
  Graph(int* nodes);
  Graph(int node);
  // ...

  int* get_nodes();
  int* adj_nodes(i);
  void add_node(int i);
  void remove_node(int i);

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
    bool new_hypothesis = false;
    for (auto i : q.graph.nodes()) {
      // QUERY TUTTA ETICHETTATA.
      // ricerca ordinata O(log k) con k = max arietà adj
      if (binary_search(i, my_graph.get_nodes(), j)) {
          // il nodo della query in pos i è presente nella conoscenza del sensore in pos j
          // controlla se lista di adiacenza è coerente.
          //
        if (subset(q.graph.adj_nodes(i), my_graph.adj_nodes(j))) {
          // so tutto di quel nodo: rimuovilo dalla query.
          new_query.graph.remove_node(i);
          new_hypothesis = true;
        }
        else {
          // viene richiesta una lista di adiacenza più grande della conosciuta.
          // se ho la certezza di sapere tutto di quel nodo (se ci sono sopra)
          // segnalo inconsistenza.
          if (i == my_node) {
            throw_query_failed();
            return false;
          }
        }
      } // nodo non presente o non abbastanza info
    }
    return new_hypothesis;
  }

  bool check_inv() {
    binary_search(my_node, my_graph.get_nodes());
    // ...
  }

public:
  Sensore(int node) {
    my_node = node;
    my_grap = new Graph(node);
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
  // popola random grafo con sensori
  // move a tutti
  // -> Printer : failure detected
};

// Riceve eventi dal cluster relativi alla query
// (è il pub sub stesso??)
class Query_Event_Distpatcher {
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
