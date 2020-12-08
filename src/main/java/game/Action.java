package game;

/*
Enumerates the possible actions on the board
*/
public enum Action {
    MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN;

    public Action getOpposite (Action a) {
        switch (a) {
            case MOVE_UP : return MOVE_DOWN;
            case MOVE_DOWN : return MOVE_UP;
            case MOVE_LEFT : return MOVE_RIGHT;
            case MOVE_RIGHT : return MOVE_LEFT;
        }
        return null;
    }
}
