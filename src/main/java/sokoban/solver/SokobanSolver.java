package sokoban.solver;

import sokoban.game.Action;
import sokoban.game.Cell;
import sokoban.game.CellContent;
import sokoban.game.GameBoard;
import sokoban.solver.algorithms.IDBFS;
import sokoban.solver.algorithms.IDDFS;

import java.util.ArrayList;
import java.util.logging.Logger;

public class SokobanSolver {
    private static Logger logger = Logger.getLogger("SokobanSolver");
    private static ArrayList<Action> solution = null;

/*
    Static method that acts as a façade between the client and the actual algorithms.
    It takes a GameBoard configured with the level to solve, and the strategy chosen by the client to solve it,
    and launches the search accordingly. If a solution is found, it acts upon the original GameBoard to show it.
*/
    public static void solve(GameBoard toSolve, Strategy strategy) throws InterruptedException, CloneNotSupportedException {

        switch (strategy) {
            case IDBFS -> {
                solution = IDBFS.launch((GameBoard) toSolve.clone());
                break;
            }
            case IDDFS -> {
                solution = IDDFS.launch((GameBoard) toSolve.clone());
                break;
            }

        }

        if (solution != null) {
            logger.info("solution found in " + solution.size() + " moves!");
            logger.info("" + solution);
            for (Action a : solution) {
                //we execute every action in the solution: the board will automagically solve the puzzle as a result
                toSolve.takeAction(a);
                Thread.sleep(200);
            }
        }
        else
            logger.info("solution not found");
    }

}
