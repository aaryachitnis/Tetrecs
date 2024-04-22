package uk.ac.soton.comp1206.scene;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Scores scene. Holds the high scores and is displayed after a Single Player game ends
 */
public class ScoreScene extends BaseScene implements CommunicationsListener {

    private static final Logger logger = LogManager.getLogger(ScoreScene.class);

    /**
     * High Score heading
     */
    private final Text highScoreTitle = new Text("High Scores");

    /**
     * Online Score sub-heading
     */
    private final Text onlineHighScoreTitle = new Text("Online Scores");

    /**
     * Local Score sub-heading
     */
    private final Text localHighScoreTitle = new Text("Remote Scores");

    /**
     * Game over text
     */
    private final Text gameOverTitle = new Text("Game Over");

    /**
     * ArrayList containing local names and scores
     */
    protected ArrayList<Pair<String, Integer>> scoresArrayList = new ArrayList<>() {};

    /**
     * ArrayList containing names and scores from the server
     */
    private final ObservableList<Pair<String, Integer>> remoteScoresArrayList = FXCollections.observableArrayList();

    /**
     * Wrapping the ArrayList scoresArrayList which contains the name and score in SimpleListProperty
     */
    protected final SimpleListProperty<Pair<String, Integer>> localScores = new SimpleListProperty<>(FXCollections.observableArrayList(scoresArrayList));

    /**
     * Bindable property of remote scores
     */
    private SimpleListProperty<Pair<String, Integer>> remoteScores ;


    /**
     * Holds and displays a list of names and associated scores from the localScores.txt file
     */
    protected ScoresList scoresList = new ScoresList();

    /**
     * Holds and displays a list of names and associated scores that are sent by the server
     */
    protected ScoresList remoteScoresList = new ScoresList();

    /**
     * To get the state of the previous game
     */
    protected Game game;

    /**
     * Score achieved in the previous game
     */
    protected int newScore;

    /**
     * To communicate with the server
     */
    protected Communicator communicator;

    /**
     * To play background music
     */
    private Multimedia multimedia = new Multimedia();

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     * @param gameWindow the game window
     */
    public ScoreScene(GameWindow gameWindow, Game g) {
        super(gameWindow);
        game = g;
        newScore = game.getScore().get();
        communicator = gameWindow.getCommunicator();
        logger.info("Creating score scene");
    }

    /**
     * Building the scores scene
     */
    public void build(){
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var scoresPane = new StackPane();
        scoresPane.setMaxWidth(gameWindow.getWidth());
        scoresPane.setMaxHeight(gameWindow.getHeight());
        scoresPane.getStyleClass().add("scores-background");
        root.getChildren().add(scoresPane);

        var mainPane = new BorderPane();
        scoresPane.getChildren().add(mainPane);

        // play background music for menu scene
        multimedia.playBgMusic("music/menu.mp3");

        // setting up the communicator listener to receive messages
        communicator.addListener(this::receiveCommunication);
        communicator.send("HISCORES"); // asking for online high scores

        // TetrECS image
        Image titleImage = new Image(getClass().getResource("/images/TetrECS.png").toExternalForm());
        ImageView titleImageView = new ImageView(titleImage);
        titleImageView.setFitHeight(500); // Set the height
        titleImageView.setFitWidth(500); // Set the width
        titleImageView.setPreserveRatio(true); // Preserve aspect ratio
        HBox title = new HBox(titleImageView); // Box with the image view
        title.setAlignment(Pos.CENTER);

        // Game over and high score headings
        gameOverTitle.getStyleClass().add("bigtitle");
        highScoreTitle.getStyleClass().add("title");
        localHighScoreTitle.getStyleClass().add("heading");
        onlineHighScoreTitle.getStyleClass().add("heading");
        gameOverTitle.setTranslateX(200);

        VBox titlesTopPane = new VBox(20);
        titlesTopPane.getChildren().add(title);
        titlesTopPane.getChildren().add(gameOverTitle);
        mainPane.setTop(titlesTopPane);

        loadScores(); // populate the scoresArrayList to update localScores
        logger.info("Scores have been loaded");

        // binding the ScoresList scores to the ScoresScene scores list
        scoresList.getScoresListProperty().bind(localScores);
        remoteScoresList.getScoresListProperty().bind(remoteScores);

        VBox localHighScoreList = new VBox(localHighScoreTitle, scoresList);
        VBox remoteHighscoreList = new VBox(onlineHighScoreTitle, remoteScoresList);
        HBox scoresListBox = new HBox(150);
        scoresListBox.getChildren().add(localHighScoreList);
        scoresListBox.getChildren().add(remoteHighscoreList);
        scoresListBox.setTranslateX(100);
        highScoreTitle.setTranslateX(300);
        VBox scoresBox = new VBox(highScoreTitle, scoresListBox);
        VBox centerBox = new VBox();
        mainPane.setCenter(centerBox);

        logger.info("Score was: " + newScore + ". Checking if new high score was set..");
        if (checkNewHighScore()){ // checking if the new score was higher than any scores in the list
            Text highScoreSetMsg = new Text("You set a new high score!");
            highScoreSetMsg.getStyleClass().add("high-score-set-msg");

            Text namePrompt = new Text("Enter your name");
            namePrompt.getStyleClass().add("title");

            TextField nameTextField = new TextField();
            nameTextField.setPrefWidth(200); // Set the preferred width to 200 pixels

            VBox nameBox = new VBox(highScoreSetMsg, namePrompt, nameTextField);
            centerBox.getChildren().add(nameBox);


            nameTextField.setOnAction(event -> {
                String playerName = nameTextField.getText(); // Save the entered name to the variable
                logger.info("Name submitted: " + playerName);
                centerBox.getChildren().remove(nameBox);
                centerBox.getChildren().add(scoresBox);
                newHighScoreSet(playerName); // write new high score to localScores.txt
                writeOnlineScore(playerName); // send high score to server
                loadScores();
                scoresList.reveal(); // displaying the scores
                remoteScoresList.reveal();
            });
        }

        if (!checkNewHighScore()){ // if there is no new high score, display the high score list directly
            localScores.setAll(scoresArrayList); // update the SimpleListProperty localScores
            centerBox.getChildren().add(scoresBox);
            scoresList.reveal(); // displaying the scores
            remoteScoresList.reveal();
        }
    }

    /**
     * Initialising the scores scene
     */
    public void initialise(){
        scene.setOnKeyPressed(this::handleKey); // handle key being pressed
    }

    /**
     * Populates the scoresArrayList using the file.
     * If file doesn't exist, create one with dummy data
     */
    public void loadScores(){
        logger.info("Loading scores");
        try {
            File localScoresFile = new File("localScores.txt");

            // writing default scores if file doesn't exist
            if (!localScoresFile.exists()){
                logger.info("File didnt exist, creating one..");
                FileWriter writer = new FileWriter(localScoresFile);
                writer.write("Verstappen:2000\n");
                writer.write("Sainz:1700\n");
                writer.write("Norris:1500\n");
                writer.write("Piastri:1300\n");
                writer.write("Ricardo:1100\n");
                writer.write("Hamilton:900\n");
                writer.write("Leclerc:700\n");
                writer.write("Vettel:500\n");
                writer.write("Ocon:300\n");
                writer.write("Gasly:100\n");
                writer.close();
                logger.info("Created and populated file");
            }

            // Reading from the file for populating the scoresArrayList
            BufferedReader fileReader = new BufferedReader(new FileReader(localScoresFile));
            String line;
            while ((line = fileReader.readLine()) != null){
                String[] parts = line.split(":", 2); // Split the line into two parts
                String name = parts[0];
                String scoreString = parts[1];
                Integer score = Integer.parseInt(scoreString); // convert score from string to int
                scoresArrayList.add(new Pair<>(name, score)); // populate scoresArrayList
                localScores.setAll(scoresArrayList); // update the SimpleListProperty localScores
            }
            fileReader.close();

        } catch (IOException e) {
            logger.error("loadScores method didnt execute");
            throw new RuntimeException(e);
        }
    }

    /**
     * Write the contents of the scoresArrayList to the file
     */
    public void writeScores(){
        File localScoresFile = new File("localScores.txt");
        try {
            FileWriter writer = new FileWriter(localScoresFile);

            // write the first 10 elements of arraylist to the file
            for (int i = 0; i < 10; i++){
                Pair<String, Integer> pair = scoresArrayList.get(i);
                writer.write(pair.getKey() + ":" + pair.getValue() + "\n");
            }
            logger.info("Written to file");
            writer.close();
        } catch (IOException e) {
            logger.info("Couldn't write to file");
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if new high score was set by comparing the values in the scoresArrayList to the new score
     * @return return true if new high score was set
     */
    public boolean checkNewHighScore(){
        for (Pair<String, Integer> pair : scoresArrayList) {
            if (newScore > pair.getValue()) {
                logger.info("New high score was set");
                return true; // Return true if there is a new high score
            }
        }
        return false; // Return false if there is no new high score
    }

    /**
     * Adds new high score to the scoresArrayList and calls the writeScores() method to write it to the file
     * @param name Name of the player that set the high score
     */
    public void newHighScoreSet(String name){
        logger.info("updating scoresArrayList after a new high score was set");

        // remove the last element so that there's only 10 elements in the list
        logger.info("scoresArrayList size: " + scoresArrayList.size());
        scoresArrayList.remove(9);

        // add their name and score to the list
        scoresArrayList.add(new Pair<>(name, newScore));

        // sort the arraylist in descending order
        scoresArrayList.sort((p1, p2) -> p2.getValue().compareTo(p1.getValue()));

        // call writeScores() so that the localScores.txt file can be updated
        writeScores();
    }

    /**
     * Populate the remoteScores using the communication received from server
     */
    public void loadOnlineScores(String[] hiscores){
        for (String hiscore : hiscores){
            String[] hiscoreParts = hiscore.split(":");
            String name = hiscoreParts[0];
            Integer score = Integer.parseInt(hiscoreParts[1]);
            remoteScoresArrayList.add(new Pair<>(name, score)); // populate scoresArrayList
        }
        remoteScores = new SimpleListProperty<>(remoteScoresArrayList);
    }

    /**
     * Submit the high score to the server
     */
    public void writeOnlineScore(String playerName){
        communicator.send("HISCORE " + playerName + ":" + newScore);
    }

    /**
     * For receiving remote scores
     * @param communication the message that was received
     */
    public void receiveCommunication(String communication){
        if (communication.contains("HISCORES")){
            String temp = communication.split(" ")[1];
            String[] hiscores = temp.split("\n");
            loadOnlineScores(hiscores);
        }
    }

    /**
     * Keyboard support
     * @param event key being pressed
     */
    public void handleKey(KeyEvent event){
        if (event.getCode() == KeyCode.ESCAPE){
            logger.info("Escape pressed, going to menu scene");
            multimedia.stopBgMusic();
            gameWindow.cleanup(); // clean up the window before going back to the menu scene
            gameWindow.startMenu();
        }
    }

}
