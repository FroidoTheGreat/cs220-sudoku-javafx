package sudoku;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

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
        return value >= 1 && value <= 9 && getPossibleValues(row, col).contains(value);
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
}
