package uk.ac.soton.comp1206.component;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import javafx.util.Duration;

/**
 * To show the scores in the score scene
 */
public class ScoresList extends VBox{

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

    public void reveal() {
        // Delay between each line reveal
        double delayPerLine = 0.5; // Adjust as needed

        // Initialize delay for each line
        double totalDelay = 0.0;

        for (Pair<String, Integer> pair : scoresListProperty) {
            String name = pair.getKey();
            Integer score = pair.getValue();

            Text line = new Text(name + ": " + score);
            line.getStyleClass().add("scorelist");

            getChildren().add(line); // adding line to the VBox container

            // Animation to display each line one by one
            Timeline timeline = new Timeline();
            line.setOpacity(0); // line is transparent at the start
            // Using Key frame to change opacity from 0 to 1
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(totalDelay),
                    new KeyValue(line.opacityProperty(), 1)));
            totalDelay += delayPerLine; // incrementing total delay for the next lin
            timeline.play();
        }
    }

}








