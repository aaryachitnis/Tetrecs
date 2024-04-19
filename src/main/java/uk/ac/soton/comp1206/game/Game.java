package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.utility.Multimedia;

import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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

    /**
     * For playing sounds and background music
     */
    protected Multimedia multimedia = new Multimedia();

    /**
     * The piece that the user is interacting with
     */
    protected GamePiece currentPiece;

    /**
     * The next piece that will follow
     */
    protected GamePiece incomingPiece;

//    protected GamePiece incomingPiece = spawnPiece();

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
     * Bindable property for timer
     */
    protected IntegerProperty timerDelay = new SimpleIntegerProperty(12000);

    /**
     * Timer object
     */
    protected Timer timer;

    /**
     * TimerTask object
     */
    protected TimerTask timerTask;

    /**
     * NextPieceListener field
     */
    protected NextPieceListener nextPieceListener;

    /**
     * LineClearedListener field
     */
    protected LineClearedListener lineClearedListener;

    /**
     * GameLoopListener field
     */
    protected GameLoopListener gameLoopListener;

    /**
     * GameOverListener field
     */
    protected GameOverListener gameOverListener;

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
    public void setLineClearedListener(LineClearedListener listener){lineClearedListener = listener;}

    /**
     * Setting up listener for when time is running
     * @param listener listener
     */
    public void setGameLoopListener(GameLoopListener listener){gameLoopListener = listener;}

    /**
     * Calls the Scores scene
     * @param listener listener
     */
    public void setGameOverListener(GameOverListener listener){gameOverListener = listener;}

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
        lives.set(newLives);
    }


    protected Communicator communicator;

    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows, Communicator comm) {
        this.communicator = comm;
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
        startTimer();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        incomingPiece = spawnPiece();
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

        // clearing blocks
        if (!blocksToClear.isEmpty()){
            multimedia.playAudio("sounds/clear.wav"); // playing the sound for cleared lines
            clearBlocks(blocksToClear);
            lineClearedListener.lineCleared(blocksToClear); // sending blocksToClear to lineClearedListener for fadeOut effect
        } else {
            logger.info("No blocks to clear");
        }

        restartTimer();
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

    /**
     * Updates timerDelay
     */
    public void setTimerDelay(){
        int delay = 12000 - 500 * getLevel().get();
//        logger.info("timer delay: " + delay);
        // if delay is less than 2500, set timerDelay to 2500, otherwise the calculated delay
        if (delay >= 2500){
            timerDelay.set(delay);
        } else {
            timerDelay.set(2500);
        }
//        logger.info("Timer delay set");

        gameLoopListener.setOnGameLoop(getTimerDelay().get()); // send the timer delay to the listener
    }

    /**
     * Accessor method for timerDelay
     * @return timerDelay
     */
    public IntegerProperty getTimerDelay(){
        return timerDelay;
    }

    /**
     * Handles what must be done when the timer reaches 0
     * Lose a life, current piece is discarded, multiplier is reset to 1 and timer is reset
     */
    public void gameLoop(){
//        logger.info("timer reached 0");

        // lose a life
        multimedia.playAudio("sounds/lifelose.wav"); // play life lost sound
        setLives(getLives().get()-1);
        if (getLives().get() < 0){ // if lives left are 0, end the game
            logger.info("Game over");
            gameOverListener.onGameOver(); // go to the scores scene
        }

        // current piece is replaced with the incoming piece which is replaced by a new piece
//        logger.info("Discarding current piece");
        nextPiece();

        // reset multiplier to 1
        setMultiplier(1);

        // resetting timer
//        logger.info("Resetting timer");
        startTimer(); // restart timer
    }

    /**
     * Starts the timer and calls gameLoop method when the timer runs out
     */
    public void startTimer(){
        // TODO: gameLoop shouldnt be called if piece is placed, start timer after piece has been placed?
//        logger.info("timer started");
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                gameLoop();
            }
        };
        setTimerDelay();
        timer.schedule(timerTask, getTimerDelay().get());
    }

    /**
     * Stops and purges the timer before starting it again
     */
    public void restartTimer(){
//        logger.info("restarting timer");
        if (timer != null){
            timer.cancel();
            timer.purge();
        }
        startTimer();
    }

    public void stopTimer(){
        logger.info("Stopped timer");
        timer.cancel();
    }

}