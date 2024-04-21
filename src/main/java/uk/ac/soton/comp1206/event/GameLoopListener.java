package uk.ac.soton.comp1206.event;

/**
 * GameLoopListener listens to the time allocated for a piece to be placed before a life is lost
 */
public interface GameLoopListener {

    /**
     * Called in ChallengeScene to show the UI timebar
     * @param time time allocated
     */
    void setOnGameLoop(int time);
}
