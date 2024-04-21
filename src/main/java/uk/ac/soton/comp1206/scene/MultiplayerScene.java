package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Multiplayer game scene. Holds the UI for the multiplayer game mode in the game.
 */
public class MultiplayerScene extends ChallengeScene{
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    /**
     * Handles the logic of the game
     */
    protected MultiplayerGame multiGame;

    /**
     * Title of the scene
     */
    protected Text multiplayerTitle = new Text("Multiplayer");

    /**
     * To send messages to the server
     */
    protected Communicator communicator = gameWindow.getCommunicator();

    /**
     * Timer to ask for player info every 2 seconds
     */
    Timer timer = new Timer();

    /**
     * Observable ArrayList containing the player name, score and lives
     */
    protected final ObservableList<String> playersInfoList = FXCollections.observableArrayList();

    /**
     * Bindable property in which playerInfoList will be wrapped
     */
    protected SimpleListProperty<String> playersInfo ;

    /**
     * Leaderboard component to show players scores
     */
    protected Leaderboard leaderboard = new Leaderboard();

    /**
     * Create a new Multiplayer game scene
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Multiplayer Scene");
    }

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

        communicator.addListener(this::receiveCommunication);

        board = new GameBoard(multiGame.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);

        // Play background music
        multimedia.playBgMusic("music/game.wav");

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);

        // displaying score
        scoreText = new Text(); // stores the score
        scoreText.textProperty().bind(multiGame.getScore().asString()); // binding to the score IntegerProperty
        HBox scoreBox = new HBox(scoreLabel, scoreText); // Horizontal box for the label and text
        scoreLabel.getStyleClass().add("score"); // styling score label
        scoreText.getStyleClass().add("score"); // styling score text

        // displaying lives
        livesText = new Text(); // stores the number of remaining lives
        livesText.textProperty().bind(multiGame.getLives().asString()); // binding to the lives IntegerProperty
        HBox livesBox = new HBox(livesLabel, livesText); // Horizontal box for the label and text
        livesLabel.getStyleClass().add("lives"); // Styling label
        livesText.getStyleClass().add("lives"); // Styling text

        // styling title
        multiplayerTitle.getStyleClass().add("title");

        // top pane
        HBox topBox = new HBox(105); // Spacing between boxes
        topBox.getChildren().addAll(scoreBox, multiplayerTitle, livesBox);
        mainPane.setTop(topBox);
        topBox.setPadding(new Insets(10, 10, 10, 10));  // Top, Right, Bottom, Left padding

        // displaying PieceBoards for current and incoming pieces
        VBox pieceBoardBox = new VBox(20);
        incomingPiecesLabel.getStyleClass().add("incoming-pieces-heading");
        pieceBoardBox.getChildren().addAll(incomingPiecesLabel,currentPieceBoard, incomingPieceBoard);

        // right box
        VBox rightBox = new VBox(30);
        rightBox.getChildren().add(leaderboard);
        rightBox.getChildren().add(pieceBoardBox);

        mainPane.setRight(rightBox);
        rightBox.setPadding(new Insets(20)); // 20 pixels padding


        // rotate current right piece if gameboard is right-clicked
        board.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                rotatePiece(false);
            }
        });

        // Set the time bar at the bottom of the pane
        mainPane.setBottom(timeBar);

        multiGame.setGameLoopListener((e) -> {
            Platform.runLater(() ->
                    timeBar(multiGame.getTimerDelay().get()));
        });

        // rotate piece right if currentPieceBoard is left-clicked
        currentPieceBoard.setOnMouseClicked(event -> rotatePiece(false));

        // swap pieces if incomingPieceBoard is left-clicked
        incomingPieceBoard.setOnMouseClicked(event -> swapPieces());

    }

    @Override
    public void initialise() {
        multiGame.start();
        requestPlayersInfo();
        scene.setOnKeyPressed(this::handleKey);
    }

    @Override
    public void setupGame(){
        logger.info("Starting a new multiplayer challenge");

        //Start new game
        multiGame = new MultiplayerGame(5, 5, communicator);

        // listeners
        multiGame.setNextPieceListener(this::nextPiece);
        multiGame.setLineClearedListener(this::lineCleared);
        multiGame.setGameLoopListener(this::timeBar);
        multiGame.setGameOverListener(this::gameOver);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    @Override
    protected void blockClicked(GameBlock gameBlock) {
        multiGame.blockClicked(gameBlock);
    }

    /**
     * Rotate the current gamepiece and display it on the currentPieceBoard
     * @param left = true if piece needs to be rotated left
     */
    @Override
    public void rotatePiece(boolean left){
        multimedia.playAudio("sounds/rotate.wav");
        multiGame.rotateCurrentPiece(left);
        currentPieceBoard.showPiece(multiGame.getCurrentPiece());
    }

    /**
     * Swaps current piece and incoming piece
     */
    @Override
    public void swapPieces(){
        multimedia.playAudio("sounds/rotate.wav");
        multiGame.swapCurrentPiece();
        currentPieceBoard.showPiece(multiGame.getCurrentPiece());
        incomingPieceBoard.showPiece(multiGame.getIncomingPiece());
    }

    /**
     * Handles what happens when a key is pressed
     * @param event the event of pressing the escape key
     */
    @Override
    public void handleKey(KeyEvent event){

        // for exiting challenge scene and going back to the menu scene
        if (event.getCode() == KeyCode.ESCAPE){ // escape key pressed
            logger.info("Escape pressed, going to menu scene");
            multimedia.stopBgMusic(); // stop background music
            communicator.send("DIE");
            communicator.send("PART"); // Leaves the channel
            multiGame.stopTimer(); // stops timer
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
     * Call the ScoresScene when lives remaining reach 0 and game is over
     */
    @Override
    public void gameOver(){
        // go to the Scores Scene
        Platform.runLater(() -> {
            multimedia.stopBgMusic(); // stop bg music
            multimedia.playAudio("sounds/transition.wav"); // transition to score scene
            multiGame.stopTimer(); // stop timer
            gameWindow.showScoreScene(multiGame);
        });
    }

        public void requestPlayersInfo(){
        TimerTask getPlayerInfo = new TimerTask() {
            public void run() {
                communicator.send("SCORES");
            }
        };
        timer.scheduleAtFixedRate(getPlayerInfo, 0, 2000L); // channels will be requested every 2 seconds
    }

    /**
     * Receiving messages from the sever
     * @param communication communication sent
     */
    public void receiveCommunication(String communication){
        if (communication.contains("SCORES")){
            String[] playerInfo = communication.split(" "); // to remove the "SCORES" part
            updatePlayersInfo(playerInfo[1]);
        }
    }

    /**
     * Updates the player info in the list
     * @param info player info sent by server
     */
    public void updatePlayersInfo(String info){
        playersInfoList.clear();

        String[] playerline = info.split("\n");
        playersInfoList.addAll(Arrays.asList(playerline));
        playersInfo = new SimpleListProperty<>(playersInfoList);
        logger.info("playersInfoList size: " + playersInfoList.size() + " playersInfo size: " + playersInfo.size());
        for (String temp : playersInfoList){
            logger.info("contents of AL: " + temp);
        }
        leaderboard.getPlayersInfoListProperty().bind(playersInfo);
        Platform.runLater(() -> leaderboard.reveal());
    }
}
