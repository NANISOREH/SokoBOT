package gui;

import game.GameBoard;
import game.Level;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import solver.SokobanSolver;
import solver.configuration.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
This class configures the UI of the solver part of the program.
It starts both the actual solver instance and the board drawing methods.
*/
public class SolverView {
    private static Level toLoad;
    private static GameBoard game;
    private static VBox boardLayout;
    protected static Text result;
    protected static Button back;

    protected static void start(Stage primaryStage) {

        toLoad = new Level(MainMenu.levelValue);
        MainMenu.manualGameplay = false;
        SokobanSolver.setSolution(null);

        boardLayout = new VBox();
        boardLayout.setBackground(MainMenu.background);
        boardLayout.setAlignment(Pos.CENTER);
        boardLayout.setSpacing(15);

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
        GridPane gameBoard = BoardHandler.createBoard(game);

        //Configuring back button
        back = new Button("Go back to Menu");
        back.setBackground(new Background(new BackgroundFill(Color.TOMATO, null, null)));
        back.setPrefSize(150, 30);
        back.setAlignment(Pos.BOTTOM_CENTER);
        back.setOpacity(0);
        back.setDisable(true);

        boardLayout.getChildren().addAll(label1, gameBoard, result, back);
        Scene gameScene = new Scene(boardLayout, 1100, 840);

        primaryStage.close();
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("SokoBOT - Level " + MainMenu.levelValue);
        primaryStage.show();

        //Starting the thread that will execute the sokoban solver and actually move sokoban on the board
        Thread t1 = new Thread(() -> {
            try {
                //Launching the solver with the configuration specified by the UI elements
                game = new GameBoard(toLoad);
                SokobanSolver.solve(game, Configuration.getInstance(MainMenu.schemeValue, MainMenu.algorithmValue,
                        MainMenu.heuristicValue, MainMenu.routineValue));
            } catch (InterruptedException | CloneNotSupportedException e1) {
                e1.printStackTrace();
            }
        });
        t1.start();

        //Periodic refresh of the board to show selected moves and update the text elements
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                BoardHandler.updateBoard(game, toLoad);
            }
        }, 0, 200, TimeUnit.MILLISECONDS);

        //You can click on the button to get back to the menu
        back.setOnMouseClicked(keyEvent -> {
            BoardHandler.isShowing = false;
            BoardHandler.isSearching = false;
            game = null;
            result = null;
            SokobanSolver.setSolution(null);
            primaryStage.close();
            primaryStage.setTitle("SokoBOT");
            primaryStage.setScene(MainMenu.menu);
            primaryStage.show();
        });
    }
}
