package uk.ac.soton.comp1206.event;

/**
 * Listens to when lives remaining is below zero
 */
public interface GameOverListener {
    /**
     * Is called when there are no lives remaining and user should be taken to the Scores scene
     */
    void onGameOver();
}
