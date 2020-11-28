package sokoban.solver;

import sokoban.game.Action;
import sokoban.game.Cell;
import sokoban.game.CellContent;
import sokoban.game.GameBoard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.logging.Logger;

/*
This class will collect static methods implementing reusable algorithm optimizations and, in general, solutions to
subproblems encountered in the main flow of the program. Its only purpose is to keep other classes a bit cleaner.
*/
public class SokobanToolkit {
    private static Logger log = Logger.getLogger("SokobanToolkit");
/*
    This methods roughly estimates a lower bound to get a starting point for iterative deepening algorithms.
    It sees boxes tiles and goal tiles as two partitions of a bipartite graph and it constructs a complete matching
    between the two. It obtains the final value by summing the manhattan distances between the members of the matching couples.
*/
    protected static int estimateLowerBound(GameBoard toSolve) {
        HashMap<Integer, Cell> boxes = (HashMap<Integer, Cell>) toSolve.getBoxCells().clone();
        ArrayList<Cell> goals = (ArrayList<Cell>) toSolve.getGoalCells().clone();
        int result = 0;

        for (int i = 0; i <= boxes.size(); i++) {
            result = result + boxes.get(i).manhattanDistance(goals.get(0));
            boxes.remove(i);
            goals.remove(0);
        }

        return result;
    }

/*
    A simple move ordering optimization: states that involve pushing a box that was pushed by their parents too
    are considered before the others. This is useful because a lot of Sokoban proper solutions involve a certain number of consecutive
    pushes to the same box.
*/
    public static ArrayList<Node> orderByInertia (Node root, ArrayList <Node> expanded){
        Integer boxNumber = root.getLastMovedBox();
        if (boxNumber == null) {
            return expanded;
        }

        ArrayList<Node> result = new ArrayList<>();

        for (Node n : expanded) {
            if (n.getLastMovedBox() == null)
                continue;
            else if (n.getLastMovedBox() == boxNumber)
                result.add(n);
        }

        for (Node n : expanded) {
            if (!result.contains(n)) {
                result.add(n);
            }
        }

        return result;
    }


    /*
    Given a game state and a cell, it determines if Sokoban can reach that cell and returns a list of actions
    containing the best path that Sokoban can use to get there, or a null list in case there's no way to reach the target.
    It uses a BFS to do so.
*/
    public static ArrayList<Action> searchPath (GameBoard state, Cell target) throws CloneNotSupportedException {

        if (state.getSokobanCell().equals(target)) {
            return new ArrayList<>();
        }

        //Local class modeling a node for the BFS
        class BfsNode {
            ArrayList<Action> path;
            Cell node;

            public BfsNode (ArrayList<Action> path, Cell node) {
                this.path = path;
                this.node = node;
            }

            public void addAction (Action a) {
                path.add(a);
            }

            public BfsNode clone () throws CloneNotSupportedException {
                BfsNode d = new BfsNode((ArrayList) this.path.clone(), (Cell) this.node.clone());
                return d;
            }
        }

        ArrayList<Cell> transpositionTable = new ArrayList<>();

        ArrayList<BfsNode> front = new ArrayList<>();
        BfsNode root = new BfsNode(new ArrayList<>(), state.getSokobanCell());
        front.add(root);
        transpositionTable.add(root.node);

        while (true) {
            ArrayList<BfsNode> nextLevel = new ArrayList<>();
            for (BfsNode n : front) {
                if (n.node.equals(target)) {
                    return n.path;
                }

                if (state.getNorth(n.node).getContent() == CellContent.EMPTY) {
                    BfsNode north = n.clone();
                    north.node = state.getNorth(n.node);
                    if (!transpositionTable.contains(north.node)) {
                        transpositionTable.add(north.node);
                        north.addAction(Action.MOVE_UP);
                        nextLevel.add(north);
                    }
                }

                if (state.getSouth(n.node).getContent() == CellContent.EMPTY) {
                    BfsNode south = n.clone();
                    south.node = state.getSouth(n.node);
                    if (!transpositionTable.contains(south.node)) {
                        transpositionTable.add(south.node);
                        south.addAction(Action.MOVE_DOWN);
                        nextLevel.add(south);
                    }
                }

                if (state.getEast(n.node).getContent() == CellContent.EMPTY) {
                    BfsNode east = n.clone();
                    east.node = state.getEast(n.node);
                    if (!transpositionTable.contains(east.node)) {
                        transpositionTable.add(east.node);
                        east.addAction(Action.MOVE_RIGHT);
                        nextLevel.add(east);
                    }
                }

                if (state.getWest(n.node).getContent() == CellContent.EMPTY) {
                    BfsNode west = n.clone();
                    west.node = state.getWest(n.node);
                    if (!transpositionTable.contains(west.node)) {
                        transpositionTable.add(west.node);
                        west.addAction(Action.MOVE_LEFT);
                        nextLevel.add(west);
                    }
                }
            }

            if (front.size() == 0)
                return null;

            //"Promoting" the nodes found by expanding the current frontier for the next iteration
            front.clear();
            for (int i = nextLevel.size() - 1; i>=0; i--) {
                front.add(nextLevel.remove(i));
            }

        }

    }


}
