package gui;

import game.GameBoard;
import game.Level;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
    protected static Text result;
    protected static Button back;
    protected static Image arrow = new Image(MainMenu.class.getResourceAsStream("/back.png"));

    protected static void start(Stage primaryStage) {

        toLoad = new Level(MainMenu.levelValue);
        MainMenu.manualGameplay = false;
        SokobanSolver.setSolution(null);

        //root layout
        VBox boardLayout = new VBox();
        boardLayout.setBackground(MainMenu.background);

        //Configuring top label
        Label label1 = new Label("Requires " + toLoad.getBestSolution() + " moves and " +
                toLoad.getMinPushes() + " pushes");
        label1.setTextFill(Color.LIGHTGRAY);
        label1.setTextAlignment(TextAlignment.CENTER);

        //Configuring back button
        ImageView iv = new ImageView(arrow);
        iv.setFitHeight(50);
        iv.setFitWidth(50);
        iv.setPreserveRatio(true);
        back = new Button();
        back.setGraphic(iv);
        back.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(100), null)));
        back.setMaxSize(50,50);
        back.setPrefSize(50,50);
        back.setPadding(new Insets(0,0,0,0));

        back.addEventHandler(MouseEvent.MOUSE_ENTERED,
            e -> back.setBackground(
                new Background(
                        new BackgroundFill(Color.rgb(50, 50, 50), new CornerRadii(200), null)
                    )
                )
            );

        back.addEventHandler(MouseEvent.MOUSE_EXITED,
            e -> back.setBackground(
                    new Background(
                            new BackgroundFill(Color.TRANSPARENT, new CornerRadii(100), null)
                    )
                )
            );

        //Dummy right element to center the label in the BorderPane
        HBox dummy = new HBox();
        dummy.setPrefSize(50,50);

        //BorderPane to contain the elments at the top
        BorderPane top = new BorderPane();
        top.setLeft(back);
        top.setCenter(label1);
        top.setRight(dummy);
        top.setPadding(new Insets(10,5,10,5));

        //Creating the board
        game = new GameBoard(toLoad);
        GridPane gameBoard = BoardHandler.createBoard(game);
        gameBoard.setPadding(new Insets(0,30,5,30));

        //Creating the result text (will be updated by BoardHandler during the search)
        HBox bottom = new HBox();
        result = new Text("");
        result.setFill(Color.LIGHTGRAY);
        result.setTextAlignment(TextAlignment.CENTER);
        bottom.getChildren().addAll(result);
        bottom.setMinSize(600, 170);
        bottom.setAlignment(Pos.CENTER);

        //Configuring and showing the scene
        boardLayout.getChildren().addAll(top, gameBoard, bottom);
        Scene gameScene = new Scene(boardLayout);
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
                        MainMenu.heuristicValue, MainMenu.routineValue, toLoad));
            } catch (InterruptedException | CloneNotSupportedException e1) {
                e1.printStackTrace();
            }
        });
        t1.start();

        //Periodic refresh of the board to show selected moves and update the text elements
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> BoardHandler.updateBoard(game, toLoad), 0, 200, TimeUnit.MILLISECONDS);

        //You can click on the button to get back to the menu
        back.setOnMouseClicked(keyEvent -> {
            SokobanSolver.interrupt();
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
