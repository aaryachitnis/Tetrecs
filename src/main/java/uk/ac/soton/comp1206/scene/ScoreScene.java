package uk.ac.soton.comp1206.scene;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public ScoreScene(GameWindow gameWindow) {
        super(gameWindow);
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
        highScoreTitle.setTranslateX(300);

        VBox titlesTopPane = new VBox(20);
        titlesTopPane.getChildren().add(title);
        titlesTopPane.getChildren().add(gameOverTitle);
        titlesTopPane.getChildren().add(highScoreTitle);
        mainPane.setTop(titlesTopPane);

        // binding the ScoresList scores to the ScoresScene scores list

        loadScores(); // populate the scoresArrayList
//        for (Pair<String, Integer> pair : localScores) {
//            String name = pair.getKey();
//            Integer score = pair.getValue();
//            logger.info(name + ": "  + score);
//        }
        logger.info("Scores have been loaded");

        scoresList.getScoresListProperty().bind(localScores);
        mainPane.setCenter(scoresList);
        scoresList.reveal(); // displaying the scores
        scoresList.setTranslateX(250);

    }

    public void initialise(){

    }

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

        } catch (IOException e) {
            logger.error("loadScores method didnt execute");
            throw new RuntimeException(e);
        }

        // get the score from the Game
        // check if it beats any of the scores
        // if it does, prompt user to type their name
            // insert name and score in the arraylist at the correct position
            // call writeScores method and overwrite the whole arraylist to the file

        // get score
        // if its higher than any of the values in the array list
            // prompt user to enter their name
            // add their name and score to the list
            // sort the list
            // remove the last element so that there's only 10 elements in the list
            // pass the arraylist to writeScores() so that the localScores.txt file can be updated
        // Read from file like normal

    }

    public void writeScores(){
        // sort the arraylist in descending order
        scoresArrayList.sort((p1, p2) -> p2.getValue().compareTo(p1.getValue()));

        File localScoresFile = new File("localScores.txt");
        try {
            FileWriter writer = new FileWriter(localScoresFile);

            // write the first 10 elements of arraylist to the file
            for (int i = 0; i < 10; i++){
                Pair<String, Integer> pair = scoresArrayList.get(i);
                writer.write(pair.getKey() + ":" + pair.getValue() + "\n");
            }
            logger.info("Written to file");
        } catch (IOException e) {
            logger.info("Couldn't write to file");
            throw new RuntimeException(e);
        }
    }

}
