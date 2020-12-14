package gui;

import game.Action;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import game.Cell;
import game.GameBoard;
import game.Level;
import solver.DeadlockDetector;
import solver.configuration.*;
import solver.Node;
import solver.SokobanSolver;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/*
Main class of the JavaFX application. It configures the UI and takes the configuration of the solver from the user.
Not terribly interesting, lots of boilerplate code and drawing stuff. Plus it's an unholy spaghetti mess. Please
don't look at it. I don't really know JavaFX, I just improvised to get a gui out as fast as possible.
*/
public class Main extends Application {
    private static Logger log = Logger.getLogger("Main");
    private static boolean manualGameplay = false;
    private static GameBoard game;

    //Elements of the UI
    private static Rectangle[][] tiles;
    private static Image sokoban;
    private static Image box;
    private static Image wall;
    private static Image goal;
    private static Background background = new Background(new BackgroundFill(Color.rgb(54, 54, 54), null, null));
    static {
        sokoban = new Image(Main.class.getResourceAsStream("/sokoban.png"));
        box = new Image(Main.class.getResourceAsStream("/cassa.png"));
        wall = new Image(Main.class.getResourceAsStream("/wall.png"));
        goal = new Image(Main.class.getResourceAsStream("/goal.png"));
    }
    private static ChoiceBox<Integer> level = new ChoiceBox<>();
    private static ChoiceBox<String> algorithm = new ChoiceBox<>();
    private static ChoiceBox<String> scheme = new ChoiceBox<>();
    private static ChoiceBox<String> heuristic = new ChoiceBox<>();
    private static ChoiceBox<String> routine = new ChoiceBox<>();
    private static int levelValue;
    private static Scene menu;
    private static Scene gameScene;
    private static GridPane gameBoard;
    private static VBox boardLayout;
    private static Text result;

    //Variables for logging
    private static String algorithmValue;
    private static String schemeValue;
    private static String heuristicValue;
    private static String routineValue;
    private static boolean isSearching = false;
    private static boolean isShowing = false;
    private static int moves = 0;
    private static int pushes = 0;
    private static Text inGame;

    @Override
    public void start(Stage primaryStage) throws IOException {
        //Starting precomputation of the level-independent deadlocks as early as possible
        DeadlockDetector.populateDeadlocks();

        //Configuring level selection label and choicebox, and play manually button
        HBox levelContainer = new HBox();
        levelContainer.setAlignment(Pos.CENTER);
        levelContainer.setSpacing(30);
        Button button2 = new Button("Play manually");
        button2.setAlignment(Pos.BOTTOM_CENTER);
        button2.setOnAction(actionEvent -> {
            try {
                playManually();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });

        VBox levelSide = new VBox();
        Label label1 = new Label("Select a level");
        label1.setTextFill(Color.LIGHTGRAY);
        level.setValue(null);
        ObservableList<Integer> levels = level.getItems();
        for (int i = 1; i <= Level.NUM_LEVELS; i++) {
            levels.add(i);
        }
        level.setValue(levels.get(0));
        levelSide.setSpacing(15);
        levelSide.setAlignment(Pos.CENTER);
        levelSide.getChildren().addAll(label1, level);
        levelContainer.getChildren().addAll(levelSide, button2);

        //Configuring algorithm selection label and choicebox
        VBox algorithmSide = new VBox();
        Label label2 = new Label("Select an algorithm");
        label2.setTextFill(Color.LIGHTGRAY);
        algorithm.setValue(null);
        ObservableList<String> strategies = algorithm.getItems();
        List<String> finalNames = new ArrayList<>();
        for (Strategy strategy : Arrays.asList(Strategy.values())) {
            finalNames.add(Strategy.mapStrategy(strategy));
        }
        strategies.addAll(finalNames);
        algorithm.setValue(strategies.get(0));
        algorithmSide.setSpacing(15);
        algorithmSide.setAlignment(Pos.CENTER);
        algorithmSide.getChildren().addAll(label2, algorithm);

        //Configuring expansion scheme selection label and choicebox
        VBox expSide = new VBox();
        Label label3 = new Label("Select a node expansion scheme");
        label3.setTextFill(Color.LIGHTGRAY);
        scheme.setValue(null);
        ObservableList<String> schemes = scheme.getItems();
        List<String> schemeNames = new ArrayList<>();
        for (ExpansionScheme e : Arrays.asList(ExpansionScheme.values())) {
            schemeNames.add(ExpansionScheme.mapExpansionScheme(e));
        }
        schemes.addAll(schemeNames);
        scheme.setValue(schemeNames.get(0));
        expSide.setSpacing(15);
        expSide.setAlignment(Pos.CENTER);
        expSide.getChildren().addAll(label3, scheme);

        //Configuring heuristics selection label and choicebox
        VBox heuristicSide = new VBox();
        Label label4 = new Label("Select an evaluation heuristic");
        label4.setTextFill(Color.LIGHTGRAY);
        heuristic.setValue(null);
        ObservableList<String> heuristics = heuristic.getItems();
        List<String> heuristicNames = new ArrayList<>();
        for (Heuristic h : Arrays.asList(Heuristic.values())) {
            heuristicNames.add(Heuristic.mapHeuristic(h));
        }
        heuristics.addAll(heuristicNames);
        heuristic.setValue(heuristicNames.get(0));
        heuristicSide.setSpacing(15);
        heuristicSide.setAlignment(Pos.CENTER);
        heuristicSide.getChildren().addAll(label4, heuristic);

        //Configuring deadlock detection routine selection label and choicebox
        VBox ddSide = new VBox();
        Label label5 = new Label("Select a deadlock detection routine");
        label5.setTextFill(Color.LIGHTGRAY);
        routine.setValue(null);
        ObservableList<String> routines = routine.getItems();
        List<String> routineNames = new ArrayList<>();
        for (DDRoutine d : Arrays.asList(DDRoutine.values())) {
            routineNames.add(DDRoutine.mapDDRoutine(d));
        }
        routines.addAll(routineNames);
        routine.setValue(routineNames.get(0));
        ddSide.setSpacing(15);
        ddSide.setAlignment(Pos.CENTER);
        ddSide.getChildren().addAll(label5, routine);

        //Configuring start button
        Button button = new Button("Start computation");
        button.setBackground(new Background(new BackgroundFill(Color.TOMATO, null, null)));
        button.setPrefSize(150, 30);



        //Configuring main layout and adding it to the menu scene
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        layout.setBackground(background);
        layout.setSpacing(50);
        layout.getChildren().addAll(levelContainer, algorithmSide, expSide, heuristicSide, ddSide, button);
        menu = new Scene(layout, 600, 700);

        //The button on the first scene triggers the switch to the gameplay scene and starts the game
        button.setOnAction(actionEvent -> {configureGame(primaryStage);});

        //Closing the application when the window gets closed
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        //setting and showing the primary stage for the first start of the application
        primaryStage.setTitle("SokoBOT");
        primaryStage.setScene(menu);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void configureGame(Stage primaryStage) {

        algorithmValue = algorithm.getValue();
        heuristicValue = heuristic.getValue();
        schemeValue = scheme.getValue();
        levelValue = level.getValue();
        routineValue = routine.getValue();

        manualGameplay = false;
        boardLayout = new VBox();
        boardLayout.setBackground(background);
        boardLayout.setAlignment(Pos.TOP_CENTER);
        boardLayout.setSpacing(15);

        Level toLoad = new Level(levelValue);
        Label label1 = new Label("Requires " + toLoad.getBestSolution() + " moves and " +
                toLoad.getMinPushes() + " pushes");
        label1.setMaxHeight(60);
        label1.setMinHeight(60);
        label1.setTextFill(Color.LIGHTGRAY);
        label1.setAlignment(Pos.CENTER);
        result = new Text("");
        result.setFill(Color.LIGHTGRAY);

        //Creating, configuring and finally setting the scene for the game itself
        game = new GameBoard(toLoad);
        gameBoard = createBoard(game);

        boardLayout.getChildren().addAll(label1, gameBoard, result);

        gameScene = new Scene(boardLayout, 1100, 700);
        Stage gameStage = new Stage();
        gameStage.setScene(gameScene);
        gameStage.setTitle("SokoBOT - Level " + level.getValue());
        gameStage.show();

        //Starting the thread that will execute the sokoban solver and actually move sokoban on the board
        Thread t1 = new Thread(() -> {
            try {
                //Launching the solver with the configuration specified by the UI elements
                SokobanSolver.solve(game, Configuration.getInstance(schemeValue, algorithmValue, heuristicValue, routineValue));
            } catch (InterruptedException | CloneNotSupportedException e1) {
                e1.printStackTrace();
            }
        });
        t1.start();

        //Periodic refresh of the board to show selected moves and update the text elements
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(Main::updateBoard, 0, 200, TimeUnit.MILLISECONDS);

        //You can click on the board to get back to the menu
        gameBoard.setOnMouseClicked(keyEvent -> {
            SokobanSolver.setSolution(null);
            gameStage.close();
        });
    }

    private static GridPane createBoard(GameBoard game) {
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

    private static void updateBoard () {
        if (!isSearching && !isShowing) {
            return;
        }

        Cell[][] board = game.getBoard();
        String text;

        if (SokobanSolver.getSolution() != null) {
            moves = SokobanSolver.getSolutionMoves();
            pushes = SokobanSolver.getSolutionPushes();
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

        if (!manualGameplay && isShowing) {
            result.setText(algorithmValue + " found a solution in " + moves + " moves - " + pushes + " pushes.\n\n" +
                    Node.getExaminedNodes() + " unique game states were examined.\n" +
                    "Time elapsed: " + SokobanSolver.getTimeElapsed() + " seconds\n" +
                    "Branches pruned by the Deadlock Detector: " + DeadlockDetector.getPrunedNodes() +
                    "\nVisited nodes: " + Node.getExaminedNodes() + "\n\n");
            if (SokobanSolver.getSolution() == null) isShowing = false;
        }
        else if (!manualGameplay && isSearching) {
            text = "Search in progress. The solution will be demonstrated after the computation.\n" +
                    "\nAlgorithm: " + algorithmValue +
                    "\nExpansion scheme: " + schemeValue;
            if (algorithm.getValue() != Strategy.mapStrategy(Strategy.BFS))
                text += "\nHeuristic evaluation: " + heuristicValue + "\n\n";
            else
                text += "\n\n";

            if (SokobanSolver.getLogLine() != null)
                text = text + SokobanSolver.getLogLine();
            result.setText(text);
        }

    }

    private static void playManually() throws CloneNotSupportedException {
        manualGameplay = true;
        boardLayout = new VBox();
        boardLayout.setBackground(background);
        boardLayout.setAlignment(Pos.CENTER);
        boardLayout.setSpacing(15);
        isShowing = true;
        moves = 0;

        Level toLoad = new Level(level.getValue());

        game = new GameBoard(toLoad);
        gameBoard = createBoard(game);
        inGame = new Text();
        inGame.setText("Moves: " + moves + "\n\n\n");
        inGame.setFill(Color.LIGHTGRAY);
        boardLayout.getChildren().addAll(gameBoard, inGame);
        gameScene = new Scene(boardLayout);

        Stage gameStage = new Stage();
        gameStage.setScene(gameScene);
        gameStage.show();

        DeadlockDetector.setRoutine(DDRoutine.ALL_ROUTINES);
        DeadlockDetector.handleDeadPositions((GameBoard) game.clone());

        gameStage.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            switch (keyEvent.getCode()) {
                case UP : {
                    try {
                        if (DeadlockDetector.getPrunedNodes()==0 && !game.checkVictory()) {
                            if (game.takeAction(Action.MOVE_UP));
                                moves++;
                        }
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    updateBoard();
                    break;
                }
                case DOWN : {
                    try {
                        if (DeadlockDetector.getPrunedNodes()==0 && !game.checkVictory()) {
                            if (game.takeAction(Action.MOVE_DOWN));
                            moves++;
                        }
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    updateBoard();
                    break;
                }
                case RIGHT : {
                    try {
                        if (DeadlockDetector.getPrunedNodes()==0 && !game.checkVictory()) {
                            if (game.takeAction(Action.MOVE_RIGHT));
                            moves++;
                        }
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    updateBoard();
                    break;
                }
                case LEFT : {
                    try {
                        if (DeadlockDetector.getPrunedNodes()==0 && !game.checkVictory()) {
                            if (game.takeAction(Action.MOVE_LEFT));
                            moves++;
                        }
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    updateBoard();
                    break;
                }
                case R : {
                    DeadlockDetector.setPrunedNodes(0);
                    gameStage.close();
                    try {
                        playManually();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
                default : break;
            }

            if (DeadlockDetector.getPrunedNodes()>0)
                inGame.setText("\nYou reached a DEADLOCK!\nPress R to restart the game\n");
            else
                inGame.setText("Moves: " + moves + "\n\n\n");

            //checking for victory after every action taken
            if (game.checkVictory()) {
                inGame.setText("Moves: " + moves + "\n\nVICTORY! Press R to restart the game\n");
                SokobanSolver.setSolution(null);
            }
        });
    }
}
