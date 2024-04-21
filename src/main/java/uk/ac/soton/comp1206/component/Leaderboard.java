package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleListProperty;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class Leaderboard extends ScoresList{

    private static final Logger logger = LogManager.getLogger(Leaderboard.class);

    /**
     * Bindable scores list
     */
    SimpleListProperty<String> playersInfoListProperty = new SimpleListProperty<>();

    /**
     * Accessor method for the SimpleListProperty scores
     * @return SimpleListProperty scores
     */
    public SimpleListProperty<String> getPlayersInfoListProperty() {
        return playersInfoListProperty;
    }

    public Leaderboard(){
        playersInfoListProperty = getPlayersInfoListProperty();
    }

    @Override
    public void reveal(){
        this.getChildren().clear();
        for (String line : playersInfoListProperty) {
            String[] parts = line.split(":");
            String name = parts[0];
            String score = parts[1];
            String lives = parts[2];

            Text info = new Text(name + ": " + score);
            logger.info(name + ": "  + score);
            info.getStyleClass().add("scorelist");

            // Cross out player if they are dead
            if (lives.equals("DEAD")){
                info.setStrikethrough(true);
            }

            getChildren().add(info);



        }
    }
}
