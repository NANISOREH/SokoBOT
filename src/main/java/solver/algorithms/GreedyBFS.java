package solver.algorithms;

import game.GameBoard;
import solver.ExtendedNode;
import solver.Node;
import solver.SokobanSolver;
import solver.SokobanToolkit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Logger;

public class GreedyBFS {
    private static final Logger log = Logger.getLogger("BestFirst");

    public static Node launch(GameBoard game) throws CloneNotSupportedException {

        SokobanSolver.setLogLine("Top h(n) value: " + "\nFrontier size: 0" + "\nNumber of visited nodes: " + Node.getExaminedNodes());
        int topH = Integer.MAX_VALUE;

        Node solution = null;
        ExtendedNode root = new ExtendedNode(game, new ArrayList<>(), null, SokobanToolkit.estimateLowerBound(game));
        PriorityQueue<ExtendedNode> frontier = new PriorityQueue<ExtendedNode>(new Comparator<ExtendedNode>() {
            @Override
            public int compare(ExtendedNode extendedNode, ExtendedNode t1) {
                int comparison = Integer.compare(extendedNode.getLabel(), t1.getLabel());

                //tie breaker: inertia
                if (comparison == 0 && extendedNode.getParent() != null && t1.getParent() != null)
                    comparison = SokobanToolkit.compareByInertia(extendedNode, t1, extendedNode.getParent(), t1.getParent());

                return comparison;
            }
        });


        frontier.add(root);
        //Main loop of the algorithm, we're only going to break it if we found a solution or if the frontier is empty,
        for (int innerCount = 0; !frontier.isEmpty(); innerCount++) {

            //We pop the node with the best heuristic estimate off the PQueue
            ExtendedNode examined = frontier.remove();

            if (examined.getLabel() < topH) topH = examined.getLabel();
            if (examined.isGoal()) { //a solution was found
                if (solution == null) {
                    solution = examined;
                }
                else if (solution != null && examined.getPathCost() < solution.getPathCost()) {
                    solution = examined;
                }
                else if (solution != null && examined.getPathCost() == solution.getPathCost()) {
                    if (examined.getActionHistory().size() < solution.getActionHistory().size())
                        solution = examined;
                }

                continue;
            }

            //expanding the current node and adding the resulting nodes to the frontier Pqueue
            ArrayList<ExtendedNode> expanded = (ArrayList<ExtendedNode>) examined.expand();
            for (Node n : expanded) {

                //we assign the value of the heuristic h(n) to the label of the new nodes,
                frontier.add(new ExtendedNode(n, examined, SokobanToolkit.estimateLowerBound(n.getGame())));
            }

            if (innerCount % 100 == 0) {
                SokobanSolver.setLogLine("Top h(n) value: " + topH + "\nFrontier size: " + frontier.size() + "\nNumber of visited nodes: " + Node.getExaminedNodes());
            }
        }

        return solution;
    }

}
