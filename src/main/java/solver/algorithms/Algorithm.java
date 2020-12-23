package solver.algorithms;

import game.GameBoard;
import solver.Node;
import solver.configuration.Strategy;

/*
Abstract algorithm. Provides a static method to get an instance of algorithm by showing a Strategy enum
*/
public abstract class Algorithm {

    public static Algorithm getInstance(Strategy strategy) {
        switch (strategy) {
            case BFS : {
                return new BFS();
            }
            case IDDFS : {
                return new IDDFS();
            }
            case IDASTAR : {
                return new IDAStar();
            }
            case ASTAR:  {
                return new VanillaAStar();
            }
            case GBFS : {
                return new GreedyBFS();
            }
        }
        return null;
    }

    abstract public Node launch (GameBoard game) throws CloneNotSupportedException;
}
