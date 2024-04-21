package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.HashSet;

/**
 * LineClearedListener listens to when a line is cleared
 */
public interface LineClearedListener {
    /**
     * Handle when a line has been cleared
     * @param coordinates HashSet containing all the blocks to clear
     */
    void lineCleared(HashSet<GameBlockCoordinate> coordinates);
}
