package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * GameBoards for current and incoming pieces
 */
public class PieceBoard extends GameBoard{

    /**
     * Constructs a 3x3 Gameboard
     * @param width width
     * @param height height
     */
    public PieceBoard(double width, double height){
        super(3, 3, width, height); // 3x3 game board
    }

    /**
     * Displays the piece on the board
     * @param piece piece to display
     */
    public void showPiece(GamePiece piece){
        // clear the grid
        for (int row = 0; row < 3; row++){
            for (int col = 0; col < 3; col++){
                grid.set(col, row, 0);
            }
        }
        // display the piece
        grid.playPiece(piece, 1, 1, false); // play piece by the centre
    }
}
