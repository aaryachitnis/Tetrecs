package uk.ac.soton.comp1206.component;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.ScoreScene;

/**
 * To show the scores in the score scene
 */
public class ScoresList extends VBox{
    private static final Logger logger = LogManager.getLogger(ScoresList.class);

    /**
     * Bindable scores list
     */
    SimpleListProperty<Pair<String, Integer>> scoresListProperty = new SimpleListProperty<>();

    /**
     * Accessor method for the SimpleListProperty scores
     * @return SimpleListProperty scores
     */
    public SimpleListProperty<Pair<String, Integer>> getScoresListProperty() {
        return scoresListProperty;
    }

    /**
     * Constructs a VBox
     */
    public ScoresList(){
        setSpacing(5);
        scoresListProperty = getScoresListProperty();
    }

    /**
     * Reveals the scores on the Score scene
     */
    public void reveal() {
        logger.info("Revealing scores..");
        this.getChildren().clear();

        for (Pair<String, Integer> pair : scoresListProperty) {
            String name = pair.getKey();
            Integer score = pair.getValue();

            Text line = new Text(name + ": " + score);
            line.getStyleClass().add("scorelist");

            getChildren().add(line); // adding line to the VBox container

        }
    }
}








