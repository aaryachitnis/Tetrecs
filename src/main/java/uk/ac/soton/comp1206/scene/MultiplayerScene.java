package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

import java.util.Timer;
import java.util.TimerTask;

public class MultiplayerScene extends ChallengeScene implements CommunicationsListener{
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    /**
     * Handles the logic of the game
     */
//    protected MultiplayerGame game;

    protected Leaderboard leaderboard;

    /**
     * Title of the scene
     */
    protected Text multiplayerTitle = new Text("Multiplayer");

    /**
     * To communicate with the server
     */
    protected Communicator communicator = gameWindow.getCommunicator();

    /**
     * Timer to ask for scores and lives every 2 seconds
     */
    Timer timer = new Timer();

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
        multiplayerTitle.getStyleClass().add("title");

        // top pane
        HBox topBox = new HBox(105); // Spacing between boxes
        topBox.getChildren().addAll(scoreBox, multiplayerTitle, livesBox);
        mainPane.setTop(topBox);
        topBox.setPadding(new Insets(10, 10, 10, 10));  // Top, Right, Bottom, Left padding

        // leaderboard
//        mainPane.setLeft(leaderboard);
//        leaderboard.getStyleClass().add("gameBox");

        // displaying PieceBoards for current and incoming pieces
        VBox pieceBoardBox = new VBox(20);
        incomingPiecesLabel.getStyleClass().add("incoming-pieces-heading");
        pieceBoardBox.getChildren().addAll(incomingPiecesLabel,currentPieceBoard, incomingPieceBoard);
        mainPane.setRight(pieceBoardBox);
        pieceBoardBox.setTranslateY(100);
        pieceBoardBox.setPadding(new Insets(20)); // 10 pixels padding

        // rotate current right piece if gameboard is right-clicked
        board.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                rotatePiece(false);
            }
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

    @Override
    public void initialise() {
        super.initialise();
        communicator.addListener(this::receiveCommunication);
        requestPlayersInfo(); // to request player info
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
        if (communication.contains("SCORES")){
            updatePlayersInfo(communication);
        }
    }

    public void updatePlayersInfo(String playersInfo){
        logger.info("Players info: " + playersInfo);
    }


}
