package uk.ac.soton.comp1206.scene;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * Scores scene. Holds the high scores and is displayed after a Single Player game ends
 */
public class ScoreScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Title of the scene
     */
    private Text highScoretitle = new Text("High Scores");

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

        highScoretitle.getStyleClass().add("title");
        mainPane.setTop(highScoretitle);
        highScoretitle.setTranslateX(300);

    }

    public void initialise(){

    }

}
