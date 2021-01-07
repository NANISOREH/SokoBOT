package solver;

import game.Action;
import game.Cell;
import game.CellContent;
import game.GameBoard;
import solver.configuration.Heuristic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

/*
This class will collect static methods implementing reusable algorithm optimizations and, in general, solutions to
subproblems encountered in the main flow of the program. Its only purpose is to keep other classes a bit cleaner.
*/
public class SokobanToolkit {
    private static Heuristic heuristic = Heuristic.MINIMUM_PERFECT_MATCHING;
    private static Logger log = Logger.getLogger("SokobanToolkit");

    public static int estimateLowerBound(GameBoard toSolve) {
        switch (heuristic) {
            case NAIVE_MATCHING: {
                return estimateNaively(toSolve);
            }
            case MINIMUM_PERFECT_MATCHING: {
                return estimateProperly(toSolve);
            }
        }

        return -1;
    }

    /*
     * This method estimates a lower bound to get a starting point for iterative
     * deepening algorithms and to estimate the cost of the path between the current
     * node and the solution as a heuristic function. It sees box tiles and
     * goal tiles as two partitions of a bipartite graph and it matches every box
     * with the nearest goal, then sums the Manhattan distances between the couples.
     */
    private static int estimateNaively(GameBoard toSolve) {
        HashMap<Integer, Cell> boxes = (HashMap<Integer, Cell>) toSolve.getBoxCells().clone();
        ArrayList<Cell> goals = (ArrayList<Cell>) toSolve.getGoalCells().clone();

        int result = 0;
        int minimum;

        for (Cell c : goals) {
            minimum = Integer.MAX_VALUE;
            for (int i = 0; i < boxes.size(); i++) {
                if (c.manhattanDistance(boxes.get(i)) < minimum)
                    minimum = c.manhattanDistance(boxes.get(i));
            }
            result += minimum;
        }

        return result;
    }

    /*
     * This method estimates a lower bound to get a starting point for iterative
     * deepening algorithms and to estimate the cost of the path between the current
     * node and the solution as a heuristic function. It sees box tiles and
     * goal tiles as two partitions of a bipartite graph and it constructs a perfect
     * matching between the two that minimizes the sum of the manhattan distance
     * between the couples in the matching. It uses an off-the-shelf implementation
     * of the Hungarian Algorithm to do so.
     */
    private static int estimateProperly(GameBoard toSolve) {


        class HungarianAlgorithm {
            /* Copyright (c) 2012 Kevin L. Stern
             *
             * Permission is hereby granted, free of charge, to any person obtaining a copy
             * of this software and associated documentation files (the "Software"), to deal
             * in the Software without restriction, including without limitation the rights
             * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
             * copies of the Software, and to permit persons to whom the Software is
             * furnished to do so, subject to the following conditions:
             *
             * The above copyright notice and this permission notice shall be included in
             * all copies or substantial portions of the Software.
             *
             * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
             * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
             * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
             * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
             * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
             * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
             * SOFTWARE.
             */

            private final double[][] costMatrix;
            private final int rows, cols, dim;
            private final double[] labelByWorker, labelByJob;
            private final int[] minSlackWorkerByJob;
            private final double[] minSlackValueByJob;
            private final int[] matchJobByWorker, matchWorkerByJob;
            private final int[] parentWorkerByCommittedJob;
            private final boolean[] committedWorkers;

            /**
             * Construct an instance of the algorithm.
             *
             * @param costMatrix
             *          the cost matrix, where matrix[i][j] holds the cost of assigning
             *          worker i to job j, for all i, j. The cost matrix must not be
             *          irregular in the sense that all rows must be the same length; in
             *          addition, all entries must be non-infinite numbers.
             */
            public HungarianAlgorithm(double[][] costMatrix) {
                this.dim = Math.max(costMatrix.length, costMatrix[0].length);
                this.rows = costMatrix.length;
                this.cols = costMatrix[0].length;
                this.costMatrix = new double[this.dim][this.dim];
                for (int w = 0; w < this.dim; w++) {
                    if (w < costMatrix.length) {
                        if (costMatrix[w].length != this.cols) {
                            throw new IllegalArgumentException("Irregular cost matrix");
                        }
                        for (int j = 0; j < this.cols; j++) {
                            if (Double.isInfinite(costMatrix[w][j])) {
                                throw new IllegalArgumentException("Infinite cost");
                            }
                            if (Double.isNaN(costMatrix[w][j])) {
                                throw new IllegalArgumentException("NaN cost");
                            }
                        }
                        this.costMatrix[w] = Arrays.copyOf(costMatrix[w], this.dim);
                    } else {
                        this.costMatrix[w] = new double[this.dim];
                    }
                }
                labelByWorker = new double[this.dim];
                labelByJob = new double[this.dim];
                minSlackWorkerByJob = new int[this.dim];
                minSlackValueByJob = new double[this.dim];
                committedWorkers = new boolean[this.dim];
                parentWorkerByCommittedJob = new int[this.dim];
                matchJobByWorker = new int[this.dim];
                Arrays.fill(matchJobByWorker, -1);
                matchWorkerByJob = new int[this.dim];
                Arrays.fill(matchWorkerByJob, -1);
            }

            /**
             * Compute an initial feasible solution by assigning zero labels to the
             * workers and by assigning to each job a label equal to the minimum cost
             * among its incident edges.
             */
            protected void computeInitialFeasibleSolution() {
                for (int j = 0; j < dim; j++) {
                    labelByJob[j] = Double.POSITIVE_INFINITY;
                }
                for (int w = 0; w < dim; w++) {
                    for (int j = 0; j < dim; j++) {
                        if (costMatrix[w][j] < labelByJob[j]) {
                            labelByJob[j] = costMatrix[w][j];
                        }
                    }
                }
            }

            /**
             * Execute the algorithm.
             *
             * @return the minimum cost matching of workers to jobs based upon the
             *         provided cost matrix. A matching value of -1 indicates that the
             *         corresponding worker is unassigned.
             */
            public int[] execute() {
                /*
                 * Heuristics to improve performance: Reduce rows and columns by their
                 * smallest element, compute an initial non-zero dual feasible solution and
                 * create a greedy matching from workers to jobs of the cost matrix.
                 */
                reduce();
                computeInitialFeasibleSolution();
                greedyMatch();

                int w = fetchUnmatchedWorker();
                while (w < dim) {
                    initializePhase(w);
                    executePhase();
                    w = fetchUnmatchedWorker();
                }
                int[] result = Arrays.copyOf(matchJobByWorker, rows);
                for (w = 0; w < result.length; w++) {
                    if (result[w] >= cols) {
                        result[w] = -1;
                    }
                }
                return result;
            }

            /**
             * Execute a single phase of the algorithm. A phase of the Hungarian algorithm
             * consists of building a set of committed workers and a set of committed jobs
             * from a root unmatched worker by following alternating unmatched/matched
             * zero-slack edges. If an unmatched job is encountered, then an augmenting
             * path has been found and the matching is grown. If the connected zero-slack
             * edges have been exhausted, the labels of committed workers are increased by
             * the minimum slack among committed workers and non-committed jobs to create
             * more zero-slack edges (the labels of committed jobs are simultaneously
             * decreased by the same amount in order to maintain a feasible labeling).
             * <p>
             *
             * The runtime of a single phase of the algorithm is O(n^2), where n is the
             * dimension of the internal square cost matrix, since each edge is visited at
             * most once and since increasing the labeling is accomplished in time O(n) by
             * maintaining the minimum slack values among non-committed jobs. When a phase
             * completes, the matching will have increased in size.
             */
            protected void executePhase() {
                while (true) {
                    int minSlackWorker = -1, minSlackJob = -1;
                    double minSlackValue = Double.POSITIVE_INFINITY;
                    for (int j = 0; j < dim; j++) {
                        if (parentWorkerByCommittedJob[j] == -1) {
                            if (minSlackValueByJob[j] < minSlackValue) {
                                minSlackValue = minSlackValueByJob[j];
                                minSlackWorker = minSlackWorkerByJob[j];
                                minSlackJob = j;
                            }
                        }
                    }
                    if (minSlackValue > 0) {
                        updateLabeling(minSlackValue);
                    }
                    parentWorkerByCommittedJob[minSlackJob] = minSlackWorker;
                    if (matchWorkerByJob[minSlackJob] == -1) {
                        /*
                         * An augmenting path has been found.
                         */
                        int committedJob = minSlackJob;
                        int parentWorker = parentWorkerByCommittedJob[committedJob];
                        while (true) {
                            int temp = matchJobByWorker[parentWorker];
                            match(parentWorker, committedJob);
                            committedJob = temp;
                            if (committedJob == -1) {
                                break;
                            }
                            parentWorker = parentWorkerByCommittedJob[committedJob];
                        }
                        return;
                    } else {
                        /*
                         * Update slack values since we increased the size of the committed
                         * workers set.
                         */
                        int worker = matchWorkerByJob[minSlackJob];
                        committedWorkers[worker] = true;
                        for (int j = 0; j < dim; j++) {
                            if (parentWorkerByCommittedJob[j] == -1) {
                                double slack = costMatrix[worker][j] - labelByWorker[worker]
                                        - labelByJob[j];
                                if (minSlackValueByJob[j] > slack) {
                                    minSlackValueByJob[j] = slack;
                                    minSlackWorkerByJob[j] = worker;
                                }
                            }
                        }
                    }
                }
            }

            /**
             *
             * @return the first unmatched worker or {@link #dim} if none.
             */
            protected int fetchUnmatchedWorker() {
                int w;
                for (w = 0; w < dim; w++) {
                    if (matchJobByWorker[w] == -1) {
                        break;
                    }
                }
                return w;
            }

            /**
             * Find a valid matching by greedily selecting among zero-cost matchings. This
             * is a heuristic to jump-start the augmentation algorithm.
             */
            protected void greedyMatch() {
                for (int w = 0; w < dim; w++) {
                    for (int j = 0; j < dim; j++) {
                        if (matchJobByWorker[w] == -1 && matchWorkerByJob[j] == -1
                                && costMatrix[w][j] - labelByWorker[w] - labelByJob[j] == 0) {
                            match(w, j);
                        }
                    }
                }
            }

            /**
             * Initialize the next phase of the algorithm by clearing the committed
             * workers and jobs sets and by initializing the slack arrays to the values
             * corresponding to the specified root worker.
             *
             * @param w
             *          the worker at which to root the next phase.
             */
            protected void initializePhase(int w) {
                Arrays.fill(committedWorkers, false);
                Arrays.fill(parentWorkerByCommittedJob, -1);
                committedWorkers[w] = true;
                for (int j = 0; j < dim; j++) {
                    minSlackValueByJob[j] = costMatrix[w][j] - labelByWorker[w]
                            - labelByJob[j];
                    minSlackWorkerByJob[j] = w;
                }
            }

            /**
             * Helper method to record a matching between worker w and job j.
             */
            protected void match(int w, int j) {
                matchJobByWorker[w] = j;
                matchWorkerByJob[j] = w;
            }

            /**
             * Reduce the cost matrix by subtracting the smallest element of each row from
             * all elements of the row as well as the smallest element of each column from
             * all elements of the column. Note that an optimal assignment for a reduced
             * cost matrix is optimal for the original cost matrix.
             */
            protected void reduce() {
                for (int w = 0; w < dim; w++) {
                    double min = Double.POSITIVE_INFINITY;
                    for (int j = 0; j < dim; j++) {
                        if (costMatrix[w][j] < min) {
                            min = costMatrix[w][j];
                        }
                    }
                    for (int j = 0; j < dim; j++) {
                        costMatrix[w][j] -= min;
                    }
                }
                double[] min = new double[dim];
                for (int j = 0; j < dim; j++) {
                    min[j] = Double.POSITIVE_INFINITY;
                }
                for (int w = 0; w < dim; w++) {
                    for (int j = 0; j < dim; j++) {
                        if (costMatrix[w][j] < min[j]) {
                            min[j] = costMatrix[w][j];
                        }
                    }
                }
                for (int w = 0; w < dim; w++) {
                    for (int j = 0; j < dim; j++) {
                        costMatrix[w][j] -= min[j];
                    }
                }
            }

            /**
             * Update labels with the specified slack by adding the slack value for
             * committed workers and by subtracting the slack value for committed jobs. In
             * addition, update the minimum slack values appropriately.
             */
            protected void updateLabeling(double slack) {
                for (int w = 0; w < dim; w++) {
                    if (committedWorkers[w]) {
                        labelByWorker[w] += slack;
                    }
                }
                for (int j = 0; j < dim; j++) {
                    if (parentWorkerByCommittedJob[j] != -1) {
                        labelByJob[j] -= slack;
                    } else {
                        minSlackValueByJob[j] -= slack;
                    }
                }
            }
        }

        HashMap<Integer, Cell> boxes = (HashMap<Integer, Cell>) toSolve.getBoxCells().clone();
        ArrayList<Cell> goals = (ArrayList<Cell>) toSolve.getGoalCells().clone();
        double[][] distances = new double[boxes.size()][goals.size()];
        int result = 0;

        for (int i = 0; i < boxes.size(); i++) {
            for (int j = 0; j < goals.size(); j++) {
                distances[i][j] = boxes.get(i).manhattanDistance(goals.get(j));
            }
        }

        HungarianAlgorithm ha = new HungarianAlgorithm(distances);
        int[] assignment = ha.execute();

        for (int i = 0; i < assignment.length; i++) {
            result += boxes.get(i).manhattanDistance(goals.get(assignment[i]));
        }

        return result;
    }

    /*
     * A simple move ordering optimization: states that involve pushing a box that
     * was pushed by their parents too are considered before the others. This is
     * useful because a lot of Sokoban proper solutions involve a certain number of
     * consecutive pushes to the same box.
     */
    public static ArrayList<Node> orderByInertia(Node root, ArrayList<Node> expanded) {
        Integer boxNumber = root.getGame().getLastMovedBox();
        if (boxNumber == null) {
            return expanded;
        }

        ArrayList<Node> result = new ArrayList<>();

        for (Node n : expanded) {
            if (n.getGame().getLastMovedBox() == null)
                continue;
            else if (n.getGame().getLastMovedBox() == boxNumber)
                result.add(n);
        }

        for (Node n : expanded) {
            if (!result.contains(n)) {
                result.add(n);
            }
        }

        return result;
    }

    /*
     * Takes two nodes in an expansion batch and their parent, and returns a
     * positive value if the first child moved the same box as the father while the
     * second didn't, a negative value if the opposite happened, 0 if no distinction
     * has to be made. In other words, it allows for Primary Queue structures to
     * order nodes with the same heuristic estimation using a criteria of move
     * ordering by inertia
     */
    public static int compareByInertia(Node first, Node second, Node firstRoot, Node secondRoot) {

        boolean firstMoved = false, secondMoved = false;

        if (firstRoot.getGame().getLastMovedBox() != null) {
            if (first.getGame().getLastMovedBox() == firstRoot.getGame().getLastMovedBox())
                firstMoved = true;
            else
                firstMoved = false;
        }

        if (secondRoot.getGame().getLastMovedBox() != null) {
            if (second.getGame().getLastMovedBox() == secondRoot.getGame().getLastMovedBox())
                secondMoved = true;
            else
                secondMoved = false;
        }

        if ((firstMoved && secondMoved) || (!firstMoved && !secondMoved))
            return 0;
        else if (firstMoved && !secondMoved)
            return 1;
        else if (secondMoved && !firstMoved)
            return -1;

        return 0;
    }

    /*
     * Given a game state and a cell, it determines if Sokoban can reach that cell
     * and returns a list of actions containing the best path that Sokoban can use
     * to get there, or a null list in case there's no way to reach the target. It
     * uses a BFS to do so.
     */
    public static ArrayList<Action> searchPath(GameBoard state, Cell target) throws CloneNotSupportedException {

        if (state.getSokobanCell().equals(target)) {
            return new ArrayList<>();
        }

        // Local class modeling a node for the BFS
        class BfsNode {
            ArrayList<Action> path;
            Cell node;

            public BfsNode(ArrayList<Action> path, Cell node) {
                this.path = path;
                this.node = node;
            }

            public void addAction(Action a) {
                path.add(a);
            }

            public BfsNode clone() throws CloneNotSupportedException {
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
            for (int i = nextLevel.size() - 1; i >= 0; i--) {
                front.add(nextLevel.remove(i));
            }

        }

    }

    /*
     * This method uses a simple flood fill algorithm to return a byte array
     * representing every cell reachable by sokoban, given a certain game state
     */
    public static byte[] getReachableCells(GameBoard state) throws CloneNotSupportedException, IOException {
        
        //This class encapsulates a couple of coordinates
        class Coordinates implements Serializable{
            private static final long serialVersionUID = 1L;
            int row;
            int column;

            Coordinates (int row, int column) {
                this.row = row;
                this.column = column;
            }

            @Override
            public boolean equals(Object obj) {
                Coordinates c = (Coordinates) obj;
                if (this.row == c.row && this.column == c.column)
                    return true;
                else
                    return false;
            }
        }

        //structure initialization
        //transpositionTable will contain every visited cell and will be given as output
        //front will receive the cells to expand in every iteration
        ArrayList<Coordinates> transpositionTable = new ArrayList<>();
        ArrayList<Cell> front = new ArrayList<>();
        Cell root = state.getSokobanCell();
        front.add(root);
        Coordinates initial = new Coordinates (root.getRow(), root.getColumn());
        transpositionTable.add(initial);

        while (true) {
            ArrayList<Cell> nextLevel = new ArrayList<>();
            for (Cell c : front) {

                //expanding the current cell by checking for unvisited empty cells in every possible direction
                if (state.getNorth(c) != null && state.getNorth(c).getContent() != CellContent.WALL
                    && state.getNorth(c).getContent() != CellContent.BOX) {

                    Cell north = state.getNorth(c);
                    Coordinates first = new Coordinates (north.getRow(), north.getColumn());
                    if (!transpositionTable.contains(first)) {
                        transpositionTable.add(first);
                        nextLevel.add(north);
                    }
                }

                if (state.getSouth(c) != null && state.getSouth(c).getContent() != CellContent.WALL
                    && state.getSouth(c).getContent() != CellContent.BOX) {

                    Cell south = state.getSouth(c);
                    Coordinates second = new Coordinates (south.getRow(), south.getColumn());
                    if (!transpositionTable.contains(second)) {
                        transpositionTable.add(second);
                        nextLevel.add(south);
                    }
                }

                if (state.getEast(c) != null && state.getEast(c).getContent() != CellContent.WALL
                    && state.getEast(c).getContent() != CellContent.BOX) {
                                            
                    Cell east = state.getEast(c);
                    Coordinates third = new Coordinates (east.getRow(), east.getColumn());
                    if (!transpositionTable.contains(third)) {
                        transpositionTable.add(third);
                        nextLevel.add(east);
                    }
                }

                if (state.getWest(c) != null && state.getWest(c).getContent() != CellContent.WALL
                    && state.getWest(c).getContent() != CellContent.BOX) {

                    Cell west = state.getWest(c);
                    Coordinates fourth = new Coordinates (west.getRow(), west.getColumn());
                    if (!transpositionTable.contains(fourth)) {
                        transpositionTable.add(fourth);
                        nextLevel.add(west);
                    }
                }

            }

            //forming the front to explore for the next iteration
            front.clear();
            int size = nextLevel.size();
            for (int i = size - 1; i>=0; i--) {
                front.add(nextLevel.remove(i));
            }

            //breaking the cycle if we don't have anything else to explore
            if (front.isEmpty())
                break;

        }

        //sorting the resulting structure of cells so that states with the same cells reachable are
        //hashed the same way regardless of sokoban's position
        transpositionTable.sort(new Comparator<Coordinates>() {
            @Override
            public int compare(Coordinates t1, Coordinates t2) {
                if (t1.row < t2.row)
                    return -1;
                else if (t1.row > t2.row)
                    return 1;
                else if (t1.row == t2.row) {
                    return Integer.compare(t1.column, t2.column);
                }
                return 0;
            }
        });

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(transpositionTable);
        oos.flush();
        byte [] data = bos.toByteArray();

        return data;
    }


    public static void setHeuristic(Heuristic heuristic) {
        SokobanToolkit.heuristic = heuristic;
    }

    public static Heuristic getHeuristic() {
        return heuristic;
    }
}
