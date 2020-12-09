
package solver.algorithms;

import game.GameBoard;
import solver.Node;
import solver.SokobanToolkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/*
Implementation of a DFS search with Iterative Deepening and a couple of optimization,
namely move ordering by inertia and caching of the lower depths of the search to speed up the initial part of the search.
TODO: test move ordering, right now it seems to have no effect
*/
public class IDDFS {
    private static Logger log = Logger.getLogger("IDDFS");
    private static Node solution = null;
    private static ArrayList<Long> transpositionTableCopy = new ArrayList<>();
    private static boolean memoryFull = false;

    //these two hashmaps are used as a cache to avoid re-exploring the lower levels in iterative deepening
    private static HashMap<Long, Node> cache = new HashMap<>();
    private static HashMap<Long, Node> candidateCache = new HashMap<>();

    public static Node launch(GameBoard game, int lowerBound) throws CloneNotSupportedException {

        solution = null;
        Node root = new Node(game, new ArrayList<>());
        if (root.isGoal()) return root;
        cache.put(root.hash(), root);
        int limit = lowerBound;
        log.info("The lower bound estimate is: " + limit);

        //Iterative deepening cycle
        for (int count = 0; true; count++) {

            //Before every iteration, if the memory is not full yet, we make a copy of the transposition table so that,
            //when we finally have no more space, we can can restore its state to effectively start a proper, uncached
            //IDDFS starting from the last cached frontier
            if (!memoryFull) {
                transpositionTableCopy.clear();
                transpositionTableCopy = (ArrayList<Long>) Node.getTranspositionTable().clone();
            }
            //If memory is full, we have to restore the transposition table backup and increment the limit of the search:
            //from now on, no more cached nodes and we will start over from the last cached frontier
            else if (memoryFull){
                Node.resetSearchSpace();
                Node.setTranspositionTable((ArrayList<Long>) transpositionTableCopy.clone());
                limit = limit + lowerBound/2;
            }

            //In every iteration, cache will store the nodes in the deepest level reached by the previous iteration.
            //This way, we can keep expanding from there and avoid the burden of having to store the whole path.
            //candidateCache, instead, will store the nodes found in the deepest allowed level of the current iteration,
            //in other words, if the memory doesn't run out during recursiveComponent calls, it will be the next cache.
            if (!memoryFull) candidateCache = new HashMap<>();
            for (Long key : cache.keySet()) {
                //Launching the DFS on every element in the cache.
                if (count == 0) recursiveComponent(cache.get(key), limit);
                if (count > 0) recursiveComponent(cache.get(key), limit/3);
            }
            if (!memoryFull) cache = new HashMap<>(candidateCache);

            log.info("visited nodes at depth " + Node.getDepth() + ": " + Node.getExaminedNodes());

            //If we found a solution in this iteration, we put out the garbage and then return it
            if (solution != null && solution.getActionHistory().size() > 0) {
                transpositionTableCopy.clear();
                cache.clear();
                candidateCache.clear();
                return solution;
            }

        }
    }

    /*
        Private helper method that actually kickstarts the DFS recursive call stack
    */
    private static void recursiveComponent (Node root, int limit) throws CloneNotSupportedException {

        if (solution != null) return;

        //solution checking
        if (root.isGoal()) {
            solution = root;
            return;
        }

        //if we reached the bottom without finding a solution, the search will stop and
        //(if the memory allows it) this node will be in the cache for the next iteration
        if (limit == 0 && !memoryFull) {
            if ((Runtime.getRuntime().freeMemory() / 1024) / 1024 > 100) {
                candidateCache.put(root.hash(), root);
            }
            else {
                memoryFull = true;
                log.info("NO MORE MEMORY");
                candidateCache.clear();
            }
            return;
        }
        else if (limit == 0 && memoryFull) {
            candidateCache.clear();
            return;
        }
        //creating the children of the current root node
        ArrayList<Node> expanded = (ArrayList<Node>) root.expand();
        //ordering by inertia if move ordering was selected by the client
        expanded = new ArrayList<>(SokobanToolkit.orderByInertia(root, expanded));

        //recursively calling this method on root's children, lowering by one the depth they are allowed to explore
        for (Node n : expanded) {
            recursiveComponent(n, limit - 1);
        }

    }

}