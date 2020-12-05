
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

    //these two hashmaps are used as a cache to avoid re-exploring the lower levels in iterative deepening
    private static HashMap<Long, Node> cache = new HashMap<>();
    private static HashMap<Long, Node> candidateCache = new HashMap<>();

    public static Node launch(GameBoard game, int lowerBound, Strategy chosenStrategy) throws CloneNotSupportedException {

/*
        Wrapper class that adds a label field to a node.
        The label will store the heuristic estimate of the algorithm
*/
        class ExtendedNode {
            Node node;
            int label;

            ExtendedNode (Node n, int label) {
                this.node = n;
                this.label = label;
            }

            int compare (ExtendedNode extendedNode) {
                return Integer.compare(this.label, extendedNode.label);
            }

            boolean isGoal () {
                return this.node.getGame().checkVictory();
            }
        }

        int limit = lowerBound;
        Node root = new Node(game, new ArrayList<>());
        PriorityQueue<ExtendedNode> frontier = new PriorityQueue<>(ExtendedNode::compare);
        frontier.add(new ExtendedNode(root, 0 + SokobanToolkit.estimateLowerBound(game)));

/*        solution = null;
        strategy = chosenStrategy;
        cache.put(root.hash(), root);*/

        //Loop of the iterative deepening
        while (true) {
            Node.resetSearchSpace();
            frontier.clear();
            frontier.add(new ExtendedNode(root, 0 + SokobanToolkit.estimateLowerBound(game)));

            //Main loop of the algorithm
            for (int count = 0; !frontier.isEmpty(); count++) {
                if (count == 5){
                    for (ExtendedNode e : frontier)
                        log.info("" + e.label);
                }
                //We pop the node with the best heuristic estimate off the PQueue
                ExtendedNode examined = frontier.poll();
                if (examined.isGoal()) //a solution was found
                    return examined.node;
                else if (examined.node.getPathCost() >= limit) //reached the depth limit, we won't expand this node
                    continue;

                //expanding the current node and adding the resulting nodes to the frontier Pqueue
                ArrayList<Node> expanded = (ArrayList<Node>) examined.node.expand();
                for (Node n : expanded) {
                    frontier.add(new ExtendedNode(n, 1 + SokobanToolkit.estimateLowerBound(n.getGame())));
                }

                //if (count % 1000 == 0) log.info("frontier size " + frontier.size());
            }

            log.info("frontier size " + frontier.size());

            //raising the depth limit for the next iteration
            limit = limit + lowerBound;
        }

    }
}