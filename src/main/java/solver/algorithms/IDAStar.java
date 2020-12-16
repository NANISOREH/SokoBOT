
package solver.algorithms;

import game.GameBoard;
import solver.ExtendedNode;
import solver.Node;
import solver.SokobanSolver;
import solver.SokobanToolkit;

import java.util.*;
import java.util.logging.Logger;

public class IDAStar {
    private static final Logger log = Logger.getLogger("IDASTAR");
    private static Node solution;
    private static int newLimit;
    private static TreeSet<Long> transpositionTableCopy = new TreeSet<>();
    private static boolean memoryFull = false;

    //these two hashmaps are used as a cache to avoid re-exploring the lower levels in iterative deepening
    private static ArrayList<ExtendedNode> cache;
    private static ArrayList<ExtendedNode> candidateCache;

    public static Node launch(GameBoard game) throws CloneNotSupportedException {
        newLimit = Integer.MAX_VALUE;
        cache = new ArrayList<>();
        candidateCache = new ArrayList<>();

        SokobanSolver.setLogLine("f(n) cutoff point: 0" + "\nVisited nodes: " + Node.getExaminedNodes() +
                "\nCached nodes: " + (cache.size() + candidateCache.size()));

        solution = null;
        ExtendedNode root = new ExtendedNode(new Node(game, new ArrayList<>()), null, 0 + SokobanToolkit.estimateLowerBound(game));
        int lowerBound = root.getLabel();
        int limit = lowerBound;
        cache.add(root);

        //Loop of the iterative deepening
        for (int count = 0; true; count++) {
            //Before every iteration, if the memory is not full yet, we make a copy of the transposition table so that,
            //when we finally have no more space, we can can restore its state to effectively start a proper, uncached
            //iterative deepening starting from the last cached frontier
            if (!memoryFull) {
                transpositionTableCopy.clear();
                transpositionTableCopy = (TreeSet<Long>) Node.getTranspositionTable().clone();
            }
            //If memory is full, we have to restore the transposition table backup and increment the limit of the search:
            //from now on, no more cached nodes and we will start over from the last cached frontier
            else if (memoryFull){
                Node.resetSearchSpace();
                Node.setTranspositionTable((TreeSet<Long>) transpositionTableCopy.clone());
            }


            newLimit = Integer.MAX_VALUE;
            //launching the search on the current limit
            //the limit will be raised inside the recursive component and stored in newLimit
            if (!memoryFull) candidateCache = new ArrayList<>();
            for (ExtendedNode n : cache) {
                recursiveComponent(n, limit);
            }
            limit = newLimit;

            //We copy the frontier we formed in the last iteration to be used as a starting point for the next one
            if (!memoryFull) {
                cache = new ArrayList<>();
                for (int i = candidateCache.size() - 1; i >= 0; i--) {
                    cache.add(candidateCache.get(i));
                    candidateCache.remove(i);
                }

            }

            //If we found a solution in this iteration, we put out the garbage and then return it
            if (solution != null && solution.getActionHistory().size() > 0) {
                transpositionTableCopy.clear();
                cache.clear();
                candidateCache.clear();
                return solution;
            }

        }

    }

    private static void recursiveComponent (ExtendedNode root, int limit) throws CloneNotSupportedException {
        SokobanSolver.setLogLine("f(n) cutoff point: " + limit + "\nVisited nodes: " + Node.getExaminedNodes() +
                "\nCached nodes: " + (cache.size() + candidateCache.size()));

        //we surpassed the current heuristic estimate, so we stop the recursion and update the limit
        //the new limit will be the lowest f(n) value among those who surpassed the current limit
        //if the memory allows it, this node will be in the cache for the next iteration
        if (root.getLabel() > limit && !memoryFull) {
            if (cache.size() + candidateCache.size() < SokobanToolkit.MAX_NODES) {
                //this node will be cached
                candidateCache.add(root);

                if (root.getLabel() < newLimit)
                    newLimit = root.getLabel();
            }
            else {
                //no more space, we clean the thing and start from the last cached frontier
                memoryFull = true;
                log.info("NO MORE MEMORY");
                candidateCache.clear();
                newLimit = limit;
                return;
            }

            return;
        }
        else if (root.getLabel() > limit && memoryFull) {
            if (root.getLabel() < newLimit)
                newLimit = root.getLabel();
            return;
        }

        //this queue will keep the expanded batch of nodes ordered
        PriorityQueue<ExtendedNode> queue = new PriorityQueue<ExtendedNode>(new Comparator<ExtendedNode>() {
            @Override
            public int compare(ExtendedNode extendedNode, ExtendedNode t1) {
                //main criteria for insertion into the pqueue
                //it will favor the lowest f(n) label value among the two nodes
                int comparison = Integer.compare(extendedNode.getLabel(), t1.getLabel());

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

        //expanding the current node and launching the search on its children
        //ordered by their labels
        ArrayList<Node> expanded = (ArrayList<Node>) root.expand();
        for (Node n : expanded) {
            queue.add(new ExtendedNode(n, root, n.getPathCost() +
                    SokobanToolkit.estimateLowerBound(n.getGame())));
        }
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            if (isSolution(queue.peek())) return;
            else recursiveComponent(queue.remove(), limit);
        }

    }

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