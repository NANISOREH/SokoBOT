
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
public class VanillaAStar extends Algorithm{
    private static final Logger log = Logger.getLogger("SMAStar");

    //This structure will keep track of transpositions and current best label of corresponding nodes.
    //It's the only way to have a fast lookup for items in the PQueue with a label that must be updated.
    //The alternative was using a structure that supported arbitrary access to items and just going for get(object)
    //for any expanded node. But these would all be linear time lookups and it was very slow.
    //This way I can do O(1) lookups to check if there's need to update the label
    //and then I only need the linear time access if the check is positive.
    private static HashMap<Long, Integer> accounting = new HashMap<>();

    public Node launch(GameBoard game) throws CloneNotSupportedException {
        //for this algorithm I need to manage transpositions manually rather than letting the Node class do it
        Node.setManageTransposition(false);

        SokobanSolver.setLogLine("Top h(n) value: " + "\nFrontier size: 0" + "\nNumber of visited nodes: " + Node.getExaminedNodes());
        ExtendedNode root = new ExtendedNode(game, new ArrayList<>(), null, SokobanToolkit.estimateLowerBound(game));
        int branchingFactor = game.getBoxCells().size() * 4;

        //comparing criteria for the PQueue ordering
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

        //Inserting the root node in the queue, in the accounting structure and the transposition table
        frontier.add(root);
        accounting.put(root.getHash(), root.getLabel());
        Node.transpose(root);

        //Main loop of the algorithm, we're only going to break it if we found a solution or if the frontier is empty,
        for (int innerCount = 0; !frontier.isEmpty(); innerCount++) {

            //We pop the node with the best heuristic estimate off the PQueue
            ExtendedNode examined = frontier.remove();
            //We remove the node we just popped from the accounting structure
            accounting.remove(examined.getHash());

            //SOLUTION
            if (examined.isGoal()) {
                Node.setManageTransposition(true);
                return examined;
            }

            //expanding the current node and adding the resulting nodes to the frontier Pqueue
            ArrayList<ExtendedNode> expanded = (ArrayList<ExtendedNode>) examined.expand();
            for (Node n : expanded) {
                //we assign the value of the heuristic h(n) to the label of the new nodes,
                ExtendedNode temp = new ExtendedNode(n, examined, n.getPathCost() + SokobanToolkit.estimateLowerBound(n.getGame()));

                //SOLUTION
                if (temp.isGoal()) {
                    Node.setManageTransposition(true);
                    return temp;
                }

                //checking if the expanded node is already in the frontier with a worse label
                if (accounting.containsKey(temp.getHash()) && temp.getLabel() < accounting.get(temp.getHash())) {
                    //we remove the node from the frontier and insert it again with the new label
                    //because the PQueue does not support arbitrary access to just get the entry and edit the label field
                    if (frontier.remove(temp)) {
                        frontier.add(temp);
                        accounting.replace(temp.getHash(), temp.getLabel());
                    }

                }
                else if (!Node.isTransposed(temp.getHash())){
                    //this node is not present in both the frontier and the transposition table, so we just add it
                    frontier.add(temp);
                    accounting.put(temp.getHash(), temp.getLabel());
                    Node.transpose(temp);
                }

            }

            //logging
            if (frontier.peek() != null)
                SokobanSolver.setLogLine("Top f(n) value: " + Math.min(frontier.peek().getLabel(), frontier.peek().getBestChild()) +
                        "\nFrontier size: " + frontier.size() + "\nVisited nodes: " + Node.getExaminedNodes());

        }

        return null;
    }
}



