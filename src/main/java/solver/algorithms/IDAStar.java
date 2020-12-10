
package solver.algorithms;

import game.GameBoard;
import solver.ExtendedNode;
import solver.Node;
import solver.SokobanToolkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Logger;

public class IDAStar {
    private static final Logger log = Logger.getLogger("IDASTAR");
    private static Node solution = null;
    private static int newLimit;
    private static TreeSet<Long> transpositionTableCopy = new TreeSet<>();
    private static boolean memoryFull = false;

    //these two hashmaps are used as a cache to avoid re-exploring the lower levels in iterative deepening
    private static HashMap<Long, ExtendedNode> cache = new HashMap<>();
    private static HashMap<Long, ExtendedNode> candidateCache = new HashMap<>();

    public static Node launch(GameBoard game, int lowerBound) throws CloneNotSupportedException {

        int limit = lowerBound;
        solution = null;
        ExtendedNode root = new ExtendedNode(new Node(game, new ArrayList<>()), null, 0 + SokobanToolkit.estimateLowerBound(game));
        cache.put(root.hash(), root);

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
                limit = limit + lowerBound/2;
            }


            newLimit = Integer.MAX_VALUE;
            //launching the search on the current limit
            //the limit will be raised inside the recursive component and stored in newLimit
            if (!memoryFull) candidateCache = new HashMap<>();
            for (Long key : cache.keySet()) {
                recursiveComponent(cache.get(key), limit);
            }
            limit = newLimit;

            //We copy the frontier we formed in the last iteration to be used as a starting point for the next one
            if (!memoryFull) cache = new HashMap<>(candidateCache);

            //If we found a solution in this iteration, we put out the garbage and then return it
            if (solution != null && solution.getActionHistory().size() > 0) {
                transpositionTableCopy.clear();
                cache.clear();
                candidateCache.clear();
                return solution;
            }

            log.info("visited nodes on iteration " + count + ": " + Node.getExaminedNodes());
        }

    }

    private static void recursiveComponent (ExtendedNode root, int limit) throws CloneNotSupportedException {

        if (solution != null) return;

        if (root.isGoal()) {
            solution = root;
            return;
        }

        //we surpassed the current heuristic estimate, so we stop the recursion and update the limit
        //the new limit will be the lowest f(n) value among those who surpassed the current limit
        //if the memory allows it, this node will be in the cache for the next iteration
        if (root.getLabel() > limit && !memoryFull) {
            if (cache.size() + candidateCache.size() < SokobanToolkit.MAX_NODES) {
                candidateCache.put(root.hash(), root);
            }
            else {
                memoryFull = true;
                log.info("NO MORE MEMORY");
                candidateCache.clear();
            }

            if (root.getLabel() < newLimit)
                newLimit = root.getLabel();

            return;
        }
        else if (root.getLabel() > limit && memoryFull) {
            candidateCache.clear();
            if (root.getLabel() < newLimit)
                newLimit = root.getLabel();
            return;
        }

        //expanding the current node and launching the search on its children
        ArrayList<Node> expanded = SokobanToolkit.orderByInertia(root, (ArrayList<Node>) root.expand());
        for (Node n : expanded) {
            recursiveComponent(new ExtendedNode(n, root,1 + root.getPathCost() +
                    SokobanToolkit.estimateLowerBound(n.getGame())), limit);
        }
    }

}