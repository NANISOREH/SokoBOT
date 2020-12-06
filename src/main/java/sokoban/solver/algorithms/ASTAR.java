
package sokoban.solver.algorithms;

import sokoban.game.GameBoard;
import sokoban.solver.ExtendedNode;
import sokoban.solver.Node;
import sokoban.solver.SokobanToolkit;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.logging.Logger;

/*
Implementation of a simple A* search
*/
public class ASTAR {
    private static final Logger log = Logger.getLogger("IDASTAR");
    private static Node solution = null;

    public static Node launch(GameBoard game) throws CloneNotSupportedException {

        ExtendedNode root = new ExtendedNode(game, new ArrayList<>(), null, 0 + SokobanToolkit.estimateLowerBound(game));
        PriorityQueue<ExtendedNode> frontier = new PriorityQueue<>(ExtendedNode::compare);
        frontier.add(root);

        solution = null;
        frontier.add(root);

        //Main loop of the algorithm, we're only going to break it if we found a solution or if the frontier is empty,
        for (int innerCount = 0; !frontier.isEmpty(); innerCount++) {

            //We pop the node with the best heuristic estimate off the PQueue
            ExtendedNode examined = frontier.remove();
            if (examined.isGoal()) //a solution was found
                return examined;

            //expanding the current node and adding the resulting nodes to the frontier Pqueue
            ArrayList<ExtendedNode> expanded = (ArrayList<ExtendedNode>) examined.expand();
            for (Node n : expanded) {

                //we assign the value f(n) = h(n) + g(n) to the label of the new nodes,
                //meaning the sum of the path cost until this point plus the value of the heuristic function
                frontier.add(new ExtendedNode(n, examined,1 + examined.getPathCost() +
                        SokobanToolkit.estimateLowerBound(n.getGame())));
            }

            log.info("frontier " + frontier.size());
        }

        return null;
    }
}



