package sokoban.game;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static java.lang.StrictMath.abs;

/*
This class models a cell on the board
*/
public class Cell implements Cloneable, Serializable {
    private int row;
    private int column;
    private CellContent content;
    private boolean goal;
    private Integer boxNumber;

    public Cell() {}

    public Cell(int row, int column, CellContent content, boolean goal, Integer boxNumber) {
        this.row = row;
        this.column = column;
        this.content = content;
        this.goal = goal;
        this.boxNumber = boxNumber;
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

    public Integer getBoxNumber() {
        return boxNumber;
    }

    public void setBoxNumber(Integer boxNumber) {
        this.boxNumber = boxNumber;
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
        cloned.boxNumber = this.boxNumber;
        switch (this.content) {
            case BOX : {
                cloned.content = CellContent.BOX;
                break;
            }
            case SOKOBAN : {
                cloned.content = CellContent.SOKOBAN;
                break;
            }
            case WALL : {
                cloned.content = CellContent.WALL;
                break;
            }
            case EMPTY : {
                cloned.content = CellContent.EMPTY;
                break;
            }

        }

        return cloned;
    }

    @Override
    public boolean equals(Object obj) {
        Cell cell = (Cell) obj;
        if (cell.getColumn() == this.column && cell.getRow() == this.row && cell.getContent() == this.content && cell.isGoal() == this.goal)
            return true;
        else
            return false;
    }

/*
    Returns the Manhattan distance between the argument cell and the parameter cell
*/
    public int manhattanDistance (Cell target) {
        return abs(this.getColumn() - target.getColumn()) + abs(this.getRow() - target.getRow());
    }

    public String toString() {
        String output = "";
        switch (content) {
            case SOKOBAN: output = "S";
            break;
            case BOX: output = "B";
            break;
            case GOAL: output = "G";
            break;
            case EMPTY: output = "E";
            break;
            case WALL: output =  "W";
            break;
        }

        output = output + "\n" + row + " - " + column;
        return output;
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
