package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * NextPieceListener listens to when the next piece has been generated so that Pieceboards can be updated
 */
public interface NextPieceListener {

    /**
     * Handles when next piece is generated
     * @param currentPiece piece that needs to be displayed on the current pieceboard
     * @param incomingPiece piece that needs to be displayed on the incoming pieceboard
     */
    void nextPiece(GamePiece currentPiece, GamePiece incomingPiece);
}
