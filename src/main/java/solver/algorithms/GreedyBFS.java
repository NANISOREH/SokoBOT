package solver.algorithms;

import game.GameBoard;
import solver.ExtendedNode;
import solver.Node;
import solver.SokobanSolver;
import solver.SokobanToolkit;

import java.util.*;
import java.util.logging.Logger;

// Implementation of a greedy best-first search algorithm
public class GreedyBFS extends Algorithm{
    private static final Logger log = Logger.getLogger("BestFirst");

    public Node launch(GameBoard game) throws CloneNotSupportedException {

        //for this algorithm I need to manage transpositions manually rather than letting the Node class do it
        Node.setManageTransposition(false);

        //This structure will keep track of transpositions and current best label of corresponding nodes.
        //It's the only way to have a fast lookup for items in the PQueue with a label that must be updated.
        //The alternative was using a structure that supported arbitrary access to items and just going for get(object)
        //for any expanded node. But these would all be linear time lookups and it was very slow.
        //With this accounting structure instead I can do O(1) lookups to check if there's a need to update the label
        //and then I only do the linear time access if the check is positive.
        HashMap<Long, Integer> accounting = new HashMap<>();

        //logging stuff
        SokobanSolver.setLogLine("Top h(n) value: " + "\nFrontier size: 0" + "\nNumber of visited nodes: " + Node.getExaminedNodes());
        int topH = Integer.MAX_VALUE;

        //Creating the PQueue and inserting the root node in both the frontier, the accounting structure and the transposition table
        PriorityQueue<ExtendedNode> frontier = new PriorityQueue<>(ExtendedNode::gbfsCompare);
        ExtendedNode root = new ExtendedNode(game, new ArrayList<>(), null, SokobanToolkit.estimateLowerBound(game));
        frontier.add(root);
        accounting.put(root.getHash(), root.getLabel());
        Node.transpose(root);

        //Main loop of the algorithm, we're only going to break it if we found a solution or if the frontier is empty,
        for (int innerCount = 0; !frontier.isEmpty(); innerCount++) {

            //We pop the node with the best heuristic estimate off the PQueue
            ExtendedNode examined = frontier.remove();
            //We remove the node from the accounting structure
            accounting.remove(examined.getHash());

            //storing the top h(n) value for logging purposes
            if (examined.getLabel() < topH) topH = examined.getLabel();

            //SOLUTION
            if (examined.isGoal()) {
                Node.setManageTransposition(true);
                return examined;
            }

            //expanding the current node and
            //assigning the value of the heuristic h(n) to the label of the new nodes
            ArrayList<ExtendedNode> expanded = (ArrayList<ExtendedNode>) examined.expand();
            for (ExtendedNode n : expanded) {
                n.setLabel(SokobanToolkit.estimateLowerBound(n.getGame()));
            }

            //examining expanded nodes
            for (ExtendedNode n : expanded) {
                //SOLUTION
                if (n.isGoal()) {
                    Node.setManageTransposition(true);
                    return n;
                }

                //checking if the expanded node is already in the frontier with a worse label
                if (accounting.containsKey(n.getHash()) && n.getLabel() < accounting.get(n.getHash())) {
                    //we remove the node from the frontier and insert it again with the new label
                    //because the PQueue does not support arbitrary access to just get the entry and edit the label field
                    if (frontier.remove(n)) {
                        frontier.add(n);
                        accounting.replace(n.getHash(), n.getLabel());
                    }

                }
                else if (!Node.isTransposed(n.getHash())){
                    //this node is not present in both the frontier and the transposition table, so we just add it
                    frontier.add(n);
                    accounting.put(n.getHash(), n.getLabel());
                    Node.transpose(n);
                }
            }

            //logging
            SokobanSolver.setLogLine("Best h(n) value encountered: " + topH + "\nFrontier size: "
                    + frontier.size() + "\nNumber of visited nodes: " + Node.getExaminedNodes());

        }

        return null;
    }

}
