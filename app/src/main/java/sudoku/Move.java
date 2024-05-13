package sudoku;

public class Move {
    private int row;
    private int col;
    private int previousValue;
    private int newValue;

    public Move(int row, int col, int previousValue, int newValue) {
        this.row = row;
        this.col = col;
        this.previousValue = previousValue;
        this.newValue = newValue;
    }

    public void undo(Board board) {
        System.out.println("Undoing move: " + row + ", " + col + " = " + previousValue);
        board.rawSetCell(row, col, previousValue);
        System.out.println(board);
    }

    @Override
    public String toString() {
        return "Changed " + row + ", " + col + ", from " + previousValue + " to" + newValue;
    }
}