package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
//import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.utility.Multimedia;

import java.util.HashSet;
import java.util.Random;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    private Multimedia multimedia = new Multimedia();

    /**
     * The piece that the user is interacting with
     */
    protected GamePiece currentPiece;

    /**
     * The next piece that will follow
     */
    protected GamePiece incomingPiece = spawnPiece();

    /**
     * Bindable property for score
     */
    protected IntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * Bindable property for level
     */
    protected IntegerProperty level = new SimpleIntegerProperty(0);

    /**
     * Bindable property for lives
     */
    protected IntegerProperty lives = new SimpleIntegerProperty(3);

    /**
     * Bindable property for multiplier
     */
    protected IntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * Set of the blocks that need to be cleared
     */
//    private HashSet<GameBlockCoordinate> blocksToClear = new HashSet<>();

    /**
     * NextPieceListener field
     */
    private NextPieceListener nextPieceListener;

    /**
     * LineClearedListener field
     */
//    private LineClearedListener lineClearedListener;

    /**
     * Accessor method for the set of blocks to clear
     * @return HashSet of the blocks that need to be cleared
     */
//    public HashSet<GameBlockCoordinate> getBlocksToClear(){
//        return blocksToClear;
//    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
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
     * Get the current piece that the user is interacting with
     * @return the current game piece
     */
    public GamePiece getCurrentPiece(){
        return currentPiece;
    }

    /**
     * Get the incoming piece
     * @return the incoming piece
     */
    public GamePiece getIncomingPiece(){
        return incomingPiece;
    }

    /**
     * Get the current score
     * @return current score
     */
    public IntegerProperty getScore(){
        return score;
    }

    /**
     * Get the current level
     * @return current level
     */
    public IntegerProperty getLevel(){
        return level;
    }

    /**
     * Get the amount of lives remaining
     * @return lives
     */
    public IntegerProperty getLives(){
        return lives;
    }

    /**
     * Get the score multiplier
     * @return multiplier
     */
    public IntegerProperty getMultiplier(){
        return multiplier;
    }

    /**
     * Sets the current piece to the incoming piece
     * @param piece next current piece
     */
    public void setCurrentPiece(GamePiece piece){
        currentPiece = piece;
    }

    /**
     * Sets incoming piece to next piece
     * @param piece next incoming piece
     */
    public void setIncomingPiece(GamePiece piece){
        incomingPiece = piece;
    }

    /**
     * Increases the score
     * @param newScore the updated score
     */
    public void setScore(int newScore){
        score.set(newScore);
    }

    /**
     * Increases or decreases the multiplier
     * @param newMultiplier the updated multiplier
     */
    public void setMultiplier(int newMultiplier){
        multiplier.set(newMultiplier);
    }

    /**
     * Increase the level
     * @param newLevel the new level
     */
    public void setLevel(int newLevel){
        level.set(newLevel);
    }

    /**
     * Reduce lives
     * @param newLives remaining lives
     */
    public void setLives(int newLives){
        level.set(newLives);
    }

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        currentPiece = spawnPiece();
        nextPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        logger.info("Block clicked, checking..");
        if (getGrid().canPlayPiece(getCurrentPiece(), x, y)){ // if the piece can be played
            getGrid().playPiece(getCurrentPiece(), x, y, true); // playing the piece
            afterPiece(); // to clear any line
            logger.info("Cleared any lines");
            // TODO: should nextPiece() be here instead
            logger.info("Next piece loading");
            nextPiece(); // loading the next piece
        } else {
            logger.info("Piece can't be played");
            multimedia.playAudio("sounds/fail.wav"); // play the fail sound if piece cant be plated
        }

    }

    /**
     * Calls the GamePiece.createPiece() method and uses random number generation to spawn a new piece
     * @return the created game piece
     */
    public GamePiece spawnPiece(){
        Random random = new Random();
        logger.info("Spawning new game piece");
        return GamePiece.createPiece(random.nextInt(15));
    }

    /**
     * Replaces the current piece with a new piece
     */
    public void nextPiece(){
        logger.info("Next piece generated");
        setCurrentPiece(incomingPiece);
        incomingPiece = spawnPiece();
        nextPieceListener.nextPiece(currentPiece, incomingPiece);
    }

    /**
     * Setting up listener for when the nextPiece needs to be called
     * @param listener listener
     */
    public void setNextPieceListener(NextPieceListener listener){
        this.nextPieceListener = listener;
    }

    /**
     * Setting up listener for when line is cleared
     * @param listener listener
     */
//    public void setLineClearedListener(LineClearedListener listener){lineClearedListener = listener;}


    /**
     * Called after a piece is played
     * Iterates over rows and columns and stores the number of lines to be cleared and the blocks that need to be cleared
     * Updates score and multiplier
     * Clears blocks
     */
    public void afterPiece(){
        int linesToClear = 0;
        HashSet<GameBlockCoordinate> blocksToClear = new HashSet<>();

        // checking rows
        for (int rows = 0; rows < getRows(); rows++){ // iterating over rows
            boolean canClear = false;

            for (int cols = 0; cols < getCols(); cols++){ // iterating over columns of that row
                if (getGrid().get(cols, rows) != 0){  // if the block contains a piece block
                    canClear = true;
                } else {
                    canClear = false;
                    break; // stop checking this row if a block's value is 0
                }
            }
            if (canClear){
                logger.info("Can clear row");
                linesToClear++;

                for (int x = 0; x < getCols(); x++){
                    GameBlockCoordinate block = new GameBlockCoordinate(x, rows);
                    blocksToClear.add(block); // adding all the blocks of this row to the set of blocks to be cleared
                }
            }
        }

        // checking columns
        for (int cols = 0; cols < getCols(); cols++){ // iterating over columns
            boolean canClear = false;

            for (int rows = 0; rows < getRows(); rows++){ // iterating over rows of that column
                if (getGrid().get(cols, rows) != 0){  // if the block contains a piece block
                    canClear = true;
                } else {
                    canClear = false;
                    break; // stop checking this column if a block's value is 0
                }
            }
            if (canClear){
                logger.info("Can clear column");
                linesToClear++;

                for (int y = 0; y < getCols(); y++){
                    GameBlockCoordinate block = new GameBlockCoordinate(cols, y);
                    blocksToClear.add(block); // adding all the blocks of this column to the set of blocks to be cleared
                }
            }
        }

        // updating score and multiplier
        if (linesToClear > 0 ){ // if any lines are cleared
            score(linesToClear, blocksToClear.size()); // update score
            setMultiplier((getMultiplier().get() + 1)); // increase the multiplier by 1
            updateLevel(); // updating level if score changes
            logger.info("Score updated to: " + getScore().get() + ". multiplier updated to: " + getMultiplier().get());
        } else {
            // no lines to clear therefore score stays the same
            setMultiplier(1); // reset multiplier to 1 as no lines were cleared
            logger.info("No lines cleared, score stays the same, multiplier reset");
        }


        // TODO: need to double check if this works after adding the next piece window
        // clearing blocks
        if (!blocksToClear.isEmpty()){
            clearBlocks(blocksToClear);
//            lineClearedListener.lineCleared(blocksToClear);
        } else {
            logger.info("No blocks to clear");
        }
    }

    /**
     * Sets the value of the blocks at the given coordinates to 0 ("clearing them")
     * Called in afterPiece()
     * @param blocksToClear HashSet containing GameBlockCoordinate of the blocks that need to be cleared
     */
    public void clearBlocks(HashSet<GameBlockCoordinate> blocksToClear){
        for (GameBlockCoordinate block : blocksToClear) { // iterate over the hash set
            getGrid().set(block.getX(), block.getY(), 0); // set value to 0
        }
        logger.info("Cleared blocks");
    }

    /**
     * Updates the score after each piece is played
     * Called in updateScoreAndMultiplier()
     * @param linesToClear number of lines cleared after playing a piece
     * @param blocksToClear number of blocks cleared after playing a piece
     */
    public void score(int linesToClear, int blocksToClear){
        int scoreToAdd = linesToClear * blocksToClear * getMultiplier().get() * 10; // calculate the score to add
        setScore( (getScore().get()) + scoreToAdd); // set new score
        logger.info("Score: " + getScore().get());
    }

    /**
     * Update the level when the score changes
     */
    public void updateLevel(){
        // increase level every 1000 points
        setLevel(getScore().get() / 1000);
    }

    /**
     * Rotates the current piece
     * If piece needs to be rotated left, perform a 90-degree rotation 3 times
     * @param left = true if the piece needs to be rotated left
     */
    public void rotateCurrentPiece(boolean left){
        if (left){
            getCurrentPiece().rotate(3);
        }
        getCurrentPiece().rotate();
    }

    /**
     * Swaps the current piece and incoming piece
     * Called if user wants to play the originally incoming piece before current piece
     */
    public void swapCurrentPiece(){
        logger.info("Swapping pieces");
        GamePiece temp = getCurrentPiece();
        setCurrentPiece(getIncomingPiece());
        setIncomingPiece(temp);
    }

}