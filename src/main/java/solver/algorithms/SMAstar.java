
package solver.algorithms;

import game.GameBoard;
import solver.ExtendedNode;
import solver.Node;
import solver.SokobanSolver;
import solver.SokobanToolkit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.logging.Logger;

/*
Implementation of a simple memory-bounded A* search
*/
public class SMAstar {
    private static final Logger log = Logger.getLogger("SMAStar");

    //This structure will keep track of transpositions and current best label of corresponding nodes.
    //It's the only way to have a fast lookup for items in the PQueue with a label that must be updated.
    //The alternative was using a structure that supported arbitrary access to items and just going for get(object)
    //for any expanded node. But these would all be linear time lookups and it was very slow.
    //This way I can do O(1) lookups to check if there's need to update the label
    //and then I only need the linear time access if the check is positive.
    private static HashMap<Long, Integer> accounting = new HashMap<>();

    public static Node launch(GameBoard game) throws CloneNotSupportedException {

        SokobanSolver.setLogLine("Frontier size: " + "\nVisited nodes: ");
        ExtendedNode root = new ExtendedNode(game, new ArrayList<>(), null, 0 + SokobanToolkit.estimateLowerBound(game));
        PriorityQueue<ExtendedNode> frontier = new PriorityQueue<ExtendedNode>(new Comparator<ExtendedNode>() {
            @Override
            public int compare(ExtendedNode extendedNode, ExtendedNode t1) {
                //main criteria for insertion into the pqueue
                //it will favor the lowest f(n) label value among the two nodes, but every node keeps
                //the label value of his best child too, and that will be considered in the comparison, so that we can
                //keep track of the nodes that were pruned to bound memory usage and rebuild pruned branches
                int comparison = Integer.compare(Math.min(extendedNode.getLabel(), extendedNode.getBestChild()),
                        Math.min(t1.getLabel(), t1.getBestChild()));

                //tie breaker: inertia
                if (comparison == 0 && extendedNode.getParent() != null && t1.getParent() != null)
                    comparison = SokobanToolkit.compareByInertia(extendedNode, t1, extendedNode.getParent(), t1.getParent());

                //tie breaker: heuristics without the path cost
                if (comparison == 0)
                    comparison = Integer.compare(extendedNode.getLabel() - extendedNode.getPathCost(),
                            t1.getLabel() - t1.getPathCost());

                return comparison;
            }
        });

        frontier.add(root);
        int branchingFactor = game.getBoxCells().size() * 4;

        //Main loop of the algorithm, we're only going to break it if we found a solution or if the frontier is empty,
        for (int innerCount = 0; !frontier.isEmpty(); innerCount++) {

            //We pop the node with the best heuristic estimate off the PQueue
            ExtendedNode examined = frontier.remove();

            if (examined.isGoal()) {
                return examined;
            }

            //expanding the current node and adding the resulting nodes to the frontier Pqueue
            ArrayList<ExtendedNode> expanded = (ArrayList<ExtendedNode>) examined.expand();
            for (Node n : expanded) {
                //we assign the value f(n) = h(n) + g(n) to the label of the new nodes,
                //meaning the sum of the path cost until this point plus the value of the heuristic function

                ExtendedNode temp = new ExtendedNode(n, examined, examined.getPathCost() +
                            SokobanToolkit.estimateLowerBound(n.getGame()));

                //storing the heuristic estimate of the best child of the current node:
                //we will use it to determine if we have to regenerate pruned nodes
                if (temp.getLabel() < examined.getBestChild())
                    examined.setBestChild(temp.getLabel());

                //adding the newly generated note to the frontier
                if (!frontier.contains(temp))
                    frontier.add(temp);

            }

            //running out of memory, we're pruning a bunch of nodes from the frontier
            //specifically, we remove a number of nodes equal to the branching factor of the level, so that we're certain
            //that the next expansion will be done without problems
            if (frontier.size() > SokobanToolkit.MAX_NODES) {
                    log.info("pruning " + branchingFactor + " elements" + "\nfrontier " + frontier.size());
                    frontier = pruneWorst(frontier, branchingFactor);
                    log.info("after pruning" + "\nfrontier " + frontier.size());
            }

            if (frontier.peek() != null)
                SokobanSolver.setLogLine("Top f(n) value: " + Math.min(frontier.peek().getLabel(), frontier.peek().getBestChild()) +
                    "\nFrontier size: " + frontier.size() + "\nVisited nodes: " + Node.getExaminedNodes());
        }

        return null;
    }


    public static PriorityQueue pruneWorst(PriorityQueue<ExtendedNode> frontier, int amount) throws CloneNotSupportedException {
        PriorityQueue<ExtendedNode> newFrontier = new PriorityQueue<>(frontier.comparator());
        ExtendedNode temp;
        int initialSize = frontier.size();
        int target = frontier.size() - amount;

        int i;
        for (i=0; i < target; i++) {
            temp = frontier.remove();
            newFrontier.add(new ExtendedNode(temp, temp.getParent(), temp.getLabel()));
        }
        for (i=0; i < initialSize - target; i++) {
            temp = frontier.remove();
            //Node.untranspose(temp);
            accounting.remove(temp.getHash());
        }

        return newFrontier;
    }

}



