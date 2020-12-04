SokoBOT will be a Java implementation of a solver for the puzzle game Sokoban, a classic toy problem of AI, providing
a simplified model for real-life problems in the fields of robotics and logistics. 

As of now, the problem modeling, the logic of the gameplay itself and the GUI that will show the result of the search are almost completely implemented.
The class structure of the solver, as well as some of the algorithms I was planning to implement, are ready and tested on rather simple levels of the game. 

To get the optimal box-goal matching required by A* heuristic function I used code from this repo: https://github.com/aalmi/HungarianAlgorithm
distributed under MIT license. The entirety of the copyright notice is available in the file SokobanToolkit.java, in which the code was used.



AREA DI SERVIZIO
Fix necessari:
- quando si espande per mosse, il numero di spinte finali non risulta corretto
- stranezze nella creazione della matrice di byte usata per hashare gli stati
