
package solver.algorithms;

import game.GameBoard;
import solver.*;

import java.util.*;
import java.util.logging.Logger;

/*
Implementation of an IDA* algorithm
*/
public class IDAStar extends Algorithm{
    private static final Logger log = Logger.getLogger("IDASTAR");
    private static Node solution;

    public Node launch(GameBoard game) throws CloneNotSupportedException {

        SokobanSolver.setLogLine("f(n) cutoff point: 0" + "\nVisited nodes: " +
                "\nCached nodes: ");

        //initializing variables, adding root node to the cache, starting with the initial lower bound of the solution
        //as the first limit for the iterative deepening
        solution = null;
        InformedNode root = new InformedNode(new Node(game, new ArrayList<>()), null, 0 + SokobanToolkit.estimateLowerBound(game));
        int lowerBound = root.getLabel();
        int limit = lowerBound;

        //Loop of the iterative deepening
        for (int count = 0; !SokobanSolver.isInterrupted(); count++) {

            //Resetting everything
            Transposer.resetSearchSpace();
            Transposer.transpose(root);
            DeadlockDetector.setPrunedNodes(0);

            int newLimit;
            //launching the search on the current limit
            //the limit will be raised inside the recursive component and stored in newLimit
            if (solution == null)
                newLimit = recursiveComponent(root, 0, limit);
            else
                break;

            limit = newLimit;

            //If we found a solution in this iteration, we return it
            if (solution != null && solution.getActionHistory().size() > 0) {
                return solution;
            }

        }
        return solution;
    }

    private static int recursiveComponent (InformedNode root, int pathLength, int limit) throws CloneNotSupportedException {
        SokobanSolver.setLogLine("f(n) cutoff point: " + limit + "\nVisited nodes: " + Transposer.getExaminedNodes() +
                "\n");

        //SOLUTION
        if (isSolution(root) || solution != null) {
            return 0;
        }

        //we surpassed the threshold, returning the label of the current node to his father
        if (root.getLabel() > limit) {
            return root.getLabel();
        }

        //this queue will keep the expanded batch of nodes ordered by heuristic estimate
        PriorityQueue<InformedNode> queue = new PriorityQueue<InformedNode>(InformedNode::astarCompare);

        //expanding the current node and launching the search on its children
        //ordered by their labels
        ArrayList<InformedNode> expanded = (ArrayList<InformedNode>) root.expand();
        for (InformedNode n : expanded) {
            if (!Transposer.transpose(n))
                continue;
            else {
                n.setLabel(pathLength + SokobanToolkit.estimateLowerBound(n.getGame()));
                queue.add(n);
            }
        }

        //launching the recursive method on the expanded batch
        //and using the return values to determine the minimum label value surpassing the limit
        //met during the exploration of all subtrees starting from the current node:
        //of course the actual root will collect the global minimum after all recursive calls got "wrapped"
        int size = queue.size();
        int min = Integer.MAX_VALUE;
        int temp;
        for (int i = 0; i < size; i++) {
            if (isSolution(queue.peek())) return 0;
            else temp = recursiveComponent(queue.remove(), pathLength + 1, limit);

            if (temp < min) min = temp;
        }

        //returning the minimum to the father of the current node
        return min;
    }

    //checks for the solution and updates the solution static variable if we didn't already find a better one
    private static boolean isSolution(Node n) {
        if (n.isGoal()) {
            if (solution == null) {
                solution = n;
            }
            else if (solution != null && n.getPathCost() < solution.getPathCost()) {
                solution = n;
            }
            else if (solution != null && n.getPathCost() == solution.getPathCost()) {
                if (n.getActionHistory().size() < solution.getActionHistory().size())
                    solution = n;
            }
            return true;
        }

        return false;
    }

}