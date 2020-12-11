package solver.algorithms;

import game.GameBoard;
import solver.ExtendedNode;
import solver.Node;
import solver.SokobanToolkit;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.logging.Logger;

public class GreedyBFS {
    private static final Logger log = Logger.getLogger("BestFirst");
    private static Node solution = null;

    public static Node launch(GameBoard game) throws CloneNotSupportedException {

        ExtendedNode root = new ExtendedNode(game, new ArrayList<>(), null, SokobanToolkit.estimateLowerBound(game));
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

                //we assign the value of the heuristic h(n) to the label of the new nodes,
                frontier.add(new ExtendedNode(n, examined, SokobanToolkit.estimateLowerBound(n.getGame())));
            }

            //running out of memory, we're pruning a bunch of nodes from the frontier
            //specifically, we remove a number of nodes equal to the branching factor of the level, so that we're certain
            //that the next expansion will be done without problems
            if (frontier.size() > SokobanToolkit.MAX_NODES) {
                log.info("pruning " + branchingFactor + " elements" + "\nfrontier " + frontier.size());
                frontier = SokobanToolkit.pruneWorst(frontier, branchingFactor);
                log.info("after pruning" + "\nfrontier " + frontier.size());
            }

            if (innerCount % 100 == 0) log.info("frontier " + frontier.size() + "\nvisited nodes: " + Node.getExaminedNodes());
        }

        return null;
    }

}
