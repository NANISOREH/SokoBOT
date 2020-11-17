

public class Main {
    public static void main(String[] args){

        GameBoard level = new GameBoard(1);
        level.takeAction(Action.MOVE_UP);
        level.takeAction(Action.MOVE_LEFT);
        level.takeAction(Action.MOVE_LEFT);
        level.takeAction(Action.MOVE_DOWN);
        level.takeAction(Action.MOVE_DOWN);
        level.printBoard();
    }
}
