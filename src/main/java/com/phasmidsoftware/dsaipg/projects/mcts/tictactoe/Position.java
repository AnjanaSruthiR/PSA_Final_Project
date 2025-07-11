/*
 * Copyright (c) 2024. Robin Hillyard
 */
package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This class represents the board of the Tic-tac-toe game. It is a 3x3 matrix
 * of 0s, 1s, and -1s for O, X, and . respectively.
 */
public class Position {

    /**
     * Parse a string of X, O, and . to form a Position.
     *
     * @param grid the grid represented as a String.
     * @param last the last player.
     * @return a Position.
     */
    static Position parsePosition(final String grid, final int last) {
        int[][] matrix = new int[gridSize][gridSize];
        int count = 0;
        String[] rows = grid.split("\\n", gridSize);
        for (int i = 0; i < gridSize; i++) {
            String[] cells = rows[i].split(" ", gridSize);
            for (int j = 0; j < gridSize; j++) {
                int cell = parseCell(cells[j].trim());
                if (cell >= 0) {
                    count++;
                }
                matrix[i][j] = cell;
            }
        }
        return new Position(matrix, count, last);
    }

    /**
     * Method to parse a single cell.
     *
     * @param cell the String for the cell.
     * @return a number between -1 and one inclusive.
     */
    static int parseCell(String cell) {
        return switch (cell.toUpperCase()) {
            case "O", "0" ->
                0;
            case "X", "1" ->
                1;
            default ->
                -1;
        };
    }

    /**
     * Effect a player's move on this Position.
     *
     * @param player the player (0: O, 1: X)
     * @param x the first dimension value.
     * @param y the second dimension value.
     * @return the new Position.
     */
    public Position move(int player, int x, int y) {
        if (full()) {
            throw new RuntimeException("Position is full");
        }
        if (player == last) {
            throw new RuntimeException("consecutive moves by same player: " + player);
        }
        int[][] matrix = copyGrid();
        if (matrix[x][y] < 0) {
            matrix[x][y] = player;
            return new Position(matrix, count + 1, player);
        }
        throw new RuntimeException("Position is occupied: " + x + ", " + y);
    }

    /**
     * Method to yield all the possible moves available on this Position.
     *
     * @return a list of [x,y] arrays.
     */
    public List<int[]> moves(int player) {
        if (player == last) {
            throw new RuntimeException("consecutive moves by same player: " + player);
        }
        List<int[]> result = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (grid[i][j] < 0) {
                    result.add(new int[]{i, j});
                }
            }
        }
        return result;
    }

    /**
     * Method to yield a copy of this Position but reflected.
     * <p>
     * TESTME
     *
     * @param axis the axis about which to reflect.
     * @return a new Position.
     */
    public Position reflect(int axis) {
        int[][] matrix = copyGrid();
        switch (axis) {
            case 0:
                for (int j = 0; j < gridSize; j++) {
                    swap(matrix, 0, j, 2, j); // middle row

                                }break;
            case 1:
                for (int i = 0; i < gridSize; i++) {
                    swap(matrix, i, 0, i, 2); // middle column

                                }break;
            default:
                throw new RuntimeException("reflect not implemented for " + axis);
        }
        return new Position(matrix, count, last);
    }

    /**
     * Method to rotate this Position by 90 degrees clockwise. TESTME
     *
     * @return a new Position which is rotated from this.
     */
    public Position rotate() {
        int[][] matrix = new int[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                matrix[i][j] = grid[j][gridSize - i - 1];
            }
        }
        return new Position(matrix, count, last);
    }

    /**
     * Determine if this Position represents a winner.
     *
     * @return an Optional Integer.
     */
    public Optional<Integer> winner() {
        if (count > 4 && threeInARow()) {
            return Optional.of(last);
        }
        return Optional.empty();
    }

    /**
     * Method to determine if this Position has three in a row (i.e. a winning
     * position). Don't forget to check for columns and diagonals as well.
     * <p>
     * NOTE: you may find the instance field xxx to be useful, as well as the
     * projectRow, projectCol, etc. private methods.
     *
     * @return true if there are three cells in a line that are the same and
     * equal to the last player.
     */
    boolean threeInARow() {
        for (int i = 0; i < gridSize; i++) {
            if (grid[i][0] != -1 && grid[i][0] == grid[i][1] && grid[i][1] == grid[i][2]) {
                return true;
            }
        }
        for (int j = 0; j < gridSize; j++) {
            if (grid[0][j] != -1 && grid[0][j] == grid[1][j] && grid[1][j] == grid[2][j]) {
                return true;
            }
        }
        if (grid[0][0] != -1 && grid[0][0] == grid[1][1] && grid[1][1] == grid[2][2]) {
            return true;
        }
        if (grid[0][2] != -1 && grid[0][2] == grid[1][1] && grid[1][1] == grid[2][0]) {
            return true;
        }
        return false;
    }

    /**
     * Project row i.
     *
     * @param i the row index.
     * @return an array of three ints.
     */
    int[] projectRow(int i) {
        return grid[i];
    }

    /**
     * Project column j.
     *
     * @param j the column index.
     * @return an array of three ints.
     */
    int[] projectCol(int j) {
        int[] result = new int[gridSize];
        for (int i = 0; i < gridSize; i++) {
            result[i] = grid[i][j];
        }
        return result;
    }

    /**
     * Get the diagonal according to b.
     *
     * @param b true if the matrix diagonal else transpose diagonal.
     * @return an int[3].
     */
    int[] projectDiag(boolean b) {
        int[] result = new int[gridSize];
        for (int j = 0; j < gridSize; j++) {
            int i = b ? j : gridSize - j - 1;
            result[j] = grid[i][j];
        }
        return result;
    }

    /**
     * @return true if this Position has 9 elements.
     */
    boolean full() {
        return count == 9;
    }

    /**
     * Method to render this Position in a pleasing manner.
     *
     * @return a String.
     */
    public String render() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                sb.append(render(grid[i][j]));
                if (j < gridSize - 1) {
                    sb.append(' ');
                }
            }
            if (i < gridSize - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                sb.append(grid[i][j]);
                if (j < gridSize - 1) {
                    sb.append(',');
                }
            }
            if (i < gridSize - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position position)) {
            return false;
        }
        return Arrays.deepEquals(grid, position.grid);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(grid);
    }

    Position(int[][] grid, int count, int last) {
        this.grid = grid;
        this.count = count;
        this.last = last;
        xxx = new int[]{last, last, last};
    }

    private int[][] copyGrid() {
        int[][] result = new int[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            result[i] = Arrays.copyOf(grid[i], gridSize);
        }
        return result;
    }

    private char render(int x) {
        return switch (x) {
            case 0 ->
                'O';
            case 1 ->
                'X';
            default ->
                '.';
        };
    }

    /**
     * TESTME
     *
     * @param matrix the matrix to be operated on.
     * @param i1 first row.
     * @param j1 first column.
     * @param i2 second row.
     * @param j2 second column.
     */
    private void swap(int[][] matrix, int i1, int j1, int i2, int j2) {
        int temp = matrix[i1][j1];
        matrix[i1][j1] = matrix[i2][j2];
        matrix[i2][j2] = temp;
    }

    private final int[][] grid;
    final int last;
    private final int count;
    private final static int gridSize = 3;
    private final int[] xxx;
}
