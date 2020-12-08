package game;

/*
Enumerates the possible content of a cell
Note that the "GOAL" value is only used to recognize the goal cells in the json source,
after parsing, goal cells are treated like empty cells with a boolean field expressing the fact that they're goal cells.
*/
public enum CellContent {
    EMPTY, WALL, SOKOBAN, BOX, GOAL
}
