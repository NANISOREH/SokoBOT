package solver.algorithms;

import game.GameBoard;
import solver.*;

import java.util.*;
import java.util.logging.Logger;

// Implementation of a greedy best-first search algorithm
public class GreedyBFS extends Algorithm{
    private static final Logger log = Logger.getLogger("BestFirst");

    public Node launch(GameBoard game) throws CloneNotSupportedException {

        //logging stuff
        SokobanSolver.setLogLine("Top h(n) value: " + "\nFrontier size: 0" + "\nNumber of visited nodes: " + Transposer.getExaminedNodes());
        int topH = Integer.MAX_VALUE;

        //Creating the PQueue and inserting the root node in both the frontier, the accounting structure and the transposition table
        PriorityQueue<InformedNode> frontier = new PriorityQueue<>(InformedNode::gbfsCompare);
        InformedNode root = new InformedNode(game, new ArrayList<>(), null, SokobanToolkit.estimateLowerBound(game));
        frontier.add(root);
        Transposer.saveLabel(root);
        Transposer.transpose(root);

        //Main loop of the algorithm, we're only going to break it if we found a solution or if the frontier is empty,
        for (int innerCount = 0; !frontier.isEmpty(); innerCount++) {

            //We pop the node with the best heuristic estimate off the PQueue and remove it from the accounting table
            InformedNode examined = frontier.remove();
            Transposer.removeLabel(examined);

            //storing the top h(n) value for logging purposes
            if (examined.getLabel() < topH) topH = examined.getLabel();

            //SOLUTION
            if (examined.isGoal()) {
                return examined;
            }

            //expanding the current node and
            //assigning the value of the heuristic h(n) to the label of the new nodes
            ArrayList<InformedNode> expanded = (ArrayList<InformedNode>) examined.expand();
            for (InformedNode n : expanded) {
                n.setLabel(SokobanToolkit.estimateLowerBound(n.getGame()));
            }

            //examining expanded nodes
            for (InformedNode n : expanded) {
                //SOLUTION
                if (n.isGoal()) {
                    return n;
                }

                //checking if the expanded nodes are already in the frontier with a worse label
                if (Transposer.hasBetterLabel(n)) {
                    //we remove the node from the frontier and insert it again with the new label
                    //because the PQueue does not support arbitrary access to just get the entry and edit the label field
                    if (frontier.remove(n)) {
                        frontier.add(n);
                        Transposer.saveLabel(n);
                    }

                }
                //checking if the expanded node is already in the transposition table and, if it's not
                //the transpose method will add it
                else if (Transposer.transpose(n)){
                    //this node is not present in both the frontier and the accounting table, so we just add it
                    Transposer.saveLabel(n);
                    frontier.add(n);
                }
            }

            //logging
            SokobanSolver.setLogLine("Best label value encountered: " + topH + "\nFrontier size: "
                    + frontier.size() + "\nNumber of visited nodes: " + Transposer.getExaminedNodes());

        }

        return null;
    }

}
