# Sudoku using JavaFX

## TODO: notes for you to implement
1. !CHECK loadBoard() method should throw an exception if the file is not a valid sudoku board 
1. when saving: check if the file already exists, and ask the user if they want to overwrite it
1. !CHECK Undo the last move
    * requires a way to store a stack of moves
1. Undo, show values entered: show all the values we've entered since we loaded the board
1. !CHECK Hint, Show Hint: highlight all cells where only one legal value is possible
1. !CHECK on right-click handler: show a list of possible values that can go in this square

## Also add two interesting features of your own
* This is for the final 10 points to get to 100. 
    1. made the cells which were loaded in unchangeable until a further load
    2. made entering values more intuitive
    3. added the ability to show and hide hints
    4. navigate board with arrow keys
    