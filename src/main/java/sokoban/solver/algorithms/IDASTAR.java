
package sokoban.solver.algorithms;

import sokoban.game.GameBoard;
import sokoban.solver.Node;
import sokoban.solver.SokobanToolkit;
import sokoban.solver.Strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.logging.Logger;

public class IDASTAR {
    private static final Logger log = Logger.getLogger("IDASTAR");
    private static Node solution = null;
    private static ArrayList<Long> transpositionTableCopy = new ArrayList<>();
    private static boolean memoryFull = false;
    private static Strategy strategy;

    public static Node launch(GameBoard game, int lowerBound, Strategy chosenStrategy) throws CloneNotSupportedException {

/*
        Wrapper class that adds a label field to a node.
        The label will store the heuristic estimate of the algorithm
*/
        class ExtendedNode {
            Node node;
            Node parent;
            int label;

            ExtendedNode (Node n, Node parent, int label) {
                this.parent = parent;
                this.node = n;
                this.label = label;
            }

/*
            Compares nodes when inserting them into the priority queue
            Nodes with the lowest label are favored, meaning that the priority queue will first extract the nodes
            with the smallest estimated distance from the goal state. In case of a tie, inertial move ordering is applied:
            we favor nodes in which Sokoban moved the same box as the last turn.
*/
            int compare (ExtendedNode extendedNode) {
                int comparison =  Integer.compare(this.label, extendedNode.label);

                if (comparison != 0)
                    return comparison;
                else if (this.parent != null)
                    return SokobanToolkit.compareByInertia(this.node, extendedNode.node, this.parent);

                return 0;
            }

            boolean isGoal () {
                return this.node.getGame().checkVictory();
            }
        }

        int limit = lowerBound;
        ExtendedNode root = new ExtendedNode(new Node(game, new ArrayList<>()), null, 0 + SokobanToolkit.estimateLowerBound(game));
        PriorityQueue<ExtendedNode> frontier = new PriorityQueue<>(ExtendedNode::compare);
        frontier.add(root);

        //these two hashmaps are used as a cache to avoid re-exploring the lower levels in iterative deepening
        ArrayList<ExtendedNode> cache = new ArrayList<>();
        ArrayList<ExtendedNode> cacheBackup = new ArrayList<>();
        solution = null;
        frontier.add(root);
        cacheBackup.add(root);


        //Loop of the iterative deepening
        for (int count = 0; true; count++) {
            if (!memoryFull) {
                transpositionTableCopy.clear();
                transpositionTableCopy = (ArrayList<Long>) Node.getTranspositionTable().clone();

                cacheBackup.clear();
                cacheBackup.addAll(cache);
                cache.clear();
                frontier.addAll(cacheBackup);
            }
            else if (memoryFull){
                Node.resetSearchSpace();
                Node.setTranspositionTable((ArrayList<Long>) transpositionTableCopy.clone());

                frontier.addAll(cacheBackup);
            }

            //Main loop of the algorithm, we're only going to break it if we found a solution or if frontier is empty,
            //meaning that we explored all nodes up until a certain depth
            for (int innerCount = 0; !frontier.isEmpty(); count++) {

                //We pop the node with the best heuristic estimate off the PQueue
                ExtendedNode examined = frontier.remove();
                if (examined.isGoal()) //a solution was found
                    return examined.node;

                if (examined.node.getPathCost() >= limit) { //reached the depth limit, we won't expand this node

                    if (!memoryFull) {
                        if ((Runtime.getRuntime().freeMemory() / 1024) / 1024 > 100) { //we have the memory to keep on using the cache
                            cache.add(examined);
                        } else { //we ran out of memory
                            memoryFull = true;
                            log.info("NO MORE MEMORY");
                            cache.clear();
                        }
                    }

                    continue;
                }

                //expanding the current node and adding the resulting nodes to the frontier Pqueue
                ArrayList<Node> expanded = (ArrayList<Node>) examined.node.expand();
                for (Node n : expanded) {
                    frontier.add(new ExtendedNode(n, examined.node, 1 + SokobanToolkit.estimateLowerBound(n.getGame())));
                }

                if (innerCount % 10000 == 0) {
                    log.info("frontier: " + frontier.size() + "\nvisited nodes: " + Node.getExaminedNodes());
                }
            }

            //incrementing the max depth for the next iteration
            limit++;

            log.info(limit + ": cache size " + cache.size());
            log.info("visited nodes: " + Node.getExaminedNodes());
        }

    }
}