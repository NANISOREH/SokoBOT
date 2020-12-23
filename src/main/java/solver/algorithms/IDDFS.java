
package solver.algorithms;

import game.GameBoard;
import solver.Node;
import solver.SokobanSolver;
import solver.SokobanToolkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Logger;

/*
Implementation of a DFS search with Iterative Deepening and a couple of optimization,
namely move ordering by inertia and caching of the lower depths of the search to speed up the initial part of the search.
TODO: test move ordering, right now it seems to have no effect
*/
public class IDDFS extends Algorithm{
    private static Logger log = Logger.getLogger("IDDFS");
    private static Node solution = null;
    private static TreeSet<Long> transpositionTableCopy = new TreeSet<>();
    private static int depthCopy;
    private static boolean memoryFull = false;

    //these two hashmaps are used as a cache to avoid re-exploring the lower levels in iterative deepening
    private static ArrayList<Node> cache = new ArrayList<>();
    private static ArrayList<Node> candidateCache = new ArrayList<>();

    public Node launch(GameBoard game) throws CloneNotSupportedException {

        SokobanSolver.setLogLine("Depth cutoff point: " + Node.getDepth() + "\nVisited nodes: " + Node.getExaminedNodes() +
                "\nCached nodes: " + (cache.size() + candidateCache.size()));

        solution = null;
        Node root = new Node(game, new ArrayList<>());
        if (root.isGoal()) return root;
        cache.add(root);
        int lowerBound = SokobanToolkit.estimateLowerBound(game);
        int limit = lowerBound;
        log.info("The lower bound estimate is: " + limit);

        //Iterative deepening cycle
        for (int count = 0; true; count++) {

            //Before every iteration, if the memory is not full yet, we make a copy of the transposition table so that,
            //when we finally have no more space, we can can restore its state to effectively start a proper, uncached
            //IDDFS starting from the last cached frontier
            if (!memoryFull) {
                transpositionTableCopy.clear();
                transpositionTableCopy = (TreeSet<Long>) Node.getTranspositionTable().clone();
                depthCopy = Node.getDepth();
            }
            //If memory is full, we have to restore the transposition table backup and increment the limit of the search:
            //from now on, no more cached nodes and we will start over from the last cached frontier
            else if (memoryFull){
                Node.resetSearchSpace();
                Node.setTranspositionTable((TreeSet<Long>) transpositionTableCopy.clone());
                Node.setDepth(depthCopy);
                limit = limit + 1;
            }

            //In every iteration, cache will store the nodes in the deepest level reached by the previous iteration.
            //This way, we can keep expanding from there and avoid the burden of having to store the whole path.
            //candidateCache, instead, will store the nodes found in the deepest allowed level of the current iteration,
            //in other words, if the memory doesn't run out during recursiveComponent calls, it will be the next cache.
            if (!memoryFull) {
                candidateCache = new ArrayList<>();
            }

            //Launching the DFS on every element in the cache.
            for (Node n : cache) {
                if (solution != null) break;

                //In case memory is full we will start with a cached frontier as a starting point
                //so we'll set the depth of the recursion to the depth limit minus the current depth.
                //Also, if it's the first iteration we will go as deep as the lower bound of the solution
                //because it makes no sense to stop before that, since we won't find any shallower solution.
                if (count == 0 || memoryFull) recursiveComponent(n, limit - Node.getDepth());

                //In case memory is not full we just go one level deeper
                //This way we're getting all the advantages of BFS but we switch to proper IDDFS once memory starts
                //to fail us.
                else recursiveComponent(n, 1);
            }

            //We copy the frontier we formed in the last iteration to be used as a starting point for the next one
            if (!memoryFull) {
                cache = new ArrayList<>();
                for (int i = candidateCache.size() - 1; i >= 0; i--) {
                    cache.add(candidateCache.remove(i));
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

    /*
        Private helper method that actually kickstarts the DFS recursive call stack
    */
    private static void recursiveComponent (Node root, int limit) throws CloneNotSupportedException {

        SokobanSolver.setLogLine("Depth cutoff point: " + Node.getDepth() + "\nVisited nodes: " + Node.getExaminedNodes() +
                "\nCached nodes: " + (cache.size() + candidateCache.size()));

        if (isSolution(root)) return;

        //if we reached the bottom without finding a solution, the search will stop and
        //(if the memory allows it) this node will be in the cache for the next iteration
        if (limit == 0 && !memoryFull) {
            if (cache.size() + candidateCache.size() < SokobanToolkit.MAX_NODES) {
                candidateCache.add(root);
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
            if (isSolution(n)) return;
            else recursiveComponent(n, limit - 1);
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