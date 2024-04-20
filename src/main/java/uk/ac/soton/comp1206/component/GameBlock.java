package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.util.Duration;

import java.math.BigDecimal;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Used as a flag if the gameblock is being hovered over or selected using
     */
    private boolean hoverOn;

    /**
     * Used in fadeOut(), this is gradually decremented
     */
    private double opacity;

    /**
     * Updates the hoverOn attribute and calls the paint method so that the hover effect can be displayed
     * @param on whether hover effect should be on or off
     */
    public void setHoverOn(boolean on){
        hoverOn = on;
        paint();
    }

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the mouse is over the block, hover
        setOnMouseEntered((e) -> {
            setHoverOn(true);
            paint();
        });

        //When the mouse leaves, no longer hover
        setOnMouseExited((e) -> {
            setHoverOn(false);
            paint();
        });

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        var gc = getGraphicsContext2D();

        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }

        if ((hoverOn) && (!(gameBoard instanceof PieceBoard)) ){
            gc.setFill(new Color(0.2, 0.2, 0.2, 0.7)); // 50% opacity
            gc.fillRect(0,0,width,height);
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        // filling the empty block colours
        if (gameBoard instanceof PieceBoard){
            gc.setFill(new Color(1, 1, 1, 0.8)); // for the boards of current and incoming piece
        } else {
            gc.setFill(new Color(1, 1, 1, 0.2)); // for the main game board
        }
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2); // make the border size bigger
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);

        // To have more of a vignette effect on each block
        // Values passed in constructor like: centerX, centerY, radius, focus angle, focus distance
        RadialGradient gradient = new RadialGradient(0, 0, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT), // the black colour is transparent at the centre
                new Stop(1, Color.rgb(0, 0, 0, 0.3)) // black colour at 35% opacity at the border
        );

        gc.setFill(gradient);
        gc.fillRect(0, 0, width, height);


        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);

        // drawing dot at the center block
        if ((gameBoard instanceof PieceBoard) && (getX() == 1) && (getY() == 1)){
            drawDot(gc);
        }
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * Drawing dot at the center of the gamepices in the current and incoming piece pieceboards
     * @param gc GraphicsContext instance
     */
    public void drawDot(GraphicsContext gc){
        // Draw a dot in the center of the pieceboards
        gc.setFill(Color.color(0,0,0,0.5));
        gc.fillOval(width/4,height/4,width/2,height/2);
    }

    /**
     * Fades out the cleared blocks slowly
     */
    public void fadeOut(){
        logger.info("blocks clear fadeOut animation");
        var gc = getGraphicsContext2D();
        opacity = 1;

        var fadeTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                paint(); // to return to the empty block colour

                // fill colour is set to white
                gc.setFill(Color.color(1,1,1, opacity)); // opacity is decremented
                gc.fillRect(0,0,width,height);

                if (opacity == 0.0){
                    this.stop(); // stopping timer when opacity reaches 0 (becomes transparent basically)
                }

                // opacity is decremented by 0.06 until it reaches 0 so that it fades out more each time handle() is called
                double temp = opacity - 0.05;
                if (temp<0){
                    opacity = 0;
                } else {
                    opacity = temp;
                }
            }
        };

        fadeTimer.start();
    }

}
