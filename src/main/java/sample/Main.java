package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.w3c.dom.Text;
import sokoban.Action;
import sokoban.Cell;
import sokoban.CellContent;
import sokoban.GameBoard;
import solver.SokobanSolver;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends Application {
    private static Rectangle[][] tiles;
    private static GameBoard game = new GameBoard(1);

    @Override
    public void start(Stage primaryStage) throws Exception{

        //Creating a board view
        GridPane gameBoard = createBoard(game);

        //Creating a scene object
        Scene scene = new Scene(gameBoard);
        //Setting title to the Stage
        primaryStage.setTitle("Sokoban");
        //Adding scene to the stage
        primaryStage.setScene(scene);

        //setting a listener for certain keypresses, triggering the actions in the game
/*        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            switch (keyEvent.getCode()) {
                case UP : {
                    //game.takeAction(Action.MOVE_UP);
                    updateBoard();
                    break;
                }
                case DOWN : {
                    game.takeAction(Action.MOVE_DOWN);
                    updateBoard();
                    break;
                }
                case RIGHT : {
                    game.takeAction(Action.MOVE_RIGHT);
                    updateBoard();
                    break;
                }
                case LEFT : {
                    game.takeAction(Action.MOVE_LEFT);
                    updateBoard();
                    break;
                }
                default : break;
            }

            //checking for victory after every action taken
            if (game.checkVictory()) {
                System.out.println("VICTORY!");
                try {
                    stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(Main::updateBoard, 0, 1, TimeUnit.SECONDS);

        //Displaying the contents of the stage
        primaryStage.show();

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SokobanSolver.solve(game);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
    }

    public static void main(String[] args) {
        launch(args);

    }

    private static GridPane createBoard(GameBoard game) {
        GridPane gameBoard = new GridPane();
        Cell[][] board = game.getBoard();
        tiles = new Rectangle[game.getRows()][game.getColumns()];

        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getColumns(); j++) {

                Rectangle tile = new Rectangle(50, 50);

                switch (board[i][j].getContent()) {
                    case WALL : tile.setFill(Color.SLATEGRAY);
                        break;
                    case EMPTY: if (!board[i][j].isGoal()) tile.setFill(Color.BEIGE);
                                else tile.setFill(Color.TOMATO);
                        break;
                    case GOAL:  tile.setFill(Color.TOMATO);
                        break;
                    case BOX:   tile.setFill(Color.YELLOW);
                        break;
                    case SOKOBAN: tile.setFill(Color.BLUE);
                        break;
                }

                tile.setStroke(Color.BLACK);

                tiles[i][j] = tile;
                gameBoard.add(tile, j, i);
            }
        }

        return gameBoard;
    }

    private static void updateBoard () {
        Cell[][] board = game.getBoard();
        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getColumns(); j++) {

                Rectangle tile = tiles[i][j];

                switch (board[i][j].getContent()) {
                    case WALL : tile.setFill(Color.SLATEGRAY);
                        break;
                    case EMPTY: if (!board[i][j].isGoal()) tile.setFill(Color.BEIGE);
                                else tile.setFill(Color.TOMATO);
                        break;
                    case BOX:   tile.setFill(Color.YELLOW);
                        break;
                    case SOKOBAN: tile.setFill(Color.BLUE);
                        break;
                }

                tiles[i][j] = tile;
            }
        }
    }
}
