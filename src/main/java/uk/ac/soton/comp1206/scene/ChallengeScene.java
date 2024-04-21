package uk.ac.soton.comp1206.scene;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.Button;
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

import java.io.*;
import java.util.HashSet;


/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements NextPieceListener, LineClearedListener {

    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);

    /**
     * Handles the logic of the game
     */
    protected Game game;

    /**
     * Storing the value of score
     */
    protected Text scoreText = new Text();

    /**
     * Storing the value of level
     */
    protected Text levelText = new Text();

    /**
     * Storing the value of remaining lives
     */
    protected Text livesText = new Text();

    /**
     * Storing the value of the current local high score
     */
    protected Text highScoreText = new Text();

    /**
     * Label for score
     */
    protected Text scoreLabel = new Text("Score: ");

    /**
     * Label for level
     */
    protected Text levelLabel = new Text("Level: ");

    /**
     * Label for lives
     */
    protected Text livesLabel = new Text("Lives: ");

    /**
     * Label for high score
     */
    protected Text highScoreLabel = new Text("High Score:");

    /**
     * Label for incoming pieces
     */
    protected Text incomingPiecesLabel = new Text("Incoming:");

    /**
     * Title of the scene
     */
    protected Text title = new Text("Single Player");

    /**
     * For sound effects
     */
    protected Multimedia multimedia = new Multimedia();

    /**
     * Main gameboard where pieces will be placed
     */
    protected GameBoard board;

    /**
     * PieceBoard for the current game piece
     */
    protected PieceBoard currentPieceBoard = new PieceBoard(180,180);

    /**
     * PieceBoard for the incoming game piece
     */
    protected PieceBoard incomingPieceBoard = new PieceBoard(100,100);

    /**
     * y-coordinate of the selected block
     */
    protected int selectedRow = 0;

    /**
     * x-coordinate of the selected block
     */
    protected int selectedCol = 0;

    /**
     * Time bar to show how much time is remaining
     * Is an attribute and not local var so that the listener method implementation can access it too
     */
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
        challengePane.getStyleClass().add("challenge-background");
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

        // styling title
        title.getStyleClass().add("title");

        // top pane
        HBox topBox = new HBox(90); // Spacing between boxes
        topBox.getChildren().addAll(scoreBox, title, livesBox);
        mainPane.setTop(topBox);
        topBox.setPadding(new Insets(10, 10, 10, 10));  // Top, Right, Bottom, Left padding

        // displaying level
        levelText = new Text(); // stores the current level
        levelText.textProperty().bind(game.getLevel().asString()); // binding to the level IntegerProperty
        HBox levelBox = new HBox(levelLabel, levelText); // Horizontal box for the label and text
        levelLabel.getStyleClass().add("level"); // Styling label
        levelText.getStyleClass().add("level"); // Styling text

        // displaying high score
        highScoreText = new Text(getHighScore());
        VBox highScoreBox = new VBox(highScoreLabel, highScoreText);
        highScoreLabel.getStyleClass().add("hiscore"); // Styling label
        highScoreText.getStyleClass().add("hiscore"); // Styling text

        VBox leftBox = new VBox(10);
        leftBox.getChildren().add(levelBox);
        leftBox.getChildren().add(highScoreBox);
        mainPane.setLeft(leftBox);
        leftBox.setTranslateX(10);


        // displaying PieceBoards for current and incoming pieces
        VBox pieceBoardBox = new VBox(20);

        Button skipBtn = new Button("Skip piece");
        skipBtn.getStyleClass().add("skip-button");

        incomingPiecesLabel.getStyleClass().add("incoming-pieces-heading");
        pieceBoardBox.getChildren().addAll(skipBtn, incomingPiecesLabel,currentPieceBoard, incomingPieceBoard);
        mainPane.setRight(pieceBoardBox);
        pieceBoardBox.setTranslateY(60);
        pieceBoardBox.setPadding(new Insets(20)); // 10 pixels padding

        // rotate current right piece if gameboard is right-clicked
        board.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                rotatePiece(false);
            }
        });

        skipBtn.setOnAction(event -> {
            game.skipPiece();
        });

        // Set the time bar at the bottom of the pane
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
    protected void blockClicked(GameBlock gameBlock) {
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
        game.setGameOverListener(this::gameOver);
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
            game.stopTimer(); // stops timer
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
    public void moveBlock(int rowOffset, int colOffset) {
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
    public void blockSelected(int selectedCol, int selectedRow){
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

    /**
     * Displaying the timebar
     * @param time time allocated in which user must place a piece
     */
    public void timeBar(int time){
        long startTime = System.currentTimeMillis();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - startTime; // calculate how much time has passed
                if (elapsed >= time) {
                    this.stop();  // Stop the timer when the time is up
                    timeBar.setFill(Color.RED);  // Final colour is red (indicating time is up)
                } else {
                    double fraction = (double) elapsed / time;
                    // Slowly increase the red value of colour while decreasing the green value of colour by the same amount
                    Color currentColor = Color.color(fraction, 1 - fraction, 0);
                    timeBar.setFill(currentColor);
                    // Decrease width as time passes to show time decreasing
                    timeBar.setWidth(gameWindow.getWidth() * (1 - fraction));
                }
            }
        };
        timer.start();  // Start the timer
    }

    /**
     * Call the ScoresScene when lives remaining reach 0 and game is over
     */
    public void gameOver(){
        // go to the Scores Scene
        Platform.runLater(() -> {
            multimedia.stopBgMusic(); // stop bg music
            multimedia.playAudio("sounds/transition.wav"); // transition to score scene
            game.stopTimer(); // stop timer
            gameWindow.showScoreScene(game);
        });
    }

    /**
     * Gets the first line of the localScores text file to get the highest score
     * @return the score in the first line of file
     */
    public String getHighScore(){
        // Get the top high score when starting a game in the ChallengeScene and display it in the UI

        try (BufferedReader reader = new BufferedReader(new FileReader("localScores.txt"))) {
            String firstLine = reader.readLine();  // Read only the first line
            if (firstLine != null) {
                String[] parts = firstLine.split(":", 2); // Split the line into two parts
                return parts[1];
            }
        } catch (IOException e) {
            logger.info("Exception in getting high score");
            e.printStackTrace();
        }
        return "";
    }
}
