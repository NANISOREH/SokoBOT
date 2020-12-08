
package solver.algorithms;

import game.GameBoard;
import solver.ExtendedNode;
import solver.Node;
import solver.SokobanToolkit;

import java.util.ArrayList;
import java.util.logging.Logger;

public class IDAStar {
    private static final Logger log = Logger.getLogger("IDASTAR");
    private static Node solution = null;
    private static int newLimit;

    public static Node launch(GameBoard game, int lowerBound) throws CloneNotSupportedException {

        int limit = lowerBound;
        solution = null;
        ExtendedNode root = new ExtendedNode(new Node(game, new ArrayList<>()), null, 0 + SokobanToolkit.estimateLowerBound(game));

        //Loop of the iterative deepening
        for (int count = 0; true; count++) {
            Node.resetSearchSpace();
            newLimit = Integer.MAX_VALUE;

            //launching the search on the current limit
            recursiveComponent(root, limit);
            //the limit will be raised inside the recursive component and stored in newLimit
            limit = newLimit;

            if (solution != null)
                return solution;

            log.info("visited nodes on iteration " + count + ": " + Node.getExaminedNodes());
        }

    }

    private static void recursiveComponent (ExtendedNode root, int limit) throws CloneNotSupportedException {

        if (root.isGoal()) {
            solution = root;
            return;
        }

        //we surpassed the current heuristic estimate, so we stop the recursion and update the limit
        //the new limit will be the lowest f(n) value among those who surpassed the current limit
        if (root.getLabel() > limit) {
            if (root.getLabel() < newLimit)
                newLimit = root.getLabel();

            return;
        }

        //expanding the current node and launching the search on its children
        ArrayList<Node> expanded = (ArrayList<Node>) root.expand();
        for (Node n : expanded) {
            recursiveComponent(new ExtendedNode(n, root,1 + root.getPathCost() +
                    SokobanToolkit.estimateLowerBound(n.getGame())), limit);
        }
    }

}