package uk.ac.soton.comp1206.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;

public class MultiplayerGame extends Game implements CommunicationsListener {

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    protected boolean piecesNotInitialised = true;

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

    public void setNextPieceValue(int value){
        nextPieceValue = value;

        if (piecesNotInitialised){
            logger.info("First piece being initialised");
            piecesNotInitialised = false;
            incomingPiece = spawnPiece();
            nextPiece();
        }
    }

    public int getNextPieceValue(){
        return nextPieceValue;
    }

    @Override
    public void initialiseGame() {
        logger.info("Initialising game");
        communicator.send("PIECE");
        requestPlayersInfo();
    }

    @Override
    public void nextPiece(){
        logger.info("Next piece generated");
        setCurrentPiece(incomingPiece);
        communicator.send("PIECE");
        incomingPiece = spawnPiece();
        nextPieceListener.nextPiece(currentPiece, incomingPiece);
    }

    @Override
    public GamePiece spawnPiece(){
        logger.info("Spawning piece of value: " + getNextPieceValue());
        return GamePiece.createPiece(getNextPieceValue());
    }

    public void requestPlayersInfo(){
        TimerTask getPlayerInfo = new TimerTask() {
            public void run() {
                communicator.send("SCORES");
            }
        };
        timer.scheduleAtFixedRate(getPlayerInfo, 0, 2000L); // channels will be requested every 2 seconds
    }

    public void receiveCommunication(String communication){
        if (communication.contains("PIECE")){
            // get the value of piece, convert it to string and set it as the nextPieceValue
            int value = Integer.parseInt(communication.split(" ")[1]);
            setNextPieceValue(value);
        } else if (communication.contains("SCORES")){
            updatePlayersInfo(communication);
        }
    }

    public void updatePlayersInfo(String playersInfo){
        logger.info("Players info: " + playersInfo);
    }

}