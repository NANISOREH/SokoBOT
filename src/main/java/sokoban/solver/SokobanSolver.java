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
            case MOCK -> {
                mockSolver(toSolve);
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

/*
    Mock sokoban solver created for testing purposes.
    It just randomly moves on free tiles.
*/
    private static void mockSolver(GameBoard toSolve) throws InterruptedException, CloneNotSupportedException {
        Cell sokobanCell;
        int random = 0;
        while (true) {
            sokobanCell = toSolve.getSokobanCell();
            random = (int) (4 * Math.random());

            switch (random) {
                case 0 : {
                    if (toSolve.getNorth(sokobanCell).getContent() == CellContent.EMPTY) {
                        toSolve.takeAction(Action.MOVE_UP);
                        Thread.sleep(500);
                    }
                    break;
                }
                case 1 : {
                    if (toSolve.getEast(sokobanCell).getContent() == CellContent.EMPTY) {
                        toSolve.takeAction(Action.MOVE_RIGHT);
                        Thread.sleep(500);
                    }
                    break;
                }
                case 2 : {
                    if (toSolve.getSouth(sokobanCell).getContent() == CellContent.EMPTY) {
                        toSolve.takeAction(Action.MOVE_DOWN);
                        Thread.sleep(500);
                    }
                    break;
                }
                case 3 : {
                    if (toSolve.getWest(sokobanCell).getContent() == CellContent.EMPTY) {
                        toSolve.takeAction(Action.MOVE_LEFT);
                        Thread.sleep(500);
                    }
                    break;
                }
            }

        }

    }
}
