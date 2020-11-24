package sokoban.solver;

import sokoban.game.Action;
import sokoban.game.Cell;
import sokoban.game.GameBoard;
import sokoban.solver.algorithms.BFS;
import sokoban.solver.algorithms.IDDFS;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import static java.lang.StrictMath.abs;

/*
    Actor class that solves a sokoban puzzle.
    This class's responsibility is to take "orders" from the client, executing the preliminary operations for the algorithm's
    execution, launching the algorithm itself and finally distributing the solution.
    In fact, it only delegates the inner logic of the implemented algorithms.
*/

public class SokobanSolver {
    private static Logger log = Logger.getLogger("SokobanSolver");
    private static ArrayList<Action> solution = null;
    private static double timeElapsed;
/*
    Static method that acts as a façade between the client and the actual algorithms.
    It takes a GameBoard configured with the level to solve, and the strategy chosen by the client to solve it,
    and launches the search accordingly. If a solution is found, it acts upon the original GameBoard to show it.
*/
    public static void solve(GameBoard toSolve, Strategy strategy) throws InterruptedException, CloneNotSupportedException {

        solution = null;
        Long start;

        //Resetting the transposition table stored in the Node class just in case we are launching
        //a search on the same level in the same session of the program
        Node.resetTranspositionTable();

        start = Instant.now().toEpochMilli();
        switch (strategy) {
            case BFS -> {
                solution = BFS.launch((GameBoard) toSolve.clone());
                break;
            }
            case IDDFS, IDDFS_MO -> {
                solution = IDDFS.launch((GameBoard) toSolve.clone(), estimateLowerBound(toSolve), strategy);
                break;
            }

        }
        timeElapsed = (double) (Instant.now().toEpochMilli() - start) / 1000;

        //Showing the list of actions in the console and executing the corresponding moves on the board
        if (solution != null && !solution.isEmpty()) {
            log.info("Solution found in " + solution.size() + " moves!");
            log.info("number of examined nodes: " + Node.getExaminedNodes());
            log.info("" + solution);
            for (Action a : solution) {
                //we execute every action in the solution: the board will automagically solve the puzzle as a result
                toSolve.takeAction(a);
                Thread.sleep(200);
            }
        }
        else {
            log.info("Sorry, no solution was found!");
            log.info("number of examined nodes: " + Node.getExaminedNodes());
        }
    }


/*
    This methods roughly estimates a lower bound to get a starting point for iterative deepening algorithms.
    It sees boxes tiles and goal tiles as two partitions of a bipartite graph and it constructs a complete matching
    between the two. It obtains the final value by summing the manhattan distances between the members of the matching couples.
*/
    private static int estimateLowerBound(GameBoard toSolve) {
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

    public static ArrayList<Action> getSolution() {
        return solution;
    }

    public static double getTimeElapsed() {
        return timeElapsed;
    }
}
