package sokoban.game;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/*
This class models a cell on the board
*/
public class Cell implements Cloneable, Serializable {
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

/*    public Cell clone () {
        return new Cell (this.getRow(), this.getColumn(), this.content, this.goal);
    }*/
    @Override
    public Object clone() throws CloneNotSupportedException {
        Object obj = super.clone();
        Cell cloned = (Cell) obj;
        cloned.row = this.row;
        cloned.column = this.column;
        cloned.goal = this.goal;
        switch (this.content) {
            case BOX -> {
                cloned.content = CellContent.BOX;
                break;
            }
            case SOKOBAN -> {
                cloned.content = CellContent.SOKOBAN;
                break;
            }
            case WALL -> {
                cloned.content = CellContent.WALL;
                break;
            }
            case EMPTY -> {
                cloned.content = CellContent.EMPTY;
                break;
            }

        }

        return cloned;
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

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.flush();
        byte [] data = bos.toByteArray();

        return data;
    }
}
