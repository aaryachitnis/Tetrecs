package uk.ac.soton.comp1206.scene;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;
import javafx.geometry.Insets;

import java.util.HashSet;


/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements NextPieceListener, LineClearedListener {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected Game game;

    /**
     * Storing the value of score
     */
    public Text scoreText = new Text();

    /**
     * Storing the value of level
     */
    public Text levelText = new Text();

    /**
     * Storing the value of remaining lives
     */
    public Text livesText = new Text();

    /**
     * Label for score
     */
    public Text scoreLabel = new Text("Score: ");

    /**
     * Label for level
     */
    public Text levelLabel = new Text("Level: ");

    /**
     * Label for lives
     */
    public Text livesLabel = new Text("Lives: ");

    /**
     * Title of the scene
     */
    public Text title = new Text("Single Player");

    /**
     * NextPieceListener field to listen to new pieces generated inside the Game
     */
    public NextPieceListener nextPieceListener;

    /**
     * For sound effects
     */
    private Multimedia multimedia = new Multimedia();

    protected GameBoard board;

    /**
     * PieceBoard for the current game piece
     */
    private PieceBoard currentPieceBoard = new PieceBoard(180,180);

    /**
     * PieceBoard for the incoming game piece
     */
    private PieceBoard incomingPieceBoard = new PieceBoard(120,120);

    /**
     * y-coordinate of the selected block
     */
    private int selectedRow = 0;

    /**
     * x-coordinate of the selected block
     */
    private int selectedCol = 0;

    protected Rectangle timeBar = new Rectangle(gameWindow.getWidth(),20);

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);

        // Play background music
        multimedia.playBgMusic("music/game.wav");

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);

        // displaying score
        scoreText = new Text(); // stores the score
        scoreText.textProperty().bind(game.getScore().asString()); // binding to the score IntegerProperty
        HBox scoreBox = new HBox(scoreLabel, scoreText); // Horizontal box for the label and text
        scoreLabel.getStyleClass().add("score"); // styling score label
        scoreText.getStyleClass().add("score"); // styling score text

        // displaying lives
        livesText = new Text(); // stores the number of remaining lives
        livesText.textProperty().bind(game.getLives().asString()); // binding to the lives IntegerProperty
        HBox livesBox = new HBox(livesLabel, livesText); // Horizontal box for the label and text
        livesLabel.getStyleClass().add("lives"); // Styling label
        livesText.getStyleClass().add("lives"); // Styling text

        // displaying level
        levelText = new Text(); // stores the current level
        levelText.textProperty().bind(game.getLevel().asString()); // binding to the level IntegerProperty
        HBox levelBox = new HBox(levelLabel, levelText); // Horizontal box for the label and text
        levelLabel.getStyleClass().add("level"); // Styling label
        levelText.getStyleClass().add("level"); // Styling text
        mainPane.setLeft(levelBox);

        // styling title
        title.getStyleClass().add("title");

        // top pane
        HBox topBox = new HBox(100); // Spacing between boxes
        topBox.getChildren().addAll(scoreBox, title, livesBox);
        mainPane.setTop(topBox);

        // displaying PieceBoards for current and incoming pieces
        VBox pieceBoardBox = new VBox(20);
        pieceBoardBox.getChildren().addAll(currentPieceBoard, incomingPieceBoard);
        mainPane.setRight(pieceBoardBox);
        pieceBoardBox.setTranslateY(150);
        pieceBoardBox.setPadding(new Insets(20)); // 10 pixels padding

        // rotate current right piece if gameboard is right-clicked
        board.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                rotatePiece(false);
            }
        });

        // Time bar

//        timeBar.setFill(Color.GREEN);
        mainPane.setBottom(timeBar);

        game.setGameLoopListener((e) -> {
            Platform.runLater(() ->
                    timeBar(game.getTimerDelay().get()));
        });

        // rotate piece right if currentPieceBoard is left-clicked
        currentPieceBoard.setOnMouseClicked(event -> rotatePiece(false));

        // swap pieces if incomingPieceBoard is left-clicked
        incomingPieceBoard.setOnMouseClicked(event -> swapPieces());

    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);

        // listeners
        game.setNextPieceListener(this::nextPiece);
        game.setLineClearedListener(this::lineCleared);
        game.setGameLoopListener(this::timeBar);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        scene.setOnKeyPressed(this::handleKey);
    }

    /**
     * Implementation for NextPieceListener's method nextPiece()
     * Displays the current and incoming piece on their respective pieceboards
     * @param currentPiece the current gamepiece that the user needs to place
     * @param incomingPiece the next gamepiece that will follow
     */
    public void nextPiece(GamePiece currentPiece, GamePiece incomingPiece){
        currentPieceBoard.showPiece(currentPiece);
        incomingPieceBoard.showPiece(incomingPiece);
    }

    /**
     * Rotate the current gamepiece and display it on the currentPieceBoard
     * @param left = true if piece needs to be rotated left
     */
    public void rotatePiece(boolean left){
        multimedia.playAudio("sounds/rotate.wav");
        game.rotateCurrentPiece(left);
        currentPieceBoard.showPiece(game.getCurrentPiece());
    }

    /**
     * Swaps current piece and incoming piece
     */
    public void swapPieces(){
        multimedia.playAudio("sounds/rotate.wav");
        game.swapCurrentPiece();
        currentPieceBoard.showPiece(game.getCurrentPiece());
        incomingPieceBoard.showPiece(game.getIncomingPiece());
    }

    /**
     * Handles what happens when a key is pressed
     * @param event the event of pressing the escape key
     */
    public void handleKey(KeyEvent event){

        // for exiting challenge scene and going back to the menu scene
        if (event.getCode() == KeyCode.ESCAPE){ // escape key pressed
            logger.info("Escape pressed, going to menu scene");
            multimedia.stopBgMusic(); // stop background music
            gameWindow.cleanup(); // clean up the window before going back to the menu scene
            gameWindow.startMenu();
        }

        // for swapping current and incoming piece
        if ( (event.getCode() == KeyCode.SPACE) || (event.getCode() == KeyCode.X) ){
            logger.info("Swapping piece");
            swapPieces();
        }

        // for rotating the current piece right
        if ( (event.getCode() == KeyCode.Q) || (event.getCode() == KeyCode.Z) || (event.getCode() == KeyCode.OPEN_BRACKET)){
            logger.info("Rotating piece to the right");
            rotatePiece(false);
        }

        // for rotating the current piece left
        if ( (event.getCode() == KeyCode.E) || (event.getCode() == KeyCode.C) || (event.getCode() == KeyCode.CLOSE_BRACKET)){
            logger.info("Rotating piece to the left");
            rotatePiece(true);
        }

        // for dropping a piece on the board
        if ( (event.getCode() == KeyCode.ENTER) || (event.getCode() == KeyCode.X)){
            logger.info("Dropping piece");
            blockSelected(selectedCol, selectedRow);
        }

        // cursor
        if ( (event.getCode() == KeyCode.UP) || (event.getCode() == KeyCode.W) ){
            // move up
            moveBlock(-1, 0);
        } else if ((event.getCode() == KeyCode.RIGHT) || (event.getCode() == KeyCode.D)) {
            // move right
            moveBlock(0, 1);
        } else if ( (event.getCode() == KeyCode.LEFT) || (event.getCode() == KeyCode.A) ) {
            // move left
            moveBlock(0, -1);
        } else if ( (event.getCode() == KeyCode.DOWN) || (event.getCode() == KeyCode.S) ) {
            // move down
            moveBlock(1, 0);
        }
    }

    /**
     * Updates the selectedRow and selectedColumn
     * Displays the hover effect over the selected block
     * @param rowOffset number of places to move on the y-axis
     * @param colOffset number of places to move on the x-axis
     */
    private void moveBlock(int rowOffset, int colOffset) {
        int newRow = selectedRow + rowOffset;
        int newCol = selectedCol + colOffset;

        if ( (newRow>=0) && (newCol>=0) && (newRow<5) && (newCol<5) ){
            board.getBlock(selectedCol, selectedRow).setHoverOn(false); // removing hover effect of the previous block
            selectedRow = newRow;
            selectedCol = newCol;
            board.getBlock(selectedCol, selectedRow).setHoverOn(true); // adding hover effect
        }

    }

    /**
     * Handles piece being played using keys
     * @param selectedCol column number of the block currently selected
     * @param selectedRow row number of the block currently selected
     */
    private void blockSelected(int selectedCol, int selectedRow){
        logger.info("Dropping piece at the selected block");
        game.blockClicked(board.getBlock(selectedCol, selectedRow)); // playing piece at block selected using keys
    }

    /**
     * Calls the fadeOut method in GameBoard
     * @param blocksToClear HashSet containing coordinates of all the blocks that need to be cleared
     */
    public void lineCleared(HashSet<GameBlockCoordinate> blocksToClear){
        board.fadeOut(blocksToClear);
    }

    public void timeBar(int time){
        logger.info("showing time bar");
        long startTime = System.currentTimeMillis();
        long endTime = startTime + time;

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - startTime;
                if (elapsed >= time) {
                    this.stop();  // Stop the timer when the time is up
                    timeBar.setFill(Color.RED);  // Final color to indicate time is up
                } else {
                    double fraction = (double) elapsed / time;
                    // Interpolate the color from green (0,1,0) to red (1,0,0)
                    Color currentColor = Color.color(fraction, 1 - fraction, 0);
                    timeBar.setFill(currentColor);
                    timeBar.setWidth(gameWindow.getWidth() * (1 - fraction));  // Decrease width as time passes
                }
            }
        };
        timer.start();  // Start the timer
    }

}
