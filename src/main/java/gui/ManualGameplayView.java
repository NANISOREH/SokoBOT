package gui;

import java.util.ArrayList;
import java.util.logging.Logger;

import game.Action;
import game.GameBoard;
import game.Level;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import solver.DeadlockDetector;
import solver.Node;
import solver.SokobanSolver;
import solver.SokobanToolkit;
import solver.configuration.DDRoutine;
import solver.configuration.ExpansionScheme;

public class ManualGameplayView {
    private static final Logger log = Logger.getLogger("manualGameplayView");
    private static Level toLoad;
    private static GameBoard game;
    private static int moves;
    protected static VBox boardLayout;
    static Node test;

    static void start() throws CloneNotSupportedException {
        boardLayout = new VBox();
        boardLayout.setBackground(MainMenu.background);
        boardLayout.setAlignment(Pos.CENTER);
        boardLayout.setSpacing(15);

        toLoad = new Level(MainMenu.levelValue);

        game = new GameBoard(toLoad);
        GridPane gameBoard = BoardHandler.createBoard(game);
        BoardHandler.isShowing = true;

        Text inGame = new Text();
        moves = 0;
        inGame.setText("Moves: " + moves + "\n\n\n");
        inGame.setFill(Color.LIGHTGRAY);
        boardLayout.getChildren().addAll(gameBoard, inGame);
        Scene gameScene = new Scene(boardLayout);

        Stage gameStage = new Stage();
        gameStage.setScene(gameScene);
        gameStage.show();

        DeadlockDetector.setRoutine(DDRoutine.DEAD_POSITIONS);
        DeadlockDetector.handleDeadPositions((GameBoard) game.clone());
        DeadlockDetector.setPrunedNodes(0);

        Node.setExpansionScheme(ExpansionScheme.PUSH_BASED);
        test = new Node(game, new ArrayList<>());
        log.info("hash " + test.hash());

        gameStage.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            switch (keyEvent.getCode()) {
                case UP : {
                    try {
                        if (DeadlockDetector.getPrunedNodes()==0 && !game.checkVictory()) {
                            if (game.takeAction(Action.MOVE_UP))
                                moves++;
                        }
                        log.info("hash " + test.hash() + "\nlower bound " + SokobanToolkit.estimateLowerBound(game));
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    BoardHandler.updateBoard(game, toLoad);
                    break;
                }
                case DOWN : {
                    try {
                        if (DeadlockDetector.getPrunedNodes()==0 && !game.checkVictory()) {
                            if (game.takeAction(Action.MOVE_DOWN))
                                moves++;
                        }
                        log.info("hash " + test.hash() + "\nlower bound " + SokobanToolkit.estimateLowerBound(game));
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    BoardHandler.updateBoard(game, toLoad);
                    break;
                }
                case RIGHT : {                 
                    try {                         
                        if (DeadlockDetector.getPrunedNodes()==0 && !game.checkVictory()) {
                            if (game.takeAction(Action.MOVE_RIGHT))
                                moves++;
                        }
                        log.info("hash " + test.hash() + "\nlower bound " + SokobanToolkit.estimateLowerBound(game));
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    BoardHandler.updateBoard(game, toLoad);
                    break;
                }
                case LEFT : {                    
                    try {                     
                        if (DeadlockDetector.getPrunedNodes()==0 && !game.checkVictory()) {
                            if (game.takeAction(Action.MOVE_LEFT))
                                moves++;
                        }
                        log.info("hash " + test.hash() + "\nlower bound " + SokobanToolkit.estimateLowerBound(game));
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    BoardHandler.updateBoard(game, toLoad);
                    break;
                }
                case R : {
                    DeadlockDetector.setPrunedNodes(0);
                    gameStage.close();
                    moves = 0;
                    try {
                        start();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case N : {
                    MainMenu.levelValue = (MainMenu.levelValue + 1) % (Level.NUM_LEVELS + 1);
                    if (MainMenu.levelValue == 0) MainMenu.levelValue++;
                    game = new GameBoard(toLoad);
                    DeadlockDetector.setPrunedNodes(0);
                    gameStage.close();
                    moves = 0;
                    try {
                        start();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
                default : break;
            }

            if (DeadlockDetector.getPrunedNodes()>0)
                inGame.setText("\nYou reached a DEADLOCK!\nPress R to restart the game or N to try next level\n");
            else
                inGame.setText("Moves: " + moves + "\n\n\n");

            //checking for victory after every action taken
            if (game.checkVictory()) {
                inGame.setText("Moves: " + moves + "\n\nVICTORY! Press R to restart the game or N to try next level\n");
                SokobanSolver.setSolution(null);
            }
        });
    }
}
