package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import sokoban.game.Action;
import sokoban.game.Cell;
import sokoban.game.GameBoard;
import sokoban.game.Level;
import sokoban.solver.Node;
import sokoban.solver.SokobanSolver;
import sokoban.solver.Strategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
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
    private static Background background = new Background(new BackgroundFill(Color.rgb(54, 54, 54), null, null));
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
    private static ChoiceBox<Integer> level = new ChoiceBox<>();
    private static ChoiceBox<Strategy> algorithm = new ChoiceBox<>();
    private static Scene menu;
    private static Scene gameScene;
    private static GridPane gameBoard;
    private static VBox boardLayout;
    private static Text result;



    @Override
    public void start(Stage primaryStage) {

        //Creating and configuring the scene for the menu
        level.setValue(null);
        ObservableList<Integer> levels = level.getItems();
        for (int i = 1; i <= new File("levels").listFiles().length; i++) {
            levels.add(i);
        }
        level.setValue(levels.get(0));

        algorithm.setValue(null);
        ObservableList<Strategy> strategies = algorithm.getItems();
        strategies.addAll(Arrays.asList(Strategy.values()));
        algorithm.setValue(strategies.get(0));

        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        layout.setBackground(background);
        layout.setSpacing(15);
        Label label1 = new Label("Select a level");
        label1.setTextFill(Color.LIGHTGRAY);
        Label label2 = new Label("Select an algorithm");
        label2.setTextFill(Color.LIGHTGRAY);
        TextField text = new TextField();
        text.setMaxWidth(100);
        Button button = new Button("Start");

        layout.getChildren().addAll(label1, level, label2, algorithm, button);
        menu = new Scene(layout, 800, 680);

        //The button on the first scene triggers the switch to the gameplay scene and starts the sokoban.game
        button.setOnAction(actionEvent -> {configureGame(primaryStage);});

        //Closing the application when the window gets closed
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });


        //setting and showing the primary stage for the first start of the application
        primaryStage.setTitle("Sokoban");
        primaryStage.setScene(menu);
        //primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);

    }

    private void configureGame(Stage primaryStage) {

        boardLayout = new VBox();
        boardLayout.setBackground(background);
        boardLayout.setAlignment(Pos.CENTER);
        boardLayout.setSpacing(15);

        Level toLoad = new Level(level.getValue());
        Label label1 = new Label("Level" + level.getValue() + "   -   Requires " + toLoad.getBestSolution() + " steps");
        label1.setTextFill(Color.LIGHTGRAY);
        result = new Text("Search in progress...");
        result.setFill(Color.LIGHTGRAY);

        //Creating, configuring and finally setting the scene for the game itself
        game = new GameBoard(toLoad);
        gameBoard = createBoard(game);
        gameScene = new Scene(gameBoard);
        //primaryStage.setResizable(false);
        boardLayout.getChildren().addAll(label1, gameBoard, result);
        primaryStage.setScene(new Scene(boardLayout, 800, 680));
        primaryStage.setResizable(false);

        //Starting the thread that will execute the sokoban solver and actually move sokoban on the board
        Thread t1 = new Thread(() -> {
            try {
                SokobanSolver.solve(game, algorithm.getValue());
            } catch (InterruptedException | CloneNotSupportedException e1) {
                e1.printStackTrace();
            }
        });
        t1.start();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(Main::updateBoard, 0, 200, TimeUnit.MILLISECONDS);

        //You can click on the board to get back to the menu
        gameBoard.setOnMouseClicked(keyEvent -> {
            try {
                t1.join(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            primaryStage.setScene(menu);
        });
    }

    private static GridPane createBoard(GameBoard game) {
        GridPane gameBoard = new GridPane();
        gameBoard.setAlignment(Pos.CENTER);
        gameBoard.setBackground(background);
        //gameBoard.setBackground(new Background());
        Cell[][] board = game.getBoard();
        tiles = new Rectangle[game.getRows()][game.getColumns()];

        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getColumns(); j++) {

                Rectangle tile = new Rectangle(60, 60);

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

        if (SokobanSolver.getSolution() != null) {
            result.setText(algorithm.getValue() + " found a solution in " + SokobanSolver.getSolution().size() + " steps" +
                    " after examining " + Node.getExaminedNodes() + " unique game states");
        }

    }

    /*private void playManually(Scene gameplay) {
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
    }*/
}
