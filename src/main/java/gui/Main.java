package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import sokoban.game.Action;
import sokoban.game.Cell;
import sokoban.game.GameBoard;
import sokoban.solver.SokobanSolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Main extends Application {
    Logger log = Logger.getLogger("Main");
    private static Rectangle[][] tiles;
    private static GameBoard game;
    private static Image sokoban;
    private static Image box;
    private static Image wall;
    private static Image goal;
    static {
        try {
            sokoban = new Image(new FileInputStream("src/main/resources/sokoban.png"));
            box = new Image(new FileInputStream("src/main/resources/cassa.png"));
            wall = new Image(new FileInputStream("src/main/resources/wall.png"));
            goal = new Image(new FileInputStream("src/main/resources/goal.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        //Creating and configuring the scene for the menu
        ChoiceBox<Integer> choiceBox = new ChoiceBox<>();
        choiceBox.setValue(null);
        ObservableList<Integer> levels = choiceBox.getItems();
        for (int i = 1; i <= new File("levels").listFiles().length; i++) {
            levels.add(i);
        }
        choiceBox.setValue(levels.get(0));

        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(15);
        Label label1 = new Label("Select a level");
        TextField text = new TextField();
        text.setMaxWidth(100);
        Button button = new Button("Start");

        layout.getChildren().addAll(label1, choiceBox, button);
        Scene menu = new Scene(layout, 300, 300);

        //The button on the first scene triggers the switch to the gameplay scene and starts the sokoban.game
        button.setOnAction(e -> {
            //Creating, configuring and finally setting the scene for the sokoban.game itself
            game = new GameBoard(choiceBox.getValue());
            GridPane gameBoard = createBoard(game);
            Scene gameplay = new Scene(gameBoard);
            primaryStage.setResizable(false);
            primaryStage.setScene(gameplay);

            //Scheduling an update of the board every second
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(Main::updateBoard, 0, 1, TimeUnit.SECONDS);

            //Starting the thread that will execute the sokoban.solver and actually move sokoban on the board
            Thread t1 = new Thread(() -> {
                try {
                    SokobanSolver.solve(game);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            });
            t1.start();
        });

        //Closing the application when the window gets closed
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        //setting and showing the primary stage for the first start of the application
        primaryStage.setTitle("Sokoban");
        primaryStage.setScene(menu);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);

    }

    private static GridPane createBoard(GameBoard game) {
        GridPane gameBoard = new GridPane();
        //gameBoard.setBackground(new Background());
        Cell[][] board = game.getBoard();
        tiles = new Rectangle[game.getRows()][game.getColumns()];

        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getColumns(); j++) {

                Rectangle tile = new Rectangle(50, 50);

                switch (board[i][j].getContent()) {
                    case WALL : tile.setFill(new ImagePattern(wall));
                                break;
                    case EMPTY: if (board[i][j].isGoal()) tile.setFill(new ImagePattern(goal));
                                break;
                    case BOX:   tile.setFill(new ImagePattern(box));
                                break;
                    case SOKOBAN: tile.setFill(new ImagePattern(sokoban));
                                break;
                }

                //tile.setStroke(Color.DIMGREY);

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
                    case EMPTY: if (!board[i][j].isGoal()) tile.setFill(Color.WHITE);
                                else tile.setFill(new ImagePattern(goal));
                                break;
                    case BOX:   tile.setFill(new ImagePattern(box));
                                break;
                    case SOKOBAN: tile.setFill(new ImagePattern(sokoban));
                                break;
                }

                tiles[i][j] = tile;
            }
        }
    }

    private void playManually(Scene gameplay) {
        gameplay.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            switch (keyEvent.getCode()) {
                case UP : {
                    game.takeAction(Action.MOVE_UP);
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
        });
    }
}
