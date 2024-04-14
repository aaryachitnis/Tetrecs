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

import java.util.ArrayList;

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
//    protected ArrayList<Pair<String, Integer>> scoresArrayList = new ArrayList<>() {};
    protected ArrayList<Pair<String, Integer>> scoresArrayList = new ArrayList<>() {{
        // dummy data
        add(new Pair<>("Player1", 100));
        add(new Pair<>("Player2", 200));
    }};
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
        // TODO: do better styling, increase fonts and set colour
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
        logger.info("local scores: " + localScores);
        scoresList.getScoresListProperty().bind(localScores);
        mainPane.setCenter(scoresList);
        scoresList.reveal();
        scoresList.setTranslateX(100);

//        mainPane.getChildren().add(scoresList);

    }

    public void initialise(){

    }

}
