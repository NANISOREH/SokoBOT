import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args){

        Level level = new Level(1);
        level.takeAction(Action.MOVE_UP);
        level.printBoard();
    }
}
