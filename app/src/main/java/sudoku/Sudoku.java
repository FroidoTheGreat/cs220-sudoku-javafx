package sudoku;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Set;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Sudoku extends Application
{
    private Board board = new Board();
    public static final int SIZE = 9;
    private VBox root;
    private VBox boardVBox;
    private TextField[][] textFields = new TextField[SIZE][SIZE];
    private int width = 800;
    private int height = 800;
    private boolean updatingBoard = false;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        root = new VBox();

        //System.out.println(new File(".").getAbsolutePath());

        root.getChildren().add(createMenuBar(primaryStage));

        GridPane gridPane = new GridPane();
        boardVBox = new VBox();
        root.getChildren().add(boardVBox);
        boardVBox.getChildren().add(gridPane);
        boardVBox.getStyleClass().add("board-vbox");
        boardVBox.setAlignment(Pos.CENTER);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getStyleClass().add("grid-pane");

        // create a 9x9 grid of text fields
        for (int row = 0; row < SIZE; row++)
        {
            for (int col = 0; col < SIZE; col++)
            {
                textFields[row][col] = new TextField();
                TextField textField = textFields[row][col];
                
                // setting ID so that we can look up the text field by row and col
                // IDs are #3-4 for the 4th row and 5th column (start index at 0)
                textField.setId(row + "-" + col);
                gridPane.add(textField, col, row);
                // using CSS to get the darker borders correct
                if (row == 0 && col == 0) {
                    // add top left class
                    textField.getStyleClass().add("top-left-border");
                } else if (row == 0 && col % 3 == 2) {
                    // add top right class
                    textField.getStyleClass().add("top-right-border");
                } else if (col == 0 && row % 3 == 2) {
                    // add bottom left class
                    textField.getStyleClass().add("bottom-left-border");
                } else if (row == 0) {
                    // add top border class
                    textField.getStyleClass().add("top-border");
                } else if (col == 0) {
                    // add left border class
                    textField.getStyleClass().add("left-border");
                } else if (row % 3 == 2 && col % 3 == 2)
                {
                    // we need a special border to highlight the bottom right
                    textField.getStyleClass().add("bottom-right-border");
                }
                else if ((col % 3 == 2 && row % 3 == 0) || (col % 3 == 2 && row % 3 == 1)) { 
                    // Thick right border
                    textField.getStyleClass().add("right-border");
                }
                else if ((col % 3 == 1 && row % 3 == 2) || (col % 3 == 0 && row % 3 == 2)) { 
                    // Thick bottom border
                    textField.getStyleClass().add("bottom-border");
                } else {
                    // add an inner-border class
                    textField.getStyleClass().add("inner-border");
                }

                textField.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    textField.positionCaret(textField.getText().length());
                    String id = textField.getId();
                    String[] parts = id.split("-");
                    int r = Integer.parseInt(parts[0]);
                    int c = Integer.parseInt(parts[1]);
                    if (event.getCode() == KeyCode.LEFT) {
                        System.out.println("LEFT");
                        // find the textfield to the left of this one and set the focus to it
                        
                        int c2 = (c-1+SIZE)%SIZE;

                        TextField tf = textFields[r][c2];
                        tf.requestFocus();
                        tf.positionCaret(tf.getText().length());
                        setHighlight(tf);
                    } else if (event.getCode() == KeyCode.RIGHT) {
                        System.out.println("RIGHT");
                        // find the textfield to the right of this one and set the focus to it
                        int c2 = (c+1)%SIZE;
                        TextField tf = textFields[r][c2];
                        tf.requestFocus();
                        tf.positionCaret(tf.getText().length());
                        setHighlight(tf);
                    } else if (event.getCode() == KeyCode.UP) {
                        System.out.println("UP");
                        // find the textfield above this one and set the focus to it
                        int r2 = (r-1+SIZE)%SIZE;
                        TextField tf = textFields[r2][c];
                        tf.requestFocus();
                        tf.positionCaret(tf.getText().length());
                        setHighlight(tf);
                    } else if (event.getCode() == KeyCode.DOWN) {
                        System.out.println("DOWN");
                        // find the textfield below this one and set the focus to it
                        int r2 = (r+1)%SIZE;
                        TextField tf = textFields[r2][c];
                        tf.requestFocus();
                        tf.positionCaret(tf.getText().length());
                        setHighlight(tf);
                    }
                });

                // add a handler for when we select a textfield
                textField.setOnMouseClicked(event -> {
                    // move the caret to the end of the text
                    textField.positionCaret(textField.getText().length());
                    // toggle highlighting
                    setHighlight(textField);
                });

                // add a handler for when we lose focus on a textfield
                textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue)
                    {
                        // remove the highlight when we lose focus
                        textField.getStyleClass().remove("text-field-selected");
                    }
                });

                // RIGHT-CLICK handler
                // add handler for when we RIGHT-CLICK a textfield
                // to bring up a selection of possible values
                textField.setOnContextMenuRequested(event -> {
                    // change the textfield background to red while keeping the rest of the css the same
                    textField.getStyleClass().add("text-field-highlight");
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Possible values");
                    // find the row and column of the text field
                    String id = textField.getId();
                    String[] parts = id.split("-");
                    int r = Integer.parseInt(parts[0]);
                    int c = Integer.parseInt(parts[1]);
                    Set<Integer> vals = board.getPossibleValues(r, c);
                    String t = vals.toString();
                    t = t.substring(1, t.length()-1);
                    alert.setContentText("Possible values for this square: " + t);
                    alert.showAndWait();
                    textField.getStyleClass().remove("text-field-highlight");
                });

                // using a listener instead of a KEY_TYPED event handler
                // KEY_TYPED requires the user to hit ENTER to trigger the event
                textField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (updatingBoard)
                    {
                        updateErrors();

                        return;
                    }

                    String id = textField.getId();
                    String[] parts = id.split("-");
                    int r = Integer.parseInt(parts[0]);
                    int c = Integer.parseInt(parts[1]);
                    
                    if (newValue.length() == 0 && oldValue.length() > 0)
                    {
                        textField.setText("");
                        board.makeMove(r, c, 0);

                        updateErrors();

                        return;
                    }

                    if (newValue.length() == 0)
                    {
                        textField.setText("");
                        board.makeMove(r, c, 0);

                        updateErrors();

                        return;
                    }

                    String first = newValue.substring(0, 1);
                    String last = newValue.substring(newValue.length() - 1);

                    if (last.matches("[1-9]?")) {
                        textField.setText(last);
                        
                        int value = Integer.parseInt(last);
                        board.makeMove(r, c, value);
                    } else if (first.matches("[1-9]?")) {
                        textField.setText(first);

                        int value = Integer.parseInt(first);
                        board.makeMove(r, c, value);

                    } else {
                        textField.setText("");
                    }

                    updateErrors();
                });
            }
        }

        // add key listener to the root node to grab ESC keys
        root.setOnKeyPressed(event -> {
            System.out.println("Key pressed: " + event.getCode());
            switch (event.getCode())
            {
                // check for the ESC key
                case ESCAPE:
                    // clear all the selected text fields
                    for (int row = 0; row < SIZE; row++)
                    {
                        for (int col = 0; col < SIZE; col++)
                        {
                            TextField textField = textFields[row][col];
                            textField.getStyleClass().remove("text-field-selected");
                        }
                    }
                    break;
                default:
                    System.out.println("you typed key: " + event.getCode());
                    break;
                
            }
        });

        Scene scene = new Scene(root, width, height);

        URL styleURL = getClass().getResource("/style.css");
		String stylesheet = styleURL.toExternalForm();
		scene.getStylesheets().add(stylesheet);
        primaryStage.setTitle("Sudoku");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
        	System.out.println("oncloserequest");
        });
    }

    private void updateBoard()
    {
        updatingBoard = true;
        for (int row = 0; row < SIZE; row++)
        {
            for (int col = 0; col < SIZE; col++)
            {
                TextField textField = textFields[row][col];
                int value = board.getCell(row, col);
                if (value > 0)
                {
                    textField.setText(Integer.toString(value));
                }
                else
                {
                    textField.setText("");
                }
            }
        }
        updatingBoard = false;
    }

    private MenuBar createMenuBar(Stage primaryStage)
    {
        MenuBar menuBar = new MenuBar();
    	menuBar.getStyleClass().add("menubar");

        //
        // File Menu
        //
    	Menu fileMenu = new Menu("File");

        // add a menu item which allows the user to generate a random board
        addMenuItem(fileMenu, "Generate random board", () -> {
            System.out.println("Generate random board");
            board.generateMinimumSolvable();
            board.clearMoves();
            hideAllHints();
            unfixBoard();
            updateBoard();
            fixBoard();
        });

        addMenuItem(fileMenu, "Load from file", () -> {
            System.out.println("Load from file");
            FileChooser fileChooser = new FileChooser();
            // XXX: this is a hack to get the file chooser to open in the right directory
            // we should probably have a better way to find this folder than a hard coded path
			fileChooser.setInitialDirectory(new File("../puzzles"));
			File sudokuFile = fileChooser.showOpenDialog(primaryStage);
            if (sudokuFile != null)
            {
                System.out.println("Selected file: " + sudokuFile.getName());
                
                try {
                    board = Board.loadBoard(new FileInputStream(sudokuFile));
                    board.clearMoves();
                    unfixBoard();
                    updateBoard();
                    fixBoard();
                    // loop throught text fields removing the hint class
                    hideAllHints();
                } catch (Exception e) {
                    // pop up and error window
                    Alert alert = new Alert(AlertType.ERROR);
    	            alert.setTitle("Unable to load sudoku board from file "+ sudokuFile.getName());
    	            alert.setHeaderText(e.getMessage());
                    alert.setContentText(e.getMessage());
                    e.printStackTrace();
                    if (e.getCause() != null) e.getCause().printStackTrace();
                    
                    alert.showAndWait();
                }
            }
        });

        // save to text
        addMenuItem(fileMenu, "Save to text", () -> {
            System.out.println("Save puzzle to text");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("../puzzles"));
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null)
            {
                System.out.println("Selected file: " + file.getName());
                try {
                    //TODO: check if the file already exists, and ask the user if they want to overwrite
                    writeToFile(file, board.toString());
                } catch (Exception e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Unable to save to file");
                    alert.setHeaderText("Unsaved changes detected!");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        });
        
        addMenuItem(fileMenu, "Print Board", () -> {
            // Debugging method that just prints the board
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Board");
            alert.setHeaderText(null);
            alert.setContentText(board.toString());
            alert.showAndWait();
        });
        // add a separator to the fileMenu
        fileMenu.getItems().add(new SeparatorMenuItem());

        addMenuItem(fileMenu, "Exit", () -> {
            System.out.println("Exit");
            primaryStage.close();
        });

        menuBar.getMenus().add(fileMenu);

        //
        // Edit
        //
        Menu editMenu = new Menu("Edit");

        addMenuItem(editMenu, "Undo", () -> {
            try {
                board.undo();
            } catch (IllegalStateException e) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Unable to undo");
                alert.setHeaderText("No moves to undo");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
            updateBoard();
        });

        // add menu item to reset the board
        addMenuItem(editMenu, "Reset", () -> {
            System.out.println("Reset");
            resetBoard();
        });

        addMenuItem(editMenu, "Show values entered", () -> {
            System.out.println("Show all the values we've entered since we loaded the board");
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Values entered");
            alert.setHeaderText("Values entered since loading the board");
            alert.setContentText(board.getMovesArchive());
            alert.showAndWait();

        });

        menuBar.getMenus().add(editMenu);

        //
        // Hint Menu
        //
        Menu hintMenu = new Menu("Hints");

        addMenuItem(hintMenu, "Show hint", () -> {
            System.out.println("Show hint");
            showHints();
        });

        addMenuItem(hintMenu, "Hide used hints", () -> {
            System.out.println("Hide used hints");
            hideUsedHints();
        });

        addMenuItem(hintMenu, "Hide all hints", () -> {
            System.out.println("Hide all hints");
            hideAllHints();
        });

        // solve
        addMenuItem(hintMenu, "Solve", () -> {
            System.out.println("Solve");
            board.solve();
            updateBoard();
        });

        // check solvability
        addMenuItem(hintMenu, "Check solvability", () -> {
            System.out.println("Check solvability");
            if (board.getSolvable()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Solvability");
                alert.setHeaderText("This board is solvable");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Solvability");
                alert.setHeaderText("This board is not solvable");
                alert.showAndWait();
            }
        });

        // print number of solutions
        addMenuItem(hintMenu, "Print number of solutions", () -> {
            System.out.println("Print number of solutions");
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Number of solutions");
            alert.setHeaderText("Number of solutions: " + board.numSolutions());
            alert.showAndWait();
        });

        menuBar.getMenus().add(hintMenu);

        return menuBar;
    }

    private static void writeToFile(File file, String content) throws IOException
    {
        Files.write(file.toPath(), content.getBytes());
    }

    private void addMenuItem(Menu menu, String name, Runnable action)
    {
        MenuItem menuItem = new MenuItem(name);
        menuItem.getStyleClass().add("menuitem");
        menuItem.setOnAction(event -> action.run());
        menu.getItems().add(menuItem);
    }
        
    public static void main(String[] args) 
    {
        launch(args);
    }

    public void showHints() {
        // search through the board for squares that are not 0 and have only one possible value
        for (int row = 0; row < SIZE; row++)
        {
            for (int col = 0; col < SIZE; col++)
            {
                if (board.getCell(row, col) == 0)
                {
                    // get the possible values for this cell
                    // if there is only one possible value, highlight the cell
                    if (board.getPossibleValues(row, col).size() == 1)
                    {
                        System.out.println("Hint: " + row + ", " + col + " = " + board.getPossibleValues(row, col));
                        TextField textField = textFields[row][col];
                        if (!textField.getStyleClass().contains("text-field-hint")) {
                            textField.getStyleClass().add("text-field-hint");
                        }
                    }
                }
            }
        }
    }

    public void hideUsedHints() {
        // loop through the board and remove the hint class from all text fields which are not 0
        for (int row = 0; row < SIZE; row++)
        {
            for (int col = 0; col < SIZE; col++)
            {
                if (board.getCell(row, col) != 0)
                {
                    TextField textField = textFields[row][col];
                    textField.getStyleClass().remove("text-field-hint");
                }
            }
        }
    }

    public void hideAllHints() {
        // loop through the board and remove the hint class from all text fields
        for (int row = 0; row < SIZE; row++)
        {
            for (int col = 0; col < SIZE; col++)
            {
                TextField textField = textFields[row][col];
                textField.getStyleClass().remove("text-field-hint");
            }
        }
    }

    private void fixBoard() {
        // loop through the board and set the text fields to read only if the value is not 0
        for (int row = 0; row < SIZE; row++)
        {
            for (int col = 0; col < SIZE; col++)
            {
                TextField textField = textFields[row][col];
                if (board.getCell(row, col) != 0)
                {
                    textField.setEditable(false);
                    textField.getStyleClass().add("text-field-fixed");
                }
            }
        }
    }

    public void unfixBoard() {
        // loop through the board and set the text fields to write if the value is not 0
        for (int row = 0; row < SIZE; row++)
        {
            for (int col = 0; col < SIZE; col++)
            {
                TextField textField = textFields[row][col];
                textField.setEditable(true);
                textField.getStyleClass().remove("text-field-fixed");
            }
        }
    }

    // update errors adds the error class to any text field that has an impossible value
    private void updateErrors() {
        for (int row = 0; row < SIZE; row++)
        {
            for (int col = 0; col < SIZE; col++)
            {
                TextField textField = textFields[row][col];
                if (board.getCell(row, col) == 0) {
                    textField.getStyleClass().remove("text-field-error");
                } else {
                    Set<Integer> possibleValues = board.getPossibleValues(row, col);
                    if (!possibleValues.contains(board.getCell(row, col))) {
                        if (!textField.getStyleClass().contains("text-field-error")) {
                            textField.getStyleClass().add("text-field-error");
                        }
                    }
                    else {
                        textField.getStyleClass().remove("text-field-error");
                    }
                }
            }
        }
    }

    private void setHighlight(TextField textField) {
        for (int r = 0; r < SIZE; r++)
        {
            for (int c = 0; c < SIZE; c++)
            {
                textField.getStyleClass().remove("text-field-selected");
            }
        }
        if (!textField.getStyleClass().contains("text-field-selected")) {
            textField.getStyleClass().add("text-field-selected");
        }
    }

    public void resetBoard() {
        // loop through the board and set all the values which are not fixed to 0
        for (int row = 0; row < SIZE; row++)
        {
            for (int col = 0; col < SIZE; col++)
            {
                TextField textField = textFields[row][col];
                if (!textField.getStyleClass().contains("text-field-fixed")) {
                    textField.setText("");
                    board.rawSetCell(row, col, 0);
                }
            }
        }
    }
}
