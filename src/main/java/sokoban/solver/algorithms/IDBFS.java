package sokoban.solver.algorithms;

import sokoban.game.Action;
import sokoban.game.GameBoard;
import sokoban.solver.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class IDBFS {

    private static Logger log = Logger.getLogger("IDBFS");

    public static ArrayList<Action> launch(GameBoard game) throws CloneNotSupportedException {

        //Data structure where all the nodes will be mantained throughout the search:
        //in a BFS. we always need to mantain all of the explored nodes through every iteration
        ArrayList<Node> tree = new ArrayList<>();

        //Utility data structure used to isolate the nodes inside the "frontier" of the search, the ones we will need to
        //expand in the current iteration. It's initialized by adding the first front, with only the root node inside
        ArrayList<Node> front = new ArrayList<>();
        Node root = new Node(null, game, new ArrayList<Action>());
        front.add(root);

        //Algorithm loop
        //I'm using an iterative version: every node in the frontier is expanded and the children (the next frontier) are added
        //to a second utility structure that will be expanded the same way on the next iteration.
        //The stopping condition is, of course, finding a node that represents a winning state.
        for (int j=0;  j < 8; j++) {
            log.info("tree size " + tree.size() + "\nfront size " + front.size());
            ArrayList<Node> nextLevel = new ArrayList<>();
            for (Node n : front) {
                if (n.getGame().checkVictory()) {
                    return n.getActionHistory();
                }
                n.setVisited(true);
                for (Node v : n.expand()){
                    if (!v.isVisited())
                        nextLevel.add(v);
                }
            }

            for (int i = front.size() - 1; i>=0; i--) {
                tree.add(front.remove(i));
            }

            for (int i = nextLevel.size() - 1; i>=0; i--) {
                front.add(nextLevel.remove(i));
            }

        }

        return null;
    }

}
