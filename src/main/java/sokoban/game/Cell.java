package sokoban.game;

/*
This class models a cell on the board
*/
public class Cell {
    private int row;
    private int column;
    private CellContent content;
    private boolean goal;

    public Cell() {}

    public Cell(int row, int column, CellContent content, boolean goal) {
        this.row = row;
        this.column = column;
        this.content = content;
        this.goal = goal;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public CellContent getContent () {
        return content;
    }

    public void setContent (CellContent content) {
        this.content = content;
    }

    public boolean isGoal() {
        return goal;
    }

    public void setGoal(boolean goal) {
        this.goal = goal;
    }

    public Cell clone () {
        CellContent content = this.getContent();
        return new Cell (this.getRow(), this.getColumn(), content, this.goal);
    }

    public String toString() {
        switch (content) {
            case SOKOBAN: return "S";
            case BOX: return "B";
            case GOAL: return "G";
            case EMPTY: return "E";
            case WALL: return  "W";
        }

        return null;
    }
}
