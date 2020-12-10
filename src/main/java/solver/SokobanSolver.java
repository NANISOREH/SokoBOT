package solver;

import game.Action;
import game.GameBoard;
import solver.algorithms.*;
import solver.configuration.Configuration;
import solver.configuration.DDRoutine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;

import static java.lang.StrictMath.abs;

/*
    Actor class that solves a sokoban puzzle.
    This class's responsibility is to take "orders" from the client, executing the preliminary operations for the algorithm's
    execution, launching the algorithm itself and finally distributing the solution.
*/

public class SokobanSolver {
    private static Logger log = Logger.getLogger("SokobanSolver");
    private static Node solution = null;
    private static double timeElapsed;
/*
    Static method that acts as a façade between the client and the actual algorithms.
    It takes a GameBoard configured with the level to solve, and the strategy chosen by the client to solve it,
    and launches the search accordingly. If a solution is found, it acts upon the original GameBoard to show it.
*/
    public static void solve(GameBoard toSolve, Configuration configuration) throws InterruptedException, CloneNotSupportedException {

        solution = null;
        Long start;

        //Resetting the transposition table stored in the Node class just in case we are launching
        //a search on the same level in the same session of the program
        Node.resetSearchSpace();

        //Configuring components of the solver as the client asked
        Node.setExpansionScheme(configuration.getExpansionScheme());
        SokobanToolkit.setHeuristic(configuration.getHeuristic());
        DeadlockDetector.setRoutine(configuration.getRoutine());

        //Starting the clock to measure elapsed time
        start = Instant.now().toEpochMilli();

        //Precomputes dead positions before starting the search, if required
        if (configuration.getRoutine() == DDRoutine.ALL_ROUTINES || configuration.getRoutine() == DDRoutine.DEAD_POSITIONS)
            DeadlockDetector.handleDeadPositions((GameBoard) toSolve.clone());

        //Starting the search with the required algorithm
        switch (configuration.getStrategy()) {
            case BFS : {
                solution = BFS.launch((GameBoard) toSolve.clone());
                break;
            }
            case IDDFS : {
                solution = IDDFS.launch((GameBoard) toSolve.clone(), SokobanToolkit.estimateLowerBound(toSolve));
                break;
            }
            case IDASTAR : {
                solution = IDAStar.launch((GameBoard) toSolve.clone(), SokobanToolkit.estimateLowerBound(toSolve));
                break;
            }
            case SMASTAR : {
                solution = SMAStar.launch((GameBoard) toSolve.clone());
                break;
            }
        }
        timeElapsed = (double) (Instant.now().toEpochMilli() - start) / 1000;

        ArrayList<Action> solutionActions = new ArrayList<>();
        if (solution != null) solutionActions = solution.getActionHistory();

        //Showing the list of actions in the console and executing the corresponding moves on the board
        if (!solutionActions.isEmpty()) {
            log.info("Solution found in " + solutionActions.size() + " moves!");
            log.info(solution.getPushesNumber() + " pushes were needed");
            log.info("number of examined nodes: " + Node.getExaminedNodes());
            log.info("" + solution.getActionHistory());
            for (Action a : solutionActions) {
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
    
    public static ArrayList<Action> getSolution() {
        if (solution != null && solution.getActionHistory().size() > 0)
            return solution.getActionHistory();
        else
            return null;
    }

    public static int getSolutionMoves() {
        if (solution != null && solution.getActionHistory().size() > 0)
            return solution.getActionHistory().size();
        else
            return -1;
    }

    public static int getSolutionPushes() {
        if (solution != null && solution.getActionHistory().size() > 0)
            return solution.getPushesNumber();
        else
            return -1;
    }

    public static double getTimeElapsed() {
        return timeElapsed;
    }
}
