package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleListProperty;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public ScoresList(){
        setSpacing(5);
        scoresListProperty = getScoresListProperty();
        logger.info("scoresListProperty: " + scoresListProperty);
    }

    public void reveal(){
        logger.info("In reveal");
        for (Pair<String, Integer> pair : scoresListProperty) {
            String name = pair.getKey();
            Integer score = pair.getValue();

            Text line = new Text(name + ": " + score);
            line.getStyleClass().add("scorelist");
            getChildren().add(line);
        }
    }

}








