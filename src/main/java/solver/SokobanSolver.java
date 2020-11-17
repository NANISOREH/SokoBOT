package solver;

import sokoban.Action;
import sokoban.Cell;
import sokoban.CellContent;
import sokoban.GameBoard;

import java.util.logging.Logger;

public class SokobanSolver {
    private static Logger logger = Logger.getLogger("SokobanSolver");
    private static GameBoard game;

    public static void solve(GameBoard toSolve) throws InterruptedException {
        game = toSolve;
        mockSolver();
    }

    int a = (int) (4 * Math.random());

/*
    Temporary mock solver created for testing purposes
*/
    private static void mockSolver() throws InterruptedException {
        Cell sokobanCell;
        int random = 0;
        while (true) {
            sokobanCell = game.getSokobanCell();
            random = (int) (4 * Math.random());

            switch (random) {
                case 0 : {
                    if (game.getNorth(sokobanCell).getContent() == CellContent.EMPTY) {
                        game.takeAction(Action.MOVE_UP);
                        Thread.sleep(1000);
                    }
                    break;
                }
                case 1 : {
                    if (game.getEast(sokobanCell).getContent() == CellContent.EMPTY) {
                        game.takeAction(Action.MOVE_RIGHT);
                        Thread.sleep(1000);
                    }
                    break;
                }
                case 2 : {
                    if (game.getSouth(sokobanCell).getContent() == CellContent.EMPTY) {
                        game.takeAction(Action.MOVE_DOWN);
                        Thread.sleep(1000);
                    }
                    break;
                }
                case 3 : {
                    if (game.getWest(sokobanCell).getContent() == CellContent.EMPTY) {
                        game.takeAction(Action.MOVE_LEFT);
                        Thread.sleep(1000);
                    }
                    break;
                }
            }

        }

    }
}
