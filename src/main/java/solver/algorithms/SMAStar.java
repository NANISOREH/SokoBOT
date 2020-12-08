
package solver.algorithms;

import game.GameBoard;
import solver.ExtendedNode;
import solver.Node;
import solver.SokobanToolkit;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.logging.Logger;

/*
Implementation of a simple memory-bounded A* search
*/
public class SMAStar {
    private static final Logger log = Logger.getLogger("IDASTAR");
    private static Node solution = null;

    public static Node launch(GameBoard game) throws CloneNotSupportedException {

        ExtendedNode root = new ExtendedNode(game, new ArrayList<>(), null, 0 + SokobanToolkit.estimateLowerBound(game));
        PriorityQueue<ExtendedNode> frontier = new PriorityQueue<ExtendedNode>(ExtendedNode::compare);
        frontier.add(root);

        solution = null;
        frontier.add(root);
        int branchingFactor = game.getBoxCells().size() * 4;

        //Main loop of the algorithm, we're only going to break it if we found a solution or if the frontier is empty,
        for (int innerCount = 0; !frontier.isEmpty(); innerCount++) {

            //We pop the node with the best heuristic estimate off the PQueue
            ExtendedNode examined = frontier.remove();
            if (examined.isGoal()) //a solution was found
                return examined;

            //expanding the current node and adding the resulting nodes to the frontier Pqueue
            ArrayList<ExtendedNode> expanded = (ArrayList<ExtendedNode>) examined.expand();
            for (Node n : expanded) {

                //we assign the value f(n) = h(n) + g(n) to the label of the new nodes,
                //meaning the sum of the path cost until this point plus the value of the heuristic function
                frontier.add(new ExtendedNode(n, examined,1 + examined.getPathCost() +
                        SokobanToolkit.estimateLowerBound(n.getGame())));
            }

            //running out of memory, we're pruning a bunch of nodes from the frontier
            //specifically, we remove a number of nodes equal to the branching factor of the level, so that we're certain
            //that the next expansion will be done without problems
            if ((Runtime.getRuntime().freeMemory() / 1024) / 1024 < 100) {
                    log.info("pruning " + branchingFactor + " elements" + "\nfrontier " + frontier.size());
                    frontier = prune(frontier, branchingFactor);
                    log.info("after pruning" + "\nfrontier " + frontier.size());
            }

            if (innerCount % 10000 == 0) log.info("frontier " + frontier.size() + "\nvisited nodes: " + Node.getExaminedNodes());
        }

        return null;
    }

    private static PriorityQueue prune(PriorityQueue<ExtendedNode> frontier, int branchingFactor) {
        PriorityQueue<ExtendedNode> newFrontier = new PriorityQueue<>(ExtendedNode::compare);
        ExtendedNode temp;
        int target = frontier.size() - branchingFactor;

        for (int i=0; i < target; i++) {
            temp = frontier.remove();
            newFrontier.add(new ExtendedNode(temp, temp.getParent(), temp.getLabel()));
        }

        return newFrontier;
    }
}



