package solver.algorithms;

import game.GameBoard;
import solver.*;
import solver.configuration.Strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Logger;

/*
Abstract algorithm. The class provides a static method to get an instance of algorithm by showing a Strategy enum.
This way it's possible to use Strategy enum values to decouple clients of the algorithms from the algorithms:
they don't really know or care which algorithms are available and how they operate, they just get a Strategy from the UI
and flip it to getInstance, then they use the launch(game) method on it
*/
public abstract class Algorithm {
    static final Logger log = Logger.getLogger("Algorithm");

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


/*
    Functional interface with the label assignment method
    Used to abstract label assignment in the generic PQueue algorithm below
*/
    interface Labeler {
        public void assignLabel (InformedNode informedNode);
    }

/*
    This method can generalize any "Dijkstra-like" PQueue based search algorithm. It can be used for A*,
    for a best-first search as well as a uniform cost search.
    You just have to pass it a proper comparator to keep the PQueue ordered the way the algorithm requires
    and a label assigning method to update the label variable in the InformedNode after expansion.

    For A*, the label f(n) of a node n is equals to g(n) + h(n), where g(n) is the path cost and h(n) is the heuristic estimation.
    In a best-first search f(n) = h(n), whereas for a uniform cost search it would be f(n) = g(n).
*/
    protected Node launchPQueueSearch (GameBoard game, Comparator <InformedNode> c, Labeler l) throws CloneNotSupportedException {
        SokobanSolver.setLogLine("Top h(n) value: " + "\nFrontier size: 0" + "\nNumber of visited nodes: " + Transposer.getExaminedNodes());

        //comparing criteria for the PQueue ordering is passed in the Comparator variable
        PriorityQueue<InformedNode> frontier = new PriorityQueue<InformedNode>(c);

        //Inserting the root node in the queue, in the accounting structure and the transposition table
        InformedNode root = new InformedNode(game, new ArrayList<>(), null, SokobanToolkit.heuristicEstimate(game));
        frontier.add(root);
        Transposer.transpose(root);
        Transposer.saveLabel(root);

        //Main loop of the algorithm, we're only going to break it if we found a solution or if the frontier is empty,
        while (!frontier.isEmpty() && !SokobanSolver.isInterrupted()) {

            //We pop the node with the best heuristic estimate off the PQueue
            InformedNode examined = frontier.remove();
            //We remove the node we just popped from the structure that keeps the labels for the frontier.
            //If the heuristic chosen is consistent, then A* doesn't need to keep track of "closed" nodes:
            //we will be sure that whenever a node is extracted from the frontier, it will be extracted with the best value.
            Transposer.removeLabel(examined);

            //SOLUTION
            if (examined.isGoal()) {
                return examined;
            }

            //expanding the current node and adding the resulting nodes to the frontier Pqueue
            ArrayList<InformedNode> expanded = (ArrayList<InformedNode>) examined.expand();

            //examining the expanded nodes and determining if we should add them to the frontier
            //note that label updating is not concretely implemented here, to keep this method generalized
            for (InformedNode n : expanded) {
                l.assignLabel(n);

                //checking if the expanded node is already in the frontier with a worse label
                if (Transposer.hasBetterLabel(n)) {
                    //we remove the node from the frontier and insert it again with the new label
                    //because the PQueue does not support arbitrary access to just get the entry and edit the label field
                    if (frontier.remove(n)) {
                        frontier.add(n);
                        Transposer.saveLabel(n);
                    }

                }
                //checking if the expanded node is already in the transposition table and, if it's not, adding it
                else if (Transposer.transpose(n)){
                    //we add the node to the frontier and we transpose its label for later checking
                    Transposer.saveLabel(n);
                    frontier.add(n);
                }

            }

            //logging
            if (frontier.peek() != null)
                SokobanSolver.setLogLine("Top label value: " + frontier.peek().getLabel() +
                        "\nFrontier size: " + frontier.size() + "\nVisited nodes: " + Transposer.getExaminedNodes());

        }

        return null;
    }
}
