
package solver.algorithms;

import game.GameBoard;
import solver.*;

import java.util.logging.Logger;

/*
Implementation of a simple A* search
Code from generic Algorithm implementation is completely reused: the only needed change is in how we compare nodes
for Priority Queue insertion and extraction order and how we label nodes. This time, f(n) = g(n) + h(n) for any given node.
*/
public class VanillaAStar extends Algorithm{
    private static final Logger log = Logger.getLogger("AStar");

    public Node launch(GameBoard game) throws CloneNotSupportedException {
        //Passing the node comparison logic method and the node labelling logic method
        return super.launchPQueueSearch(game, VanillaAStar::compare, VanillaAStar::assignLabel);
    }

    protected static int compare(InformedNode informedNode, InformedNode t1) {
        //main criteria for insertion into the pqueue
        //it will favor the lowest f(n) label value among the two nodes
        int comparison = Integer.compare(informedNode.getLabel(), t1.getLabel());

        //tie breaker: inertia
        if (comparison == 0 && informedNode.getParent() != null && t1.getParent() != null)
            comparison = SokobanToolkit.compareByInertia(informedNode, t1, informedNode.getParent(), t1.getParent());

        //tie breaker: heuristics without the path cost
        if (comparison == 0)
            comparison = Integer.compare(informedNode.getLabel() - informedNode.getPathCost(),
                    t1.getLabel() - t1.getPathCost());

        return comparison;
    }

    public static void assignLabel(InformedNode informedNode) {
        informedNode.setLabel(informedNode.getPathCost() + SokobanToolkit.heuristicEstimate(informedNode.getGame()));
    }
}