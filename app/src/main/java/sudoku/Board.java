package sudoku;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Board
{
    private int[][] board;
    private Stack<Move> moves;
    private List<Move> movesArchive;

    public Board()
    {
        board = new int[9][9];
        moves = new Stack<>();
        movesArchive = new ArrayList<>();
    }

    public Board(Board b)
    {
        board = new int[9][9];
        for (int r = 0; r < 9; r++)
        {
            for (int c = 0; c < 9; c++)
            {
                rawSetCell(r, c, b.getCell(r, c));
            }
        }
    }

    public static Board loadBoard(InputStream in)
    {
        Board board = new Board();
        Scanner scanner = new Scanner(in);
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                int value = scanner.nextInt();
                if (value != 0 && !board.getPossibleValues(row, col).contains(value)) {
                    throw new IllegalArgumentException("Invalid board: " + value + " is not a possible value for cell (" + row + ", " + col + ")");
                }
                board.rawSetCell(row, col, value);
            }
        }

        System.out.println("the new board is: " + board);

        scanner.close();
        return board;
    }

    public boolean isLegal(int row, int col, int value)
    {
        return value >= 1 && value <= 9;
    }

    public void setCell(int row, int col, int value)
    {
        if (value < 0 || value > 9)
        {
            throw new IllegalArgumentException("Value must be between 1 and 9 (or 0 to reset a value)");
        }
        // NOTE: I removed the check for possible values because I want to allow the user to input any value.
        // I made it so that instead it will set the color to red.

        // based on other values in the sudoku grid
        board[row][col] = value;
    }
    public void rawSetCell(int row, int col, int value)
    {
        board[row][col] = value;
    }

    public int getCell(int row, int col)
    {
        return board[row][col];
    }

    public boolean hasValue(int row, int col)
    {
        return getCell(row, col) > 0;
    }

    public Set<Integer> getPossibleValues(int row, int col)
    {
        Set<Integer> possibleValues = new HashSet<>();
        for (int i = 1; i <= 9; i++)
        {
            possibleValues.add(i);
        }
        // check the row
        for (int c = 0; c < 9; c++)
        {
            if (c != col) {
                possibleValues.remove(getCell(row, c));
            }
        }
        // check the column
        for (int r = 0; r < 9; r++)
        {
            if (r != row) {
                possibleValues.remove(getCell(r, col));
            }
        }
        // check the 3x3 square
        int startRow = row / 3 * 3;
        int startCol = col / 3 * 3;
        for (int r = startRow; r < startRow + 3; r++)
        {
            for (int c = startCol; c < startCol + 3; c++)
            {
                if (!(r == row && c == col)) {
                    possibleValues.remove(getCell(r, c));
                }
            }
        }
        return possibleValues;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                sb.append(getCell(row, col));
                if (col < 8)
                {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public void addMove(Move move)
    {
        moves.push(move);
        movesArchive.add(move);
    }

    public void makeMove(int r, int c, int value) {
        if (value == 0 || isLegal(r, c, value)) {
            int oldValue = getCell(r, c);
            System.out.println("Setting cell: " + r + ", " + c + " = " + value);
            setCell(r, c, value);
            if (oldValue != value) {
                System.out.println("adding move: " + r + ", " + c + " = " + oldValue + " to " + value);
                addMove(new Move(r, c, oldValue, value));
            }
        } else {
            throw new IllegalArgumentException("Invalid move: " + r + ", " + c + " = " + value);
        }
    }

    public String getMovesArchive()
    {
        String s = "";
        for (Move m : movesArchive)
        {
            s += m.toString() + "\n";
        }
        return s;
    }

    public void clearMoves()
    {
        moves.clear();
        movesArchive.clear();
    }

    public void undo()
    {
        if (!moves.isEmpty())
        {
            moves.pop().undo(this);
        } else
        {
            throw new IllegalStateException("No moves to undo");
        }
    }

    public boolean isSolved()
    {
        for (int r = 0; r < 9; r++)
        {
            for (int c = 0; c < 9; c++)
            {
                if (getCell(r, c) == 0 || !getPossibleValues(r, c).contains(getCell(r, c)));
                {
                    return false;
                }
            }
        }
        return true;
    }

    public void clearBoard()
    {
        clearMoves();
        for (int r = 0; r < 9; r++)
        {
            for (int c = 0; c < 9; c++)
            {
                rawSetCell(r, c, 0);
            }
        }
    }

    public void generateRandomBoard() {
        clearBoard();
        Board newBoard = generateCell();
        copy(newBoard);
    }

    public void generateMinimumSolvable() {
        generateRandomBoard();

        // store a list of all possible cells
        Stack<int[]> possibleCells = new Stack<>();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                possibleCells.push(new int[] {r, c});
            }
        }
        // randomize
        Collections.shuffle(possibleCells);

        // try removing each cell and checking if the board is still solvable
        // remove only one cell
        for (int i = 1; i < 82; i++) {
            int[] coords = possibleCells.pop();
            int r = coords[0];
            int c = coords[1];
            int value = getCell(r, c);
            rawSetCell(r, c, 0);
            if (numSolutions() > 1) {
                rawSetCell(r, c, value);
                System.out.println("COMPLETE!");
                return;
            }
        }
    }

    public int numSolutions() {
        Board b = new Board(this);

        return numSolutionsHelper(b);
    }
    private int numSolutionsHelper(Board b) {
        Board b2 = new Board(b);
        int numSolutions = 0;

        // get the coordinates of the next empty cell, then try setting it to the next possible value
        int[] coords = b2.findNextCoords();
        if (coords == null) {
            return numSolutions + 1;
        }
        int r = coords[0];
        int c = coords[1];

        // try setting the cell to each possible value
        for (int value : b2.getPossibleValues(r, c)) {
            b2.rawSetCell(r, c, value);
            numSolutions += numSolutionsHelper(b2);
        }


        return numSolutions;
    }

    public void copy(Board b) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                rawSetCell(r, c, b.getCell(r, c));
            }
        }
    }

    public boolean getSolvable() {
        return generateCell() != null;
    }

    public void solve() {
        Board solution = generateCell();
        if (solution != null) {
            copy(solution);
        }
    }

    private Board generateCell() {
        Board b = new Board(this);

        // get the coordinates of the next empty cell, then try setting it to the next possible value
        int[] coords = findNextCoords();
        if (coords == null) {
            return b;
        }

        int r = coords[0];
        int c = coords[1];

        List<Integer> possibleValues = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            if (b.getPossibleValues(r, c).contains(i)) {
                possibleValues.add(i);
            }
        }
        // shuffle the possible values
        Collections.shuffle(possibleValues);

        // recursively try setting the cell to each possible value
        for (int value : possibleValues) {
            b.rawSetCell(r, c, value);
            Board result = b.generateCell();
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private int[] findNextCoords() {
        // loop through the board until an empty cell is found, then return the coordinates of that cell
        for (int i = 0; i < 81; i++) {
            int r = i / 9;
            int c = i % 9;
            if (getCell(r, c) == 0) {
                return new int[] {r, c};
            }
        }      
        return null;  
    }
}
