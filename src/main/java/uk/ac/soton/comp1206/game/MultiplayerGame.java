package uk.ac.soton.comp1206.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;

public class MultiplayerGame extends Game implements CommunicationsListener {

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    /**
     * the initial current and incoming pieces
     */
    protected boolean piecesNotInitialised = true;

    /**
     * value of the next piece that is sent from the server
     */
    protected int nextPieceValue;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows, Communicator comm) {
        super(cols, rows, comm);
        communicator.addListener(this::receiveCommunication);
    }

    /**
     * Set the next piece value that you get from the server and initialise the current and incoming pieces
     * @param value value of the piece
     */
    public void setNextPieceValue(int value){
        nextPieceValue = value;

        if (piecesNotInitialised){
            logger.info("First piece being initialised");
            piecesNotInitialised = false;
            incomingPiece = spawnPiece();
            nextPiece();
        }
    }

    /**
     * Accessor method for nextPieceValue
     * @return the value of the next piece
     */
    public int getNextPieceValue(){
        return nextPieceValue;
    }

    @Override
    public void initialiseGame() {
        logger.info("Initialising game");
        communicator.send("PIECE");
    }

    @Override
    public void nextPiece(){
        setCurrentPiece(incomingPiece);
        communicator.send("PIECE");
        incomingPiece = spawnPiece();
        nextPieceListener.nextPiece(currentPiece, incomingPiece);
    }

    @Override
    public GamePiece spawnPiece(){
        return GamePiece.createPiece(getNextPieceValue());
    }

    /**
     * Handling messages received from server
     * @param communication the message that was received
     */
    public void receiveCommunication(String communication){
        if (communication.contains("PIECE")){
            // get the value of piece, convert it to string and set it as the nextPieceValue
            int value = Integer.parseInt(communication.split(" ")[1]);
            setNextPieceValue(value);
        }
    }

    @Override
    public void score(int linesToClear, int blocksToClear){
        int scoreToAdd = linesToClear * blocksToClear * getMultiplier().get() * 10; // calculate the score to add
        setScore( (getScore().get()) + scoreToAdd); // set new score
        communicator.send("SCORE " + getScore().get());
    }

    @Override
    public void reduceLives() {
        setLives(getLives().get()-1); // reduce life
        if (getLives().get() < 0 ){
            communicator.send("DIE"); // send message to server that player is dead if lives go below 0
        } else {
            communicator.send("LIVES " + getLives().get()); // update the server with the new amount of lives left
        }
    }
}