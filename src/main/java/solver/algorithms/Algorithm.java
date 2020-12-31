package solver.algorithms;

import game.GameBoard;
import solver.Node;
import solver.configuration.Strategy;

/*
Abstract algorithm. The class provides a static method to get an instance of algorithm by showing a Strategy enum.
This way it's possible to use Strategy enum values to decouple clients of the algorithms from the algorithms:
they don't really know or care which algorithms are available and how they operate, they just get a Strategy from the UI
and flip it to getInstance, then they use the launch(game) method on it
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
