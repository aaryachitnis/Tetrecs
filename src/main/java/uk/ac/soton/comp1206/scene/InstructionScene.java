package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * This class is responsible for displaying the instructions of how to play TetrECS
 * Explains the aim of the game, what controls need to be used and dynamically displays all the game pieces
 */

public class InstructionScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Constructor to construct the instruction scene
     * @param gameWindow game window
     */
    public InstructionScene (GameWindow gameWindow){
        super(gameWindow);
    }

    /**
     * Builds and styles the layout of the instruction scene
     */
    @Override
    public void build(){
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var instructionPane = new StackPane();
        instructionPane.setMaxWidth(gameWindow.getWidth());
        instructionPane.setMaxHeight(gameWindow.getHeight());
        instructionPane.getStyleClass().add("instruction-background"); // setting background
        root.getChildren().add(instructionPane);

        var mainPane = new BorderPane();
        instructionPane.getChildren().add(mainPane);

        // Title
        Text title = new Text("How to play");
        title.getStyleClass().add("instructions-title");
        title.setTranslateX(350);

        // Description of the game
        Label description = new Label("TetrECS is fast paced block placing game. Aim of the game is to place pieces of blocks strategically to clear horizontal and vertical lines to increase your score. You lose a life if you don't place a piece before the timer runs out. Good luck!");
        description.setWrapText(true); // So that the text continues on the next line
        description.setPrefWidth(800);
        description.getStyleClass().add("description");

        // Instruction image
        Image insImage = new Image(getClass().getResource("/images/Instructions.png").toExternalForm());
        ImageView insImageView = new ImageView(insImage);
        insImageView.setFitHeight(300); // Set the height
        insImageView.setFitWidth(700); // Set the width
        insImageView.setPreserveRatio(true); // Preserve aspect ratio
        HBox instructions = new HBox(insImageView); // Box with the image view
        instructions.setAlignment(Pos.CENTER);

        // Displaying title, description and the instruction image
        VBox titleDescriptionIns = new VBox(title, description, instructions);
        mainPane.setTop(titleDescriptionIns);

        // Dynamically displaying game pieces
        // Title
        Text piecesTitle = new Text("Game pieces");
        piecesTitle.getStyleClass().add("title");
        piecesTitle.setTranslateX(320);

        // Boxes for the piece boards, one will contain 7 and one will contain 8
        HBox piecesSet1 = new HBox(10);
        HBox piecesSet2 = new HBox(10);

        // Adding pieceboards to the boxes
        for (int pieceNum = 0; pieceNum < 15; pieceNum++){
            if (pieceNum <= 6){
                piecesSet1.getChildren().add(displayPieces(pieceNum));
                logger.info("Piece added to piecesSet1");
            } else {
                piecesSet2.getChildren().add(displayPieces(pieceNum));
                logger.info("Piece added to piecesSet2");
            }
        }


        // Displaying game pieces title and pieceboards
        VBox gamePieces = new VBox(20);
        gamePieces.getChildren().add(piecesTitle);
        gamePieces.getChildren().add(piecesSet1);
        piecesSet1.setTranslateX(140);
        gamePieces.getChildren().add(piecesSet2);
        piecesSet2.setTranslateX(105);
        gamePieces.setPadding(new Insets(15));
        mainPane.setCenter(gamePieces);
    }

    /**
     * Initialises Instruction scene and contains event handler for when escape key is pressed
     */
    @Override
    public void initialise(){
        scene.setOnKeyPressed(this::escapePressed);
    }

    /**
     * Go back to the menu scene when escape key is pressed
     * @param event the event of escape key being pressed
     */
    public void escapePressed(KeyEvent event){
        if (event.getCode() == KeyCode.ESCAPE){
            logger.info("Escape pressed, going to menu scene");
            gameWindow.startMenu();
        }
    }

    /**
     * Creates a pieceboard and displays a gamepiece of the specified number
     * @param pieceNum number of the piece that needs to be displayed on the pieceboard
     * @return pieceboard with the piece displayed
     */
    public PieceBoard displayPieces(int pieceNum){
        var pieceBoard = new PieceBoard(65,65);
        pieceBoard.showPiece(GamePiece.createPiece(pieceNum));
        logger.info("Piece board with game piece created");
        return pieceBoard;
    }





}
