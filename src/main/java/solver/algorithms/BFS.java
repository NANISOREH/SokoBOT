package solver.algorithms;

import game.Action;
import game.GameBoard;
import solver.Node;
import java.util.ArrayList;
import java.util.logging.Logger;

/*
Implementation of a simple BFS search
*/
public class BFS {
    private static Logger log = Logger.getLogger("BFS");

    public static Node launch(GameBoard game) throws CloneNotSupportedException {

        //Utility data structure used to isolate the nodes inside the "frontier" of the search, the ones we will need to
        //expand in the current iteration. It's initialized by adding the first frontier, with only the root node inside
        ArrayList<Node> front = new ArrayList<>();
        Node root = new Node(game, new ArrayList<Action>());
        front.add(root);

        //Algorithm loop
        //I'm using an iterative version: every node in the frontier is expanded and the children (the next frontier) are added
        //to a second utility structure that will be expanded the same way on the next iteration.
        //The stopping condition is, of course, finding a node that represents a winning state.
        for (int count = 0; true; count++) {
            log.info("level " + count + "\nfront size " + front.size() + "\nexplored nodes " + Node.getExaminedNodes());
            ArrayList<Node> nextLevel = new ArrayList<>();
            for (Node n : front) {
                if (n.getGame().checkVictory()) {
                    return n;
                }
                for (Node v : n.expand()){
                        nextLevel.add(v);
                }
            }

            if (front.size() == 0) return null;

            //"Promoting" the nodes found by expanding the current frontier for the next iteration
            front.clear();
            for (int i = nextLevel.size() - 1; i>=0; i--) {
                front.add(nextLevel.remove(i));
            }

        }

    }
}
