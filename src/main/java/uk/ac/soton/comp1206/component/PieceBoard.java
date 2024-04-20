package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;

public class PieceBoard extends GameBoard{


    public PieceBoard(double width, double height){
        super(3, 3, width, height); // 3x3 game board
    }

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
