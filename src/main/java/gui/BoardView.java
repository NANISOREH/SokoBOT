package gui;

import game.Cell;
import game.GameBoard;
import game.Level;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import solver.DeadlockDetector;
import solver.Node;
import solver.SokobanSolver;
import solver.configuration.Strategy;

import java.util.logging.Logger;

public class BoardView {
    private static final Logger log = Logger.getLogger("Board");
    protected static Rectangle[][] tiles;
    protected static Image sokoban;
    protected static Image box;
    protected static Image wall;
    protected static Image goal;
    private static int moves, pushes;
    protected static boolean isShowing;
    protected static boolean isSearching;

    protected static Background background = new Background(new BackgroundFill(Color.rgb(54, 54, 54), null, null));
    static {
        sokoban = new Image(MainMenu.class.getResourceAsStream("/sokoban.png"));
        box = new Image(MainMenu.class.getResourceAsStream("/cassa.png"));
        wall = new Image(MainMenu.class.getResourceAsStream("/wall.png"));
        goal = new Image(MainMenu.class.getResourceAsStream("/goal.png"));
    }


    protected static GridPane createBoard(GameBoard game) {
        GridPane gameBoard = new GridPane();
        gameBoard.setAlignment(Pos.CENTER);
        gameBoard.setBackground(background);
        Cell[][] board = game.getBoard();
        tiles = new Rectangle[game.getRows()][game.getColumns()];

        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getColumns(); j++) {

                Rectangle tile = new Rectangle(50, 50);

                switch (board[i][j].getContent()) {
                    case WALL : tile.setFill(new ImagePattern(wall));
                        break;
                    case EMPTY: if (!board[i][j].isGoal()) tile.setFill(Color.WHITE);
                    else tile.setFill(new ImagePattern(goal));
                        break;
                    case BOX:   tile.setFill(new ImagePattern(box));
                        break;
                    case SOKOBAN: tile.setFill(new ImagePattern(sokoban));
                        break;
                }

                tiles[i][j] = tile;
                gameBoard.add(tile, j, i);
            }
        }

        isSearching = true;
        return gameBoard;
    }

    protected static void updateBoard (GameBoard game, Level level) {
        if (!isSearching && !isShowing) {
            return;
        }

        Cell[][] board = game.getBoard();

        if (SokobanSolver.getSolution() != null) {
            moves = SokobanSolver.getSolutionMoves();
            pushes = SokobanSolver.getSolutionPushes();

            //this is necessary because when expanding by moves i can't get the exact number of pushes without going
            //through the solution and counting them, but if the solution was move-optimal, it was also push-optimal,
            //so we can just go ahead and use the minimum number of pushes stored into the Level object
            if (level.getBestSolution() >= SokobanSolver.getSolutionMoves())
                pushes = level.getMinPushes();

            isSearching = false;
            isShowing = true;
        }

        if (isShowing) {
            for (int i = 0; i < game.getRows(); i++) {
                for (int j = 0; j < game.getColumns(); j++) {

                    Rectangle tile = tiles[i][j];

                    switch (board[i][j].getContent()) {
                        case EMPTY:
                            if (!board[i][j].isGoal()) tile.setFill(Color.WHITE);
                            else tile.setFill(new ImagePattern(goal));
                            break;
                        case BOX:
                            tile.setFill(new ImagePattern(box));
                            break;
                        case SOKOBAN:
                            tile.setFill(new ImagePattern(sokoban));
                            break;
                    }

                    tiles[i][j] = tile;
                }
            }
        }

        if (!MainMenu.manualGameplay && isShowing) {
            SolverView.back.setDisable(false);
            SolverView.back.setOpacity(100);
            SolverView.result.setText(MainMenu.algorithmValue + " found a solution in " + moves + " moves - " + pushes + " pushes.\n\n" +
                    Node.getExaminedNodes() + " unique game states were examined.\n" +
                    "Time elapsed: " + SokobanSolver.getTimeElapsed() + " seconds\n" +
                    "Branches pruned by the Deadlock Detector: " + DeadlockDetector.getPrunedNodes());
            if (SokobanSolver.getSolution() == null) isShowing = false;
        }
        else if (!MainMenu.manualGameplay && isSearching) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        String text = "Search in progress. The solution will be demonstrated after the computation.\n" +
                                "\nAlgorithm: " + MainMenu.algorithmValue +
                                "\nExpansion scheme: " + MainMenu.schemeValue;
                        if (MainMenu.algorithmValue != Strategy.mapStrategy(Strategy.BFS))
                            text += "\nHeuristic evaluation: " + MainMenu.heuristicValue + "\n\n";
                        else
                            text += "\n\n";

                        text = text + SokobanSolver.getLogLine();
                        SolverView.result.setText(text);
                    }
                });

        }

    }
}
