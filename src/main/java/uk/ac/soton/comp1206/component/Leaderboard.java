package uk.ac.soton.comp1206.component;


import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;

public class Leaderboard extends ScoresList{

    SimpleListProperty<ObservableList<String>> playersInfoListProperty = new SimpleListProperty<>();

    public SimpleListProperty<ObservableList<String>> getPlayersInfoListProperty() {
        return playersInfoListProperty;
    }

    public Leaderboard(){
        setSpacing(5);
        playersInfoListProperty = getPlayersInfoListProperty();
    }

}
