package sokoban.solver;

import sokoban.game.Action;
import sokoban.game.Cell;
import sokoban.game.CellContent;
import sokoban.game.GameBoard;

import java.util.*;
import java.util.logging.Logger;

/*
This class will collect static methods implementing reusable algorithm optimizations and, in general, solutions to
subproblems encountered in the main flow of the program. Its only purpose is to keep other classes a bit cleaner.
*/
public class SokobanToolkit {
    private static Logger log = Logger.getLogger("SokobanToolkit");

/*
    This methods estimates a lower bound to get a starting point for iterative deepening algorithms and to estimate
    the cost of the path between the current node and the solution as a heuristic function for A*.
    It sees boxes tiles and goal tiles as two partitions of a bipartite graph and it constructs a perfect matching
    between the two that minimizes the sum of the manhattan distance between the couples in the matching.
    It uses an off-the-shelf implementation of the Hungarian Algorithm to do so.
*/
    public static int estimateLowerBound(GameBoard toSolve) {
         class HungarianAlgorithm {
/*             MIT License

             Copyright (c) 2018 aalmi

             Permission is hereby granted, free of charge, to any person obtaining a copy
             of this software and associated documentation files (the "Software"), to deal
             in the Software without restriction, including without limitation the rights
             to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
             copies of the Software, and to permit persons to whom the Software is
             furnished to do so, subject to the following conditions:

             The above copyright notice and this permission notice shall be included in all
             copies or substantial portions of the Software.

             THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
             IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
             FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
             AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
             LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
             OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
             SOFTWARE.*/

            int[][] matrix; // initial matrix (cost matrix)

            // markers in the matrix
            int[] squareInRow, squareInCol, rowIsCovered, colIsCovered, staredZeroesInRow;

            public HungarianAlgorithm(int[][] matrix) {
                if (matrix.length != matrix[0].length) {
                    try {
                        throw new IllegalAccessException("The matrix is not square!");
                    } catch (IllegalAccessException ex) {
                        System.err.println(ex);
                        System.exit(1);
                    }
                }

                this.matrix = matrix;
                squareInRow = new int[matrix.length];       // squareInRow & squareInCol indicate the position
                squareInCol = new int[matrix[0].length];    // of the marked zeroes

                rowIsCovered = new int[matrix.length];      // indicates whether a row is covered
                colIsCovered = new int[matrix[0].length];   // indicates whether a column is covered
                staredZeroesInRow = new int[matrix.length]; // storage for the 0*
                Arrays.fill(staredZeroesInRow, -1);
                Arrays.fill(squareInRow, -1);
                Arrays.fill(squareInCol, -1);
            }

            /**
             * find an optimal assignment
             *
             * @return optimal assignment
             */
            public int[][] findOptimalAssignment() {
                step1();    // reduce matrix
                step2();    // mark independent zeroes
                step3();    // cover columns which contain a marked zero

                while (!allColumnsAreCovered()) {
                    int[] mainZero = step4();
                    while (mainZero == null) {      // while no zero found in step4
                        step7();
                        mainZero = step4();
                    }
                    if (squareInRow[mainZero[0]] == -1) {
                        // there is no square mark in the mainZero line
                        step6(mainZero);
                        step3();    // cover columns which contain a marked zero
                    } else {
                        // there is square mark in the mainZero line
                        // step 5
                        rowIsCovered[mainZero[0]] = 1;  // cover row of mainZero
                        colIsCovered[squareInRow[mainZero[0]]] = 0;  // uncover column of mainZero
                        step7();
                    }
                }

                int[][] optimalAssignment = new int[matrix.length][];
                for (int i = 0; i < squareInCol.length; i++) {
                    optimalAssignment[i] = new int[]{i, squareInCol[i]};
                }
                return optimalAssignment;
            }

            /**
             * Check if all columns are covered. If that's the case then the
             * optimal solution is found
             *
             * @return true or false
             */
            private boolean allColumnsAreCovered() {
                for (int i : colIsCovered) {
                    if (i == 0) {
                        return false;
                    }
                }
                return true;
            }

            /**
             * Step 1:
             * Reduce the matrix so that in each row and column at least one zero exists:
             * 1. subtract each row minima from each element of the row
             * 2. subtract each column minima from each element of the column
             */
            private void step1() {
                // rows
                for (int i = 0; i < matrix.length; i++) {
                    // find the min value of the current row
                    int currentRowMin = Integer.MAX_VALUE;
                    for (int j = 0; j < matrix[i].length; j++) {
                        if (matrix[i][j] < currentRowMin) {
                            currentRowMin = matrix[i][j];
                        }
                    }
                    // subtract min value from each element of the current row
                    for (int k = 0; k < matrix[i].length; k++) {
                        matrix[i][k] -= currentRowMin;
                    }
                }

                // cols
                for (int i = 0; i < matrix[0].length; i++) {
                    // find the min value of the current column
                    int currentColMin = Integer.MAX_VALUE;
                    for (int j = 0; j < matrix.length; j++) {
                        if (matrix[j][i] < currentColMin) {
                            currentColMin = matrix[j][i];
                        }
                    }
                    // subtract min value from each element of the current column
                    for (int k = 0; k < matrix.length; k++) {
                        matrix[k][i] -= currentColMin;
                    }
                }
            }

            /**
             * Step 2:
             * mark each 0 with a "square", if there are no other marked zeroes in the same row or column
             */
            private void step2() {
                int[] rowHasSquare = new int[matrix.length];
                int[] colHasSquare = new int[matrix[0].length];

                for (int i = 0; i < matrix.length; i++) {
                    for (int j = 0; j < matrix.length; j++) {
                        // mark if current value == 0 & there are no other marked zeroes in the same row or column
                        if (matrix[i][j] == 0 && rowHasSquare[i] == 0 && colHasSquare[j] == 0) {
                            rowHasSquare[i] = 1;
                            colHasSquare[j] = 1;
                            squareInRow[i] = j; // save the row-position of the zero
                            squareInCol[j] = i; // save the column-position of the zero
                            continue; // jump to next row
                        }
                    }
                }
            }

            /**
             * Step 3:
             * Cover all columns which are marked with a "square"
             */
            private void step3() {
                for (int i = 0; i < squareInCol.length; i++) {
                    colIsCovered[i] = squareInCol[i] != -1 ? 1 : 0;
                }
            }

            /**
             * Step 7:
             * 1. Find the smallest uncovered value in the matrix.
             * 2. Subtract it from all uncovered values
             * 3. Add it to all twice-covered values
             */
            private void step7() {
                // Find the smallest uncovered value in the matrix
                int minUncoveredValue = Integer.MAX_VALUE;
                for (int i = 0; i < matrix.length; i++) {
                    if (rowIsCovered[i] == 1) {
                        continue;
                    }
                    for (int j = 0; j < matrix[0].length; j++) {
                        if (colIsCovered[j] == 0 && matrix[i][j] < minUncoveredValue) {
                            minUncoveredValue = matrix[i][j];
                        }
                    }
                }

                if (minUncoveredValue > 0) {
                    for (int i = 0; i < matrix.length; i++) {
                        for (int j = 0; j < matrix[0].length; j++) {
                            if (rowIsCovered[i] == 1 && colIsCovered[j] == 1) {
                                // Add min to all twice-covered values
                                matrix[i][j] += minUncoveredValue;
                            } else if (rowIsCovered[i] == 0 && colIsCovered[j] == 0) {
                                // Subtract min from all uncovered values
                                matrix[i][j] -= minUncoveredValue;
                            }
                        }
                    }
                }
            }

            /**
             * Step 4:
             * Find zero value Z_0 and mark it as "0*".
             *
             * @return position of Z_0 in the matrix
             */
            private int[] step4() {
                for (int i = 0; i < matrix.length; i++) {
                    if (rowIsCovered[i] == 0) {
                        for (int j = 0; j < matrix[i].length; j++) {
                            if (matrix[i][j] == 0 && colIsCovered[j] == 0) {
                                staredZeroesInRow[i] = j; // mark as 0*
                                return new int[]{i, j};
                            }
                        }
                    }
                }
                return null;
            }

            /**
             * Step 6:
             * Create a chain K of alternating "squares" and "0*"
             *
             * @param mainZero => Z_0 of Step 4
             */
            private void step6(int[] mainZero) {
                int i = mainZero[0];
                int j = mainZero[1];

                Set<int[]> K = new LinkedHashSet<>();
                //(a)
                // add Z_0 to K
                K.add(mainZero);
                boolean found = false;
                do {
                    // (b)
                    // add Z_1 to K if
                    // there is a zero Z_1 which is marked with a "square " in the column of Z_0
                    if (squareInCol[j] != -1) {
                        K.add(new int[]{squareInCol[j], j});
                        found = true;
                    } else {
                        found = false;
                    }

                    // if no zero element Z_1 marked with "square" exists in the column of Z_0, then cancel the loop
                    if (!found) {
                        break;
                    }

                    // (c)
                    // replace Z_0 with the 0* in the row of Z_1
                    i = squareInCol[j];
                    j = staredZeroesInRow[i];
                    // add the new Z_0 to K
                    if (j != -1) {
                        K.add(new int[]{i, j});
                        found = true;
                    } else {
                        found = false;
                    }

                } while (found); // (d) as long as no new "square" marks are found

                // (e)
                for (int[] zero : K) {
                    // remove all "square" marks in K
                    if (squareInCol[zero[1]] == zero[0]) {
                        squareInCol[zero[1]] = -1;
                        squareInRow[zero[0]] = -1;
                    }
                    // replace the 0* marks in K with "square" marks
                    if (staredZeroesInRow[zero[0]] == zero[1]) {
                        squareInRow[zero[0]] = zero[1];
                        squareInCol[zero[1]] = zero[0];
                    }
                }

                // (f)
                // remove all marks
                Arrays.fill(staredZeroesInRow, -1);
                Arrays.fill(rowIsCovered, 0);
                Arrays.fill(colIsCovered, 0);
            }
        }

        HashMap<Integer, Cell> boxes = (HashMap<Integer, Cell>) toSolve.getBoxCells().clone();
        ArrayList<Cell> goals = (ArrayList<Cell>) toSolve.getGoalCells().clone();
        int [][] distances = new int[boxes.size()][goals.size()];
        int result = 0;

        for (int i = 0; i < boxes.size(); i++) {
            for (int j = 0; j < goals.size(); j++) {
                distances[i][j] = boxes.get(i).manhattanDistance(goals.get(j));
            }
        }

        HungarianAlgorithm ha = new HungarianAlgorithm(distances);
        int[][] assignment = ha.findOptimalAssignment();

        for (int i = 0; i < assignment.length; i++) {
            result += boxes.get(assignment[i][1]).manhattanDistance(goals.get(assignment[i][0]));
        }

        return result;
    }

/*
    A simple move ordering optimization: states that involve pushing a box that was pushed by their parents too
    are considered before the others. This is useful because a lot of Sokoban proper solutions involve a certain number of consecutive
    pushes to the same box.
*/
    public static ArrayList<Node> orderByInertia (Node root, ArrayList <Node> expanded){
        Integer boxNumber = root.getLastMovedBox();
        if (boxNumber == null) {
            return expanded;
        }

        ArrayList<Node> result = new ArrayList<>();

        for (Node n : expanded) {
            if (n.getLastMovedBox() == null)
                continue;
            else if (n.getLastMovedBox() == boxNumber)
                result.add(n);
        }

        for (Node n : expanded) {
            if (!result.contains(n)) {
                result.add(n);
            }
        }

        return result;
    }

    public static int compareByInertia (Node first, Node second, Node root) {

        if (root.getLastMovedBox() == null ||
                (first.getLastMovedBox() == root.getLastMovedBox() && second.getLastMovedBox() == root.getLastMovedBox())) {
            return 0;
        }
        Integer boxNumber = root.getLastMovedBox();

        if (first.getLastMovedBox() == boxNumber && second.getLastMovedBox() != boxNumber)
            return -1;
        if (first.getLastMovedBox() != boxNumber && second.getLastMovedBox() == boxNumber)
            return 1;

        return 0;
    }


    /*
    Given a game state and a cell, it determines if Sokoban can reach that cell and returns a list of actions
    containing the best path that Sokoban can use to get there, or a null list in case there's no way to reach the target.
    It uses a BFS to do so.
*/
    public static ArrayList<Action> searchPath (GameBoard state, Cell target) throws CloneNotSupportedException {

        if (state.getSokobanCell().equals(target)) {
            return new ArrayList<>();
        }

        //Local class modeling a node for the BFS
        class BfsNode {
            ArrayList<Action> path;
            Cell node;

            public BfsNode (ArrayList<Action> path, Cell node) {
                this.path = path;
                this.node = node;
            }

            public void addAction (Action a) {
                path.add(a);
            }

            public BfsNode clone () throws CloneNotSupportedException {
                BfsNode d = new BfsNode((ArrayList) this.path.clone(), (Cell) this.node.clone());
                return d;
            }
        }

        ArrayList<Cell> transpositionTable = new ArrayList<>();

        ArrayList<BfsNode> front = new ArrayList<>();
        BfsNode root = new BfsNode(new ArrayList<>(), state.getSokobanCell());
        front.add(root);
        transpositionTable.add(root.node);

        while (true) {
            ArrayList<BfsNode> nextLevel = new ArrayList<>();
            for (BfsNode n : front) {
                if (n.node.equals(target)) {
                    return n.path;
                }

                if (state.getNorth(n.node).getContent() == CellContent.EMPTY) {
                    BfsNode north = n.clone();
                    north.node = state.getNorth(n.node);
                    if (!transpositionTable.contains(north.node)) {
                        transpositionTable.add(north.node);
                        north.addAction(Action.MOVE_UP);
                        nextLevel.add(north);
                    }
                }

                if (state.getSouth(n.node).getContent() == CellContent.EMPTY) {
                    BfsNode south = n.clone();
                    south.node = state.getSouth(n.node);
                    if (!transpositionTable.contains(south.node)) {
                        transpositionTable.add(south.node);
                        south.addAction(Action.MOVE_DOWN);
                        nextLevel.add(south);
                    }
                }

                if (state.getEast(n.node).getContent() == CellContent.EMPTY) {
                    BfsNode east = n.clone();
                    east.node = state.getEast(n.node);
                    if (!transpositionTable.contains(east.node)) {
                        transpositionTable.add(east.node);
                        east.addAction(Action.MOVE_RIGHT);
                        nextLevel.add(east);
                    }
                }

                if (state.getWest(n.node).getContent() == CellContent.EMPTY) {
                    BfsNode west = n.clone();
                    west.node = state.getWest(n.node);
                    if (!transpositionTable.contains(west.node)) {
                        transpositionTable.add(west.node);
                        west.addAction(Action.MOVE_LEFT);
                        nextLevel.add(west);
                    }
                }
            }

            if (front.size() == 0)
                return null;

            front.clear();
            for (int i = nextLevel.size() - 1; i>=0; i--) {
                front.add(nextLevel.remove(i));
            }

        }

    }

/*    This method uses a simple flood fill algorithm to return the list of every cell reachable by sokoban,
    given a certain game state*/
    public static ArrayList<Cell> getReachableCells(GameBoard state) throws CloneNotSupportedException {

        //structure initialization
        //transpositionTable will contain every visited cell and will be given as output
        //front will receive the cells to expand in every iteration
        ArrayList<Cell> transpositionTable = new ArrayList<>();
        ArrayList<Cell> front = new ArrayList<>();
        Cell root = state.getSokobanCell();
        front.add(root);
        transpositionTable.add(root);

        while (true) {
            ArrayList<Cell> nextLevel = new ArrayList<>();
            for (Cell c : front) {

                //expanding the current cell by checking for unvisited empty cells in every possible direction
                if (state.getNorth(c) != null && state.getNorth(c).getContent() == CellContent.EMPTY) {
                    Cell north = state.getNorth(c);
                    if (!transpositionTable.contains(north)) {
                        transpositionTable.add(north);
                        nextLevel.add(north);
                    }
                }

                if (state.getSouth(c) != null && state.getSouth(c).getContent() == CellContent.EMPTY) {
                    Cell south = state.getSouth(c);
                    if (!transpositionTable.contains(south)) {
                        transpositionTable.add(south);
                        nextLevel.add(south);
                    }
                }

                if (state.getEast(c) != null && state.getEast(c).getContent() == CellContent.EMPTY) {
                    Cell east = state.getNorth(c);
                    if (!transpositionTable.contains(east)) {
                        transpositionTable.add(east);
                        nextLevel.add(east);
                    }
                }

                if (state.getWest(c) != null && state.getWest(c).getContent() == CellContent.EMPTY) {
                    Cell west = state.getNorth(c);
                    if (!transpositionTable.contains(west)) {
                        transpositionTable.add(west);
                        nextLevel.add(west);
                    }
                }

            }

            //forming the front to explore for the next iteration
            front.clear();
            for (int i = nextLevel.size() - 1; i>=0; i--) {
                front.add(nextLevel.remove(i));
            }

            //breaking the cycle if we don't have anything else to explore
            if (front.size() == 0)
                break;

        }

        //sorting the resulting structure of cells so that states with the same cells reachable are
        //hashed the same way regardless of sokoban's position
        transpositionTable.sort(new Comparator<Cell>() {
            @Override
            public int compare(Cell cell, Cell t1) {
                if (cell.getRow() < t1.getRow())
                    return -1;
                else if (cell.getRow() > t1.getRow())
                    return 1;
                else if (cell.getRow() == t1.getRow()) {
                    return Integer.compare(cell.getColumn(), t1.getColumn());
                }
                return 0;
            }
        });

        return transpositionTable;
    }


}
