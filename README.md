# Implementazione di un algoritmo per il problema del sottografo distribuito.

## Sommario
 Il presente progetto fornisce un'implementazione del subgraph isomorphism problem applicato a grafi etichettati dei quali non si ha una conoscenza centralizzata.
Presenteremo le caratteristiche di un sistema di processi distribuiti in esecuzione su robot dislocati in un luogo sconosciuto che dovranno comunicare autonomamente per scoprire l'esistenza di un particolare percorso (query) sottoposto al cluster.
Verranno fornite le implementazioni dei processi, del sistema che simula l'ambiente fisico di esecuzione e dell'interfaccia grafica, oltre a una classe di file di test.

## Descrizione del probelma
Il subgraph isomorphism problem corrisponde a dover stabilire se un grafo contiene un sottografo isomorfo ad un altro dato in input.
Esistono diverse varianti dell'algoritmo che lo risolve, il quale è generalmente NP-completo.
Il sistema descritto nel presente progetto tratta la versione distribuita del problema:
con questo si intende che la conoscenza del grafo principale non è centralizzata, ma data come somma di conoscenze parziali e localmente certe; inoltre lo considereremo applicato a grafi con nodi etichettati e archi non etichettati, per cui il problema dell'isomorfismo si riconduce alla verifica dell'appartenenza dei nodi ed archi della query nel grafo principale.

Il modello fisico da cui il sistema trae le ipotesi principali, e che costituisce la sua concreta applicazione, vede il grafo principale come una mappa rappresentante un luogo sconosciuto che si mantiene costante: in questo vengono sparpagliati alcuni robot dotati di sensori e capaci di muoversi autonomamente, che acquisiranno conoscenza localizzata alla loro attuale posizione.
L'utente, da diversi client host connessi al sistema, può interrogare il cluster di sensori chiedendo se è presente un particolare percorso che connetta particolari nodi.
Per riuscire a rispondere, i robot devono autogestirsi scambiandosi informazioni, ma senza ricostruire alcuna conoscenza generale del grafo: infatti ognuno di questi ha una limitata capacità di memorizzazione, quindi non è possibile risolvere il problema in modo centralizzato.

La conoscenza distribuita è inoltre dinamica, perché i robot possono spostarsi, modificando la somma delle conoscenza parziali attualmente in memoria, e si deve anche supporre che questi possano fallire in qualunque momento.
I robot devono poter essere totalmente autonomi, dunque non è dato conoscere la loro posizione in ogni momento per riuscire a verificare la query.

Il presente progetto descrive le caratteristiche fisiche dei processi robot e dei client che si interfacciano al sistema, fornendone l'implementazione: il testing avviene in un framework sviluppato ah hoc che simula il contesto applicativo (i.e., la presenza di un luogo fisico nel quale distribuire i robot) in modo da monitorare e verificare la correttezza dell'esecuzione tramite un'interfaccia grafica.

## Ambiente di sviluppo e Framework
L’ambiente di sviluppo è stato scelto in modo da poter evidenziare solamente le caratteristiche più rilevanti del problema. Il sistema è stato implementato in Akka, un toolkit costituito da un insieme di librerie open source per la progettazione e creazione di sistemi distribuiti e concorrenti, con supporto alla scalabilità e alla fault tollerance, eseguibile su una JVM. Il toolkit è disponibile sia per il linguaggio imperativo usato (Java) che per il funzionale Scala e attualmente la versione più aggiornata è la 2.5.4. 
Akka si basa sul modello ad attori: un’astrazione utilizzata per eﬀettuare analisi sulla concorrenza e per la progettazione ad alto livello di sistemi distribuiti. Lo scopo del modello è quello di risolvere le questioni relative alla concorrenza e alla memoria condivisa, eliminandola completamente e sgravando dunque il programmatore da questi problemi. 


## Comunicazione indiretta: publish -  subscribe:

Le comunicazioni tra le varie componenti del sistema sfruttano principalmente il meccanismo di Distribuited Publish Subscribe in Cluster oﬀerto da Akka. Esso consente di comunicare con un insieme di attori che si dichiarano interessati a un determinato topic senza che il mittente conosca gli ActorRef dei destinatari. Per questi motivi risulta una funzionalità particolarmente adatta al sistema in questione perché garantisce una massima location transparency tra i diversi Actor System.
Gli attori membri di un cluster possono iscriversi a un path oppure a un subject; l’invio di un messaggio tramite il DistribuitedPubSubMediator di Akka può essere eseguito in modalità:

1) SendToAll / Publish, il messaggio viene ricevuto da tutti gli attori iscritti, eventualmente settando il parametro allButSelf che esclude il mittente;
2) Send, il messaggio viene ricevuto da un solo attore iscritto al cluster, eventualmente speciﬁcando la preferenza location aﬃnity.

# Autori
Anna Becchi - Idriss Riouak
