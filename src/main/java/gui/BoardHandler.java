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
import solver.DeadlockDetector;
import solver.Node;
import solver.SokobanSolver;
import solver.Transposer;
import solver.configuration.ExpansionScheme;
import solver.configuration.Strategy;

import java.util.concurrent.TransferQueue;
import java.util.logging.Logger;

// This class manages the creation and the updating of the game board during both search and manual play
public class BoardHandler {
    private static final Logger log = Logger.getLogger("Board");
    protected static Rectangle[][] tiles;
    protected static Image sokoban;
    protected static Image box;
    protected static Image wall;
    protected static Image goal;
    private static int moves, pushes;
    protected static boolean isShowing;
    protected static boolean isSearching;
    static {
        sokoban = new Image(MainMenu.class.getResourceAsStream("/sokoban.png"));
        box = new Image(MainMenu.class.getResourceAsStream("/cassa.png"));
        wall = new Image(MainMenu.class.getResourceAsStream("/wall.png"));
        goal = new Image(MainMenu.class.getResourceAsStream("/goal.png"));
    }

    // This method takes a GameBoard object and creates a GridPane with the initial content of the level 
    protected static GridPane createBoard(GameBoard game) {
        GridPane gameBoard = new GridPane();
        gameBoard.setAlignment(Pos.CENTER);
        gameBoard.setBackground(MainMenu.background);
        Cell[][] board = game.getBoard();
        tiles = new Rectangle[game.getRows()][game.getColumns()];

        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getColumns(); j++) {

                Rectangle tile = new Rectangle(MainMenu.scaleByResolution(50), MainMenu.scaleByResolution(50));

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
                    default: break;
                }

                tiles[i][j] = tile;
                gameBoard.add(tile, j, i);
            }
        }

        isSearching = true;
        isShowing = false;
        return gameBoard;
    }

    protected static void updateBoard (GameBoard game, Level level) {
        if ((!isSearching && !isShowing) || SokobanSolver.isInterrupted()) {
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

            //SokobanSolver has found a solution, that means we now need to show it by letting the update portion of
            //code run in this method
            isSearching = false;
            isShowing = true;
        }

        //this part concretely handles board updating by drawing the correct item for every cell of the GameBoard object
        //it only gets executed if we're showing a solution after finding it or if we're playing manually
        if (isShowing && !SokobanSolver.isInterrupted()) {
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
                        default: break;
                    }

                    tiles[i][j] = tile;
                }
            }
        }

        if (!MainMenu.manualGameplay && isShowing && !SokobanSolver.isInterrupted()) { //text to display when we're showing a solution
            Platform.runLater(() -> {
                //we show both moves and pushes in case we're expanding by pushes or we found a move optimal solution...
                if (SokobanSolver.getConfiguration().getExpansionScheme().equals(ExpansionScheme.PUSH_BASED) ||
                        level.getBestSolution() >= SokobanSolver.getSolutionMoves()) {

                    SolverView.result.setText(MainMenu.algorithmValue + " found a solution in " + moves + " moves - " + pushes + " pushes.\n\n" +
                            Transposer.getExaminedNodes() + " unique game states were examined.\n" +
                            "Time elapsed: " + SokobanSolver.getTimeElapsed() + " seconds\n" +
                            "Branches pruned by the Deadlock Detector: " + DeadlockDetector.getPrunedNodes() + "\n");
                }
                //...whereas in case we're expanding by moves and we didn't find a move optimal solution we just omit
                //the number of pushes, because as of now we cannot accurately get the number of pushes if we don't go through
                //the solution and manually count them, which is kind of a waste of time and code, since, in this case, we're
                //trying to optimize by pushes anyway
                else {
                    SolverView.result.setText(MainMenu.algorithmValue + " found a solution in " + moves + " moves \n\n" +
                            Transposer.getExaminedNodes() + " unique game states were examined.\n" +
                            "Time elapsed: " + SokobanSolver.getTimeElapsed() + " seconds\n" +
                            "Branches pruned by the Deadlock Detector: " + DeadlockDetector.getPrunedNodes() + "\n");
                }

                if (SokobanSolver.getSolution() == null) isShowing = false;
            });
        }
        else if (!MainMenu.manualGameplay && isSearching && !SokobanSolver.isInterrupted()) { //text to display when we're searching for a solution
                Platform.runLater(() -> {
                    String text = "Search in progress. The solution will be demonstrated after the computation.\n" +
                            "\nAlgorithm: " + MainMenu.algorithmValue +
                            "\nExpansion scheme: " + MainMenu.schemeValue;
                    if (MainMenu.algorithmValue != Strategy.mapStrategy(Strategy.BFS))
                        text += "\nHeuristic evaluation: " + MainMenu.heuristicValue + "\n\n";
                    else
                        text += "\n\n";

                    text = text + SokobanSolver.getLogLine();
                    SolverView.result.setText(text);
                });

        }

    }
}
