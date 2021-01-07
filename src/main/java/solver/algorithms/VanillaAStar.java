
package solver.algorithms;

import game.GameBoard;
import solver.*;

import java.util.*;
import java.util.logging.Logger;

/*
Implementation of a simple memory-bounded A* search
*/
public class VanillaAStar extends Algorithm{
    private static final Logger log = Logger.getLogger("AStar");

    public Node launch(GameBoard game) throws CloneNotSupportedException {

        SokobanSolver.setLogLine("Top h(n) value: " + "\nFrontier size: 0" + "\nNumber of visited nodes: " + Transposer.getExaminedNodes());

        //comparing criteria for the PQueue ordering is defined in the InformedNode class
        PriorityQueue<InformedNode> frontier = new PriorityQueue<InformedNode>(InformedNode::astarCompare);

        //Inserting the root node in the queue, in the accounting structure and the transposition table
        InformedNode root = new InformedNode(game, new ArrayList<>(), null, SokobanToolkit.estimateLowerBound(game));
        frontier.add(root);
        Transposer.transpose(root);
        Transposer.saveLabel(root);

        //Main loop of the algorithm, we're only going to break it if we found a solution or if the frontier is empty,
        while (!frontier.isEmpty() && !SokobanSolver.isInterrupted()) {

            //We pop the node with the best heuristic estimate off the PQueue
            InformedNode examined = frontier.remove();
            //We remove the node we just popped from the structure that keeps the labels for the frontier.
            //If the heuristic chosen is consistent, then A* doesn't need to keep track of "closed" nodes:
            //we will be sure that whenever a node is extracted from the frontier, it will be extracted with the best value.
            Transposer.removeLabel(examined);

            //SOLUTION
            if (examined.isGoal()) {
                return examined;
            }

            //expanding the current node and adding the resulting nodes to the frontier Pqueue
            ArrayList<InformedNode> expanded = (ArrayList<InformedNode>) examined.expand();
            
            //setting the heuristic estimate for the expanded nodes to f(n) = g(n) + h(n)
            for (InformedNode n : expanded) {
                n.setLabel(n.getPathCost() + SokobanToolkit.estimateLowerBound(n.getGame()));
            }

            //examining the expanded nodes
            for (InformedNode n : expanded) {
                //SOLUTION
                if (n.isGoal()) {
                    return n;
                }

                //checking if the expanded node is already in the frontier with a worse label
                if (Transposer.hasBetterLabel(n)) {
                    //we remove the node from the frontier and insert it again with the new label
                    //because the PQueue does not support arbitrary access to just get the entry and edit the label field
                    if (frontier.remove(n)) {
                        frontier.add(n);
                        Transposer.saveLabel(n);
                    }

                }
                //checking if the expanded node is already in the transposition table and, if it's not, adding it
                else if (Transposer.transpose(n)){
                    //we add the node to the frontier and we transpose its label for later checking
                    Transposer.saveLabel(n);
                    frontier.add(n);
                }

            }

            //logging
            if (frontier.peek() != null)
                SokobanSolver.setLogLine("Top label value: " + frontier.peek().getLabel() +
                        "\nFrontier size: " + frontier.size() + "\nVisited nodes: " + Transposer.getExaminedNodes());

        }

        return null;
    }
}