SokoBOT sarà un risolutore implementato in Java per il puzzle game Sokoban, semplice gioco di logica
che fornisce un modello semplificato per problemi reali di logistica e di robotica.

Ad ora, sono quasi completamente implementati il modeling del problema, la logica del gioco in sé, la GUI che mostrerà il risultato della ricerca.
La struttura delle classi del solver in sé è pronta ed è stata testata su livelli molto semplici. Gli algoritmi attualmente implementati sono BFS e
IDDFS, a cui sono state applicate alcune ottimizzazioni rispetto alla loro implementazione "vanilla".

ToDo:
- iniziare a lavorare sulla deadlock detection
- implementare un modello di espansione dei nodi ottimizzato per "spinte" alle casse e non per mosse del giocatore
- implementare un algoritmo di ricerca informata, probabilmente IDA*
- iniziare a formalizzare la documentazione di progetto
