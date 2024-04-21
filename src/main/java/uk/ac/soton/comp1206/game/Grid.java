package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 * The Grid should be linked to a GameBoard for its display.
 */
public class Grid {

    /**
     * Instantiating logger
     */
    private static final Logger logger = LogManager.getLogger(Grid.class);

    private Multimedia multimedia = new Multimedia();

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D array with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Check whether a piece can be played in the block selected by the user
     * @param piece game piece
     * @param x the column where piece is played
     * @param y the row where piece is played
     * @return whether the piece can be palyed
     */
    public boolean canPlayPiece(GamePiece piece, int x, int y){
        logger.info("Checking if piece can be played");

        int[][] blocks = piece.getBlocks();

        // iterating over all blocks of a game piece
        for (int row = 0; row < 3; row++){
            for (int col = 0; col < 3; col++){

                // grid coordinates
                int gridX = x - 1 + col;
                int gridY = y - 1 + row;

                if (blocks[row][col] != 0) { // if the block of the piece isn't empty
                    if ( (gridX < getCols()) || (gridY < getRows())){ // checks if piece is within the boundary of the game grid
                        if (get(gridX, gridY) != 0){ // if piece already exists in this block
                            return false; // piece can't be played if it is
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        logger.info("Piece can be played");
        return true;
    }

    /**
     * Playing a piece by writing the value of the piece to the game grid
     * @param play this will equal true if the piece needs to be played and false if the piece only needs to be displayed
     * @param piece game piece to be played
     * @param x the column in which the centre of the game piece will be
     * @param y the row in which the centre of the game piece will be
     */
    public void playPiece(GamePiece piece, int x, int y, boolean play){

        // only play the "play.wav" sound if piece is meant to be played
        if (play){
            multimedia.playAudio("sounds/place.wav");
        }

        int[][] blocks = piece.getBlocks();

        for (int row = 0; row < 3; row++){
            for (int col = 0; col < 3; col++){

                // grid coordinates
                int gridX = x - 1 + col;
                int gridY = y - 1 + row;

                if (blocks[row][col] != 0){ // if the block of the piece isn't empty
                    set(gridX,gridY, piece.getValue()); // write the piece's value to the grid at gridX and gridY
                }
            }
        }
    }

}

