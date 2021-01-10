
package solver.algorithms;

import game.GameBoard;
import solver.*;

import java.util.logging.Logger;

/*
Implementation of a simple A* search
Code from generic Algorithm implementation is completely reused: the only needed change is in how we compare nodes
for Priority Queue insertion and extraction order. This time, f(n) = g(n) + h(n) for any given node.
*/
public class VanillaAStar extends Algorithm{
    private static final Logger log = Logger.getLogger("AStar");

    public Node launch(GameBoard game) throws CloneNotSupportedException {
        return super.launchPQueueSearch(game, VanillaAStar::astarCompare);
    }

    protected static int astarCompare(InformedNode informedNode, InformedNode t1) {
        //main criteria for insertion into the pqueue
        //it will favor the lowest f(n) label value among the two nodes
        informedNode.setLabel(informedNode.getPathCost() + SokobanToolkit.estimateLowerBound(informedNode.getGame()));
        t1.setLabel(t1.getPathCost() + SokobanToolkit.estimateLowerBound(t1.getGame()));
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
}