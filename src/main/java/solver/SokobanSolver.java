package solver;

import game.Action;
import game.GameBoard;
import game.Level;
import solver.algorithms.*;
import solver.configuration.Configuration;
import solver.configuration.DDRoutine;
import solver.configuration.ExpansionScheme;

import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;

/*
    Actor class that solves a sokoban puzzle.
    This class's responsibility is to take "orders" from the client, executing the preliminary operations for the algorithm's
    execution, launching the algorithm itself and finally distributing the solution.
*/

public class SokobanSolver {
    private static Logger log = Logger.getLogger("SokobanSolver");
    private static Node solution = null;
    private static double timeElapsed;
    private static String logLine;
    private static Configuration configuration;
    private static boolean interrupted;
/*
    Static method that acts as a façade between the client and the actual algorithms.
    It takes a GameBoard configured with the level to solve, and the strategy chosen by the client to solve it,
    then configures and launches the search accordingly.
    If a solution is found, it acts upon the original GameBoard to show it.
*/
    public static void solve(GameBoard toSolve, Configuration c) throws InterruptedException, CloneNotSupportedException {

        configuration = c;
        logLine = "\n\n";
        solution = null;
        interrupted = false;
        Long start;

        //Resetting the transposition table stored in the Node class just in case we are launching
        //a search on the same level in the same session of the program
        Transposer.resetSearchSpace();

        //Configuring components of the solver as the client asked
        Node.setExpansionScheme(configuration.getExpansionScheme());
        SokobanToolkit.setHeuristic(configuration.getHeuristic());
        DeadlockDetector.setRoutine(configuration.getRoutine());
        DeadlockDetector.setPrunedNodes(0);

        //Starting the clock to measure elapsed time
        start = Instant.now().toEpochMilli();

        //Precomputes dead positions before starting the search, if required
        if (configuration.getRoutine() == DDRoutine.ALL_ROUTINES || configuration.getRoutine() == DDRoutine.DEAD_POSITIONS) {
            DeadlockDetector.handleDeadPositions((GameBoard) toSolve.clone());
            logLine = "\n\nComputing dead positions";
        }

        //Starting the search with the required algorithm
        Algorithm chosen = Algorithm.getInstance(configuration.getStrategy());
        solution = chosen.launch((GameBoard) toSolve.clone());

        //Stopping the clock
        timeElapsed = (double) (Instant.now().toEpochMilli() - start) / 1000;

        //Showing the list of actions in the console and executing the corresponding moves on the board
        ArrayList<Action> solutionActions = new ArrayList<>();
        if (solution != null) solutionActions = solution.getActionHistory();
        if (!solutionActions.isEmpty()) {
            logLine = "";
            log.info("Solution found in " + solutionActions.size() + " moves!");
            log.info("number of examined nodes: " + Transposer.getExaminedNodes());
            log.info("number of nodes pruned by DeadlockDetector: " + DeadlockDetector.getPrunedNodes());
            log.info("" + solution.getActionHistory());
            DeadlockDetector.setRoutine(DDRoutine.NO_DEADLOCK_DETECTION);
            for (Action a : solutionActions) {
                //we execute every action in the solution: the board will automagically solve the puzzle as a result
                toSolve.takeAction(a);
                Thread.sleep(200);
            }
        }
        else {
            log.info("Sorry, no solution was found!");
            log.info("number of examined nodes: " + Transposer.getExaminedNodes());
        }
    }

/*
    Obtains the list of actions in the solution node, or null if there's no solution available
*/
    public static ArrayList<Action> getSolution() {
        if (solution != null && solution.getActionHistory().size() > 0)
            return solution.getActionHistory();
        else
            return null;
    }

/*
    Obtains the number of moves required by the solution, or a negative value if there's no solution available
*/
    public static int getSolutionMoves() {
        if (solution != null && solution.getActionHistory().size() > 0)
            return solution.getActionHistory().size();
        else
            return -1;
    }

/*
    Obtains the number of pushes required by the solution, or a negative value if there's no solution available
*/
    public static int getSolutionPushes() {
        if (solution != null && solution.getActionHistory().size() > 0) {
            return solution.getPathCost();
        }
        else
            return -1;
    }

/*
    Obtains the optimal number of moves or pushes required by the level that we're solving
*/
    public static int getOptimalSolution() {
        Level l = configuration.getLevel();
        if (configuration.getExpansionScheme() == ExpansionScheme.MOVE_BASED) {
            return l.getBestSolution();
        }
        else
            return l.getMinPushes();
    }

    public static void interrupt() {
        interrupted = true;
    }

    //A bunch of standard getters and setters

    public static void setSolution(Node solution) {
        SokobanSolver.solution = solution;
    }

    public static double getTimeElapsed() {
        return timeElapsed;
    }

    public static String getLogLine() {
        return logLine;
    }

    public static void setLogLine(String logLine) {
        SokobanSolver.logLine = logLine;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static boolean isInterrupted() {
        return interrupted;
    }
}
