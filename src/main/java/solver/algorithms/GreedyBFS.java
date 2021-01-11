package solver.algorithms;

import game.GameBoard;
import solver.*;

// Implementation of a greedy best-first search algorithm
// Code from generic Algorithm implementation is completely reused: the only needed change is in how we compare nodes
// for Priority Queue insertion and extraction order and how we label nodes. This time, f(n) = h(n) for any given node.
public class GreedyBFS extends Algorithm{

    public Node launch(GameBoard game) throws CloneNotSupportedException {
        //Passing the node comparison logic method and the node labelling logic method
        return super.launchPQueueSearch(game, GreedyBFS::compare, GreedyBFS::assignLabel);
    }

    protected static int compare(InformedNode informedNode, InformedNode t1) {
        int comparison = Integer.compare(informedNode.getLabel(), t1.getLabel());

        //tie breaker: inertia
        if (comparison == 0 && informedNode.getParent() != null && t1.getParent() != null)
            comparison = SokobanToolkit.compareByInertia(informedNode, t1, informedNode.getParent(), t1.getParent());

        return comparison;
    }

    protected static void assignLabel(InformedNode informedNode) {
        informedNode.setLabel(SokobanToolkit.heuristicEstimate(informedNode.getGame()));
    }

}
