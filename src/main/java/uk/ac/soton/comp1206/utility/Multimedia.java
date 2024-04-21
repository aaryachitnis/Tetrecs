package uk.ac.soton.comp1206.utility;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.MenuScene;

/**
 * This class handles all the sound effects of the game
 */
public class Multimedia {

    private static final Logger logger = LogManager.getLogger(Multimedia.class);

    /**
     * MediaPlayer object for handling audio
     */
    private MediaPlayer audioPlayer;

    /**
     * MediaPlayer object for handling music
     */
    private MediaPlayer musicPlayer;

    /**
     * Mutator method for audioPlayer
     * @param media audio to be played
     */
    public void setAudioPlayer(Media media){
        audioPlayer = new MediaPlayer(media);
    }

    /**
     * Mutator method for musicPlayer
     * @param media music to be played
     */
    public void setMusicPlayer(Media media){
        musicPlayer = new MediaPlayer(media);
    }

    /**
     * plays audio depending on the event
     * @param audioFilePath the path to the audio file
     */
    public void playAudio(String audioFilePath){
        String audioToPlay = Multimedia.class.getResource("/" + audioFilePath).toExternalForm();
        logger.info("Playing audio: " + audioToPlay);

        try {
            Media media = new Media(audioToPlay);
            setAudioPlayer(media);
            audioPlayer.play();
        } catch (Exception e) {
            logger.error("Unable to play audio file, disabling audio");
            e.printStackTrace();
        }

    }

    /**
     * To play the background music when game starts
     * @param musicFilePath the path to the background music file
     */
    public void playBgMusic(String musicFilePath){
        String musicToPlay = Multimedia.class.getResource("/" + musicFilePath).toExternalForm();
        logger.info("Playing background music: " + musicToPlay);

        try {
            Media media = new Media(musicToPlay);
            setMusicPlayer(media);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // plays the background music on loop
            musicPlayer.play();
        } catch (Exception e) {
            logger.error("Unable to play music file, disabling audio");
            e.printStackTrace();
        }

    }

    /**
     * Stops the background music. Used when changing scene
     */
    public void stopBgMusic(){
        musicPlayer.stop();
    }
}
