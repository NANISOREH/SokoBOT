package sokoban.solver.algorithms;

import sokoban.game.Action;
import sokoban.game.Cell;
import sokoban.game.CellContent;
import sokoban.game.GameBoard;
import sokoban.solver.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

/*
Implementation of a simple DFS search with Iterative Deepening
*/
public class IDDFS {
    private static Logger log = Logger.getLogger("IDDFS");
    private static ArrayList<Action> solution = new ArrayList<>();
    private static ArrayList<Long> transpositionTableCopy;
    private static boolean memoryFull = false;

    //these two hashmaps are used as a cache to avoid re-exploring the lower levels in iterative deepening
    private static HashMap<Long, Node> cache = new HashMap<>();
    private static HashMap<Long, Node> candidateCache = new HashMap<>();

    //private static ArrayList<Long> transpositionTable = new ArrayList<>();

    public static ArrayList<Action> launch(GameBoard game, int lowerBound) throws CloneNotSupportedException {

        Node root = new Node(null, game, new ArrayList<>());
        cache.put(root.hash(), root);
        solution = new ArrayList<>();
        int limit = lowerBound;
        log.info("The lower bound estimate is: " + limit);

        //Iterative deepening cycle
        for (int count = 0; true; count++) {

            //Before every iteration, if the memory is not full yet, we make a copy of the transposition table so that,
            //when we finally have no more space, we can can restore its state to effectively start a proper, uncached
            //IDDFS starting from the last cached frontier
            if (!memoryFull) {
                transpositionTableCopy = new ArrayList<>(Node.getTranspositionTable());
            }
            //If memory is full, we have to restore the transposition table backup and increment the limit of the search:
            //from now on, no more cached nodes and we will start over from the last cached frontier
            else if (memoryFull){
                Node.setTranspositionTable(transpositionTableCopy);
                limit = limit + lowerBound;
            }

            //In every iteration, cache will store the nodes in the deepest level reached by the previous iteration.
            //This way, we can keep expanding from there and avoid the burden of having to store the whole path.
            //candidateCache, instead, will store the nodes found in the deepest allowed level of the current iteration,
            //in other words, if the memory doesn't run out during recursiveComponent calls, it will be the next cache.
            if (!memoryFull) candidateCache = new HashMap<>();
            for (Long key : cache.keySet()) { //launching the DFS on every element in the cache
                recursiveComponent(cache.get(key), limit);
            }
            if (!memoryFull) cache = new HashMap<>(candidateCache);

            log.info("visited nodes at depth " + lowerBound * count + ": " + Node.getExaminedNodes());

            //If we found a solution in this iteration, we put out the garbage and then return it
            if (!solution.isEmpty() && solution.size() > 0) {
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
    private static void recursiveComponent (Node root,int limit) throws CloneNotSupportedException {
        //solution checking
        if (root.getGame().checkVictory()) {
            solution = new ArrayList<>(root.getActionHistory());
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
                candidateCache.clear();
            }
            return;
        }

        //creating the children of the current root node
        ArrayList<Node> expanded = orderMoves(root, (ArrayList<Node>) root.expand());

        //recursively calling this method on root's children, lowering by one the depth they are allowed to explore
        for (Node n : expanded) {
            recursiveComponent(n, limit - 1);
        }

    }

/*
    A simple move ordering optimization: states that involve pushing a box that was pushed by their parents too
    are considered before the others. That's useful because a lot of Sokoban proper solutions involve a certain number of consecutive
    pushes to the same box.
*/
    private static ArrayList<Node> orderMoves (Node root, ArrayList < Node > expanded){
        Integer boxNumber = root.getLastMovedBox();
        if (boxNumber == null) {
            return expanded;
        }

        ArrayList<Node> result = new ArrayList<>();

        for (Node n : expanded) {
            if (n.getLastMovedBox() == null)
                continue;
            else if (n.getLastMovedBox().equals(boxNumber))
                result.add(n);
        }

        for (Node n : expanded) {
            if (!result.contains(n)) {
                result.add(n);
            }
        }

        return result;
    }
}
