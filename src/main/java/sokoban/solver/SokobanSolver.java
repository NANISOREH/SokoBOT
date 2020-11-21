package sokoban.solver;

import sokoban.game.Action;
import sokoban.game.Cell;
import sokoban.game.GameBoard;
import sokoban.solver.algorithms.BFS;
import sokoban.solver.algorithms.IDDFS;

import java.util.ArrayList;
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
/*
    Static method that acts as a façade between the client and the actual algorithms.
    It takes a GameBoard configured with the level to solve, and the strategy chosen by the client to solve it,
    and launches the search accordingly. If a solution is found, it acts upon the original GameBoard to show it.
*/
    public static void solve(GameBoard toSolve, Strategy strategy) throws InterruptedException, CloneNotSupportedException {

        ArrayList<Action> solution = null;

        //Resetting the transposition table stored in the Node class just in case we are launching
        //a search on the same level in the same session of the program
        Node.resetTranspositionTable();

        switch (strategy) {
            case BFS -> {
                solution = BFS.launch((GameBoard) toSolve.clone());
                break;
            }
            case IDDFS -> {
                solution = IDDFS.launch((GameBoard) toSolve.clone(), estimateLowerBound(toSolve));
                break;
            }

        }

        //Showing the list of actions in the console and executing the corresponding moves on the board
        if (solution != null) {
            log.info("Solution found in " + solution.size() + " moves!");
            log.info("" + solution);
            for (Action a : solution) {
                //we execute every action in the solution: the board will automagically solve the puzzle as a result
                toSolve.takeAction(a);
                Thread.sleep(200);
            }
        }
        else
            log.info("Sorry, no solution was found!");
    }


/*
    This methods estimates a lower bound to get a starting point for iterative deepening algorithms.
    It sees boxes tiles and goal tiles as two partitions of a bipartite graph and it constructs a complete matching
    between the two. It obtains the lower bound by summing the manhattan distances between the members of the matching couples.
*/
    private static int estimateLowerBound(GameBoard toSolve) {
        ArrayList<Cell> boxes = (ArrayList<Cell>) toSolve.getBoxCells().clone();
        ArrayList<Cell> goals = (ArrayList<Cell>) toSolve.getGoalCells().clone();
        int result = 0;

        //The matched box-goal couples are extracted from the arraylists after each iteration, so we end the loop when
        //there's nothing left to match
        while (!boxes.isEmpty() && !goals.isEmpty()) {

            //We create a map containing for every box cell an array with the manhattan distance
            //from that box cell to every goal cell
            Integer[] costs = new Integer[boxes.size()];
            LinkedHashMap<Cell, Integer[]> edges = new LinkedHashMap<>();
            for (Cell c : boxes) {
                for (int i = 0; i < goals.size(); i++) {
                    costs[i] = c.manhattanDistance(goals.get(i));
                }
                edges.put(c, costs);
            }

            //We iterate over all the boxes in the map and we find the shortest manhattan distance over every possible couple,
            //then we save the indexes of the box and the goal which had the best link at the end of the nested for cycles.
            //It's worth noting that this greedy strategy does not guarantee that we find the best overall matching possible.
            int best = 2147483647;
            int boxIndex = 0; int goalIndex = 0;
            for (int j = 0; j < boxes.size(); j++) {
                costs = edges.get(boxes.get(j));

                for (int i = 0; i < costs.length; i++) {
                    if (costs[i] > best) { //It should be the other way around but it works only if I flip it...
                        best = costs[i];
                        goalIndex = j;
                        boxIndex = i;
                    }
                }

            }

            //Updating the result, removing already matched cells.
            result = result + boxes.get(boxIndex).manhattanDistance(goals.get(goalIndex));
/*            log.info("CHOSEN:\n" + "box at: " + boxes.get(boxIndex).getRow() + " - " + boxes.get(boxIndex).getColumn() +
                    "\n" + "goal at: " +  goals.get(goalIndex).getRow() + " - " + goals.get(goalIndex).getColumn() +
                    "\n" + "manhattan distance: " + boxes.get(boxIndex).manhattanDistance(goals.get(goalIndex)) + "\n");*/


            boxes.remove(boxIndex);
            goals.remove(goalIndex);

        }

        return result;
    }
}
