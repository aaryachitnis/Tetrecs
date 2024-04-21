package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleListProperty;
import javafx.scene.text.Text;

/**
 * VBox to show players and their scores during a multiplayer game
 */
public class Leaderboard extends ScoresList{

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

    /**
     * Constructor for Leaderboard
     */
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
            info.getStyleClass().add("scorelist");

            // Cross out player if they are dead
            if (lives.equals("DEAD")){
                info.setStrikethrough(true);
            }

            getChildren().add(info);
        }
    }
}
