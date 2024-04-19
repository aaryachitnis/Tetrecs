package uk.ac.soton.comp1206.scene;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
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
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Scores scene. Holds the high scores and is displayed after a Single Player game ends
 */
public class ScoreScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(ScoreScene.class);

    /**
     * High Score sub-title
     */
    private Text highScoreTitle = new Text("High Score");

    private Text gameOverTitle = new Text("Game Over");

    /**
     * ArrayList containing name and score
     */
    protected ArrayList<Pair<String, Integer>> scoresArrayList = new ArrayList<>() {};

    /**
     * Wrapping the ArrayList scoresArrayList which contains the name and score in SimpleListProperty
     */
    protected final SimpleListProperty<Pair<String, Integer>> localScores = new SimpleListProperty<>(FXCollections.observableArrayList(scoresArrayList));

    /**
     * Holds and displays a list of names and associated scores
     */
    protected ScoresList scoresList = new ScoresList();

    /**
     * To get the state of the previous game
     */
    protected Game game;

    /**
     * Score achieved in the previous game
     */
    protected int newScore;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public ScoreScene(GameWindow gameWindow, Game g) {
        super(gameWindow);
        game = g;
        newScore = game.getScore().get();
        logger.info("Creating score scene");
    }

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
        gameOverTitle.setTranslateX(200);

        VBox titlesTopPane = new VBox(20);
        titlesTopPane.getChildren().add(title);
        titlesTopPane.getChildren().add(gameOverTitle);
        mainPane.setTop(titlesTopPane);

        loadScores(); // populate the scoresArrayList
        logger.info("Scores have been loaded");

        // binding the ScoresList scores to the ScoresScene scores list
        scoresList.getScoresListProperty().bind(localScores);

        VBox highScoreList = new VBox(highScoreTitle, scoresList);
        VBox centerBox = new VBox();
        mainPane.setCenter(centerBox);

        logger.info("Score was: " + newScore + ". Checking if new high score was set..");
        if (checkNewHighScore()){ // checking if the new score was higher than any scores in the list
            logger.info("Prompting user to enter name");
            Text highScoreSetMsg = new Text("You set a new high score!");
            highScoreSetMsg.getStyleClass().add("high-score-set-msg");
            Text namePrompt = new Text("Enter your name");
            namePrompt.getStyleClass().add("title");
            TextField nameTextField = new TextField();
            Button submitButton = new Button("Submit");
            // TODO: style the submit button

            VBox nameBox = new VBox(highScoreSetMsg, namePrompt, nameTextField, submitButton);
            centerBox.getChildren().add(nameBox);

            submitButton.setOnAction(event -> {
                String playerName = nameTextField.getText(); // Save the entered name to the variable
                logger.info("Name submitted: " + playerName);
                centerBox.getChildren().remove(nameBox);
                centerBox.getChildren().add(highScoreList);
                newHighScoreSet(playerName);
                loadScores();
                scoresList.reveal(); // displaying the scores

            });
        }

        if (!checkNewHighScore()){ // if there is no new high score, display the high score list directly
            localScores.setAll(scoresArrayList); // update the SimpleListProperty localScores
            centerBox.getChildren().add(highScoreList);
            scoresList.reveal(); // displaying the scores
        }


    }

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

            // Reading from the file
            logger.info("Attempting to read..");
            BufferedReader fileReader = new BufferedReader(new FileReader(localScoresFile));
            String line;
            while ((line = fileReader.readLine()) != null){
                String[] parts = line.split(":", 2); // Split the line into two parts
                String name = parts[0];
                String scoreString = parts[1];
                Integer score = Integer.parseInt(scoreString); // convert score from string to int
                // TODO: do i update the scoresArrayList or the SimpleListProperty??
                scoresArrayList.add(new Pair<>(name, score)); // populate scoresArrayList
                localScores.setAll(scoresArrayList); // update the SimpleListProperty localScores
                logger.info("updated arraylist");
            }
            logger.info("Populated scoresArrayList");
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
        logger.info("Writing new high score to file..");
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
        logger.info("No new high score");
        return false; // Return false if there is no new high score
    }

    /**
     * Adds new high score to the scoresArrayList and calls the writeScores() method to write it to the file
     * @param name Name of the player that set the high score
     */
    public void newHighScoreSet(String name){
        logger.info("updating scoresArrayList after a new high score was set");
        // add their name and score to the list
        scoresArrayList.add(new Pair<>(name, newScore));

        // sort the arraylist in descending order
        scoresArrayList.sort((p1, p2) -> p2.getValue().compareTo(p1.getValue()));

        // remove the last element so that there's only 10 elements in the list
        scoresArrayList.remove(10);

        // call writeScores() so that the localScores.txt file can be updated
        writeScores();
    }

    public void handleKey(KeyEvent event){
        if (event.getCode() == KeyCode.ESCAPE){
            logger.info("Escape pressed, going to menu scene");
            gameWindow.cleanup(); // clean up the window before going back to the menu scene
            gameWindow.startMenu();
        }
    }

}
