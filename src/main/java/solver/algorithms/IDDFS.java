
package solver.algorithms;

import game.GameBoard;
import solver.*;

import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;

/*
Implementation of a DFS search with Iterative Deepening and ordering by inertia.
*/
public class IDDFS extends Algorithm{
    private static final Logger log = Logger.getLogger("IDASTAR");
    private static Node solution;

    public Node launch(GameBoard game) throws CloneNotSupportedException {

        SokobanSolver.setLogLine("f(n) cutoff point: 0" + "\nVisited nodes: " +
                "\nCached nodes: ");

        Stack<Node> visited = new Stack<Node>();

        //initializing variables, adding root node to the cache, starting with the initial lower bound of the solution
        //as the first limit for the iterative deepening
        solution = null;
        Node root = new Node(game, new ArrayList<>());
        int limit = SokobanToolkit.estimateLowerBound(game);

        //Loop of the iterative deepening
        for (int count = 0; true; count++) {

            //initializing the current iteration
            Transposer.resetSearchSpace();
            Transposer.transpose(root);
            visited.push(root);
            DeadlockDetector.setPrunedNodes(0);

            while (!visited.empty() && !SokobanSolver.isInterrupted()) {
                Node current;
                current = visited.pop();

                //SOLUTION
                if (current.isBetterGoalThan(solution)) return current;

                //we surpassed the depth limit, we won't expand this node
                if (current.getPathCost() == limit) continue;

                //ordering the expanded nodes by inertia and pushing them into the stack in reverse order,
                //so that nodes that should be visited first are pushed last
                ArrayList<Node> expanded = SokobanToolkit.orderByInertia(current, (ArrayList<Node>) current.expand());
                for (int i = expanded.size() - 1; i >= 0; i--) {
                    Node n = expanded.get(i);

                    if (n.isBetterGoalThan(solution)) return n;
                    if (current.getPathCost() == limit) continue;

                    if (Transposer.transpose(n)) {
                        visited.push(n);
                    }
                }

                SokobanSolver.setLogLine("Depth cutoff point: " + limit + "\nVisited nodes: " + Transposer.getExaminedNodes() +
                        "\nStacked nodes: " + visited.size());
            }

            limit++;

        }
    }

}