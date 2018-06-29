# Implementazione di un algoritmo per il problema del sottografo distribuito.

## Il presente progetto fornisce un'implementazione del
subgraph isomorphism problem applicato a grafi
etichettati dei quali non si ha una conoscenza centralizzata.
Presenteremo le caratteristiche di un sistema di processi
distribuiti in esecuzione su robot dislocati in un
luogo sconosciuto che dovranno comunicare autonomamente
per scoprire l'esistenza di un particolare percorso
(query) sottoposto al cluster.
Verranno fornite le implementazioni dei
processi, del sistema che simula l'ambiente fisico
di esecuzione e dell'interfaccia grafica, oltre a una
classe di file di test.


Il subgraph isomorphism problem corrisponde a dover
stabilire se un grafo contiene un sottografo isomorfo ad
un altro dato in input.
Esistono diverse varianti dell'algoritmo che lo risolve, il quale è
generalmente NP-completo.
Il sistema descritto nel presente progetto
tratta la versione distribuita del problema:
con questo si intende che la conoscenza del grafo principale non
è centralizzata, ma data come somma di conoscenze parziali e
localmente certe; inoltre lo considereremo applicato
a grafi con nodi etichettati e archi non etichettati,
per cui il problema dell'isomorfismo si riconduce alla verifica
dell'appartenenza dei nodi ed archi della query nel grafo principale.

Il modello fisico da cui il sistema trae le ipotesi principali,
e che costituisce la sua concreta applicazione, vede il grafo
principale come una mappa rappresentante un luogo sconosciuto
che si mantiene costante:
in questo vengono sparpagliati alcuni robot
dotati di sensori e capaci di muoversi autonomamente, che acquisiranno
conoscenza localizzata alla loro attuale posizione.
L'utente, da diversi client host connessi al sistema, può
interrogare il cluster di sensori chiedendo se è
presente un particolare percorso che connetta particolari nodi.
Per riuscire a rispondere, i robot devono autogestirsi scambiandosi
informazioni, ma senza ricostruire alcuna conoscenza generale del grafo:
infatti ognuno di questi ha una limitata capacità di memorizzazione,
quindi non è possibile risolvere il problema in modo centralizzato.

La conoscenza distribuita è inoltre dinamica, perché i robot
possono spostarsi, modificando la somma delle conoscenza parziali
attualmente in memoria, e si deve anche supporre che questi possano
fallire in qualunque momento.
I robot devono poter essere totalmente autonomi, dunque non
è dato conoscere la loro posizione in ogni momento per
riuscire a verificare la query.

Il presente progetto descrive le caratteristiche fisiche dei
processi robot e dei client che si interfacciano al sistema,
fornendone l'implementazione: il testing
avviene in un framework sviluppato ah hoc
che simula il contesto applicativo (i.e., la presenza di un luogo
fisico nel quale distribuire i robot)
in modo da monitorare e verificare la correttezza dell'esecuzione
tramite un'interfaccia grafica.

# Autori
Anna Becchi - Idriss Riouak
