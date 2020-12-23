package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import game.Level;
import solver.DeadlockDetector;
import solver.configuration.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/*
Main class of the JavaFX application. It configures the menu UI and takes solver configurations from the user.
It starts the other parts of the UI and communicates them the information they need.
*/
public class MainMenu extends Application {
    protected static Logger log = Logger.getLogger("Main");

    //Elements of the UI
    protected static Background background = new Background(new BackgroundFill(Color.rgb(54, 54, 54), null, null));
    private static ChoiceBox<Integer> level = new ChoiceBox<>();
    private static ChoiceBox<String> algorithm = new ChoiceBox<>();
    private static ChoiceBox<String> scheme = new ChoiceBox<>();
    private static ChoiceBox<String> heuristic = new ChoiceBox<>();
    private static ChoiceBox<String> routine = new ChoiceBox<>();
    protected static Scene menu;

    //information to pass to other gui classes
    protected static String algorithmValue;
    protected static String schemeValue;
    protected static String heuristicValue;
    protected static String routineValue;
    protected static int levelValue;
    protected static boolean manualGameplay = false;


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
                manualGameplay = true;
                levelValue = level.getValue();
                ManualGameplayView.start();
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
        button.setAlignment(Pos.CENTER);


        //Configuring main layout and adding it to the menu scene
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        layout.setBackground(background);
        layout.setSpacing(50);
        layout.getChildren().addAll(levelContainer, algorithmSide, expSide, heuristicSide, ddSide, button);
        menu = new Scene(layout, 600, 700);

        //The button on the first scene triggers the switch to the gameplay scene and starts the game
        button.setOnAction(actionEvent -> {
            manualGameplay = false;
            algorithmValue = algorithm.getValue();
            heuristicValue = heuristic.getValue();
            schemeValue = scheme.getValue();
            levelValue = level.getValue();
            routineValue = routine.getValue();
            SolverView.start(primaryStage);
        });

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

}
