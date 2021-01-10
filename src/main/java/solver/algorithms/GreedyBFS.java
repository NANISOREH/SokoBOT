package solver.algorithms;

import game.GameBoard;
import solver.*;

// Implementation of a greedy best-first search algorithm
// Code from generic Algorithm implementation is completely reused: the only needed change is in how we compare nodes
// for Priority Queue insertion and extraction order. This time, f(n) = h(n) for any given node.
public class GreedyBFS extends Algorithm{

    public Node launch(GameBoard game) throws CloneNotSupportedException {
        return super.launchPQueueSearch(game, (informedNode, t1) -> {
            informedNode.setLabel(SokobanToolkit.estimateLowerBound(informedNode.getGame()));
            t1.setLabel(SokobanToolkit.estimateLowerBound(t1.getGame()));
            int comparison = Integer.compare(informedNode.getLabel(), t1.getLabel());

            //tie breaker: inertia
            if (comparison == 0 && informedNode.getParent() != null && t1.getParent() != null)
                comparison = SokobanToolkit.compareByInertia(informedNode, t1, informedNode.getParent(), t1.getParent());

            return comparison;
        });
    }

}
