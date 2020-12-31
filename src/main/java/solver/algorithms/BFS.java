package solver.algorithms;

import game.Action;
import game.GameBoard;
import solver.Node;
import solver.SokobanSolver;
import solver.Transposer;

import java.util.ArrayList;
import java.util.logging.Logger;

/*
Implementation of a simple BFS search
*/
public class BFS extends Algorithm{
    private static Logger log = Logger.getLogger("BFS");

    public Node launch(GameBoard game) throws CloneNotSupportedException {

        SokobanSolver.setLogLine("Depth level 0" + "\nFront size: 0" +"\nExplored nodes: 0");

        //Utility data structure used to isolate the nodes inside the "frontier" of the search, the ones we will need to
        //expand in the current iteration. It's initialized by adding the first frontier, with only the root node inside
        ArrayList<Node> front = new ArrayList<>();
        Node root = new Node(game, new ArrayList<Action>());
        if (root.isGoal()) {
            return root;
        }

        //Algorithm loop
        //I'm using an iterative version: every node in the frontier is expanded and the children (the next frontier) are added
        //to a second utility structure that will be expanded the same way on the next iteration.
        //The stopping condition is, of course, finding a node that represents a winning state.
        front.add(root);
        for (int count = 0; true; count++) {
            ArrayList<Node> nextLevel = new ArrayList<>();
            for (Node n : front) {
                SokobanSolver.setLogLine("Depth level " + count + "\nFront size: " + front.size() + "\nExplored nodes: " + Transposer.getExaminedNodes());
                for (Node v : n.expand()){
                    if (v.isGoal()) {
                        return v;
                    }
                    //only adding the node to the next frontier if it's not already in the transposition table
                    else if (Transposer.transpose(v)) {
                        nextLevel.add(v);
                    }
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
