package solver.algorithms;

import game.GameBoard;
import solver.ExtendedNode;
import solver.Node;
import solver.SokobanSolver;
import solver.SokobanToolkit;

import java.util.*;
import java.util.logging.Logger;

public class GreedyBFS extends Algorithm{
    private static final Logger log = Logger.getLogger("BestFirst");

    public Node launch(GameBoard game) throws CloneNotSupportedException {

        //for this algorithm I need to manage transpositions manually rather than letting the Node class do it
        Node.setManageTransposition(false);

        //This structure will keep track of transpositions and current best label of corresponding nodes.
        //It's the only way to have a fast lookup for items in the PQueue with a label that must be updated.
        //The alternative was using a structure that supported arbitrary access to items and just going for get(object)
        //for any expanded node. But these would all be linear time lookups and it was very slow.
        //This way I can do O(1) lookups to check if there's need to update the label
        //and then I only need the linear time access if the check is positive.
        HashMap<Long, Integer> accounting = new HashMap<>();

        SokobanSolver.setLogLine("Top h(n) value: " + "\nFrontier size: 0" + "\nNumber of visited nodes: " + Node.getExaminedNodes());
        int topH = Integer.MAX_VALUE;
        ExtendedNode root = new ExtendedNode(game, new ArrayList<>(), null, SokobanToolkit.estimateLowerBound(game));

        //comparing criteria for the PQueue ordering
        Comparator c = (Comparator<ExtendedNode>) (extendedNode, t1) -> {
            int comparison = Integer.compare(extendedNode.getLabel(), t1.getLabel());

            //tie breaker: inertia
            if (comparison == 0 && extendedNode.getParent() != null && t1.getParent() != null)
                comparison = SokobanToolkit.compareByInertia(extendedNode, t1, extendedNode.getParent(), t1.getParent());

            return comparison;
        };

        //Creating the PQueue and inserting the root node in both the queue and the accounting structure
        PriorityQueue<ExtendedNode> frontier = new PriorityQueue<>(c);
        frontier.add(root);
        accounting.put(root.getHash(), root.getLabel());
        Node.transpose(root);

        //Main loop of the algorithm, we're only going to break it if we found a solution or if the frontier is empty,
        for (int innerCount = 0; !frontier.isEmpty(); innerCount++) {

            //We pop the node with the best heuristic estimate off the PQueue
            ExtendedNode examined = frontier.remove();
            //We put the node we just popped into the transposition table so that it never gets visited again
            //and we remove it from the accounting structure
            accounting.remove(examined.getHash());

            //storing the top h(n) value for logging purposes
            if (examined.getLabel() < topH) topH = examined.getLabel();

            //SOLUTION
            if (examined.isGoal()) {
                Node.setManageTransposition(true);
                return examined;
            }

            //expanding the current node and adding the resulting nodes to the frontier Pqueue
            ArrayList<ExtendedNode> expanded = (ArrayList<ExtendedNode>) examined.expand();
            for (Node n : expanded) {

                //we assign the value of the heuristic h(n) to the label of the new nodes,
                ExtendedNode temp = new ExtendedNode(n, examined, SokobanToolkit.estimateLowerBound(n.getGame()));

                //SOLUTION
                if (temp.isGoal()) {
                    Node.setManageTransposition(true);
                    return temp;
                }

                //checking if the expanded node is already in the frontier with a worse label
                if (accounting.containsKey(temp.getHash()) && temp.getLabel() < accounting.get(temp.getHash())) {
                    log.info("PROVA");
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
            SokobanSolver.setLogLine("Best h(n) value encountered: " + topH + "\nFrontier size: "
                    + frontier.size() + "\nNumber of visited nodes: " + Node.getExaminedNodes());

        }

        return null;
    }

}
