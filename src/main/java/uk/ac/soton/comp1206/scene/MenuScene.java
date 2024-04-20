package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

import javafx.util.Duration;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * To play background music
     */
    private Multimedia multimedia = new Multimedia();

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        // play background music for menu scene
        multimedia.playBgMusic("music/menu.mp3");

        // Title
        Image titleImage = new Image(getClass().getResource("/images/TetrECS.png").toExternalForm());
        ImageView titleImageView = new ImageView(titleImage);
        titleImageView.setFitHeight(700); // Set the height
        titleImageView.setFitWidth(600); // Set the width
        titleImageView.setPreserveRatio(true); // Preserve aspect ratio
        HBox title = new HBox(titleImageView); // Box with the image view
        title.setAlignment(Pos.CENTER);

        // Animating the title
        Timeline timeline = new Timeline(); // create a Timeline for the animation
        // Define the key frames for moving the image up and down
        KeyValue keyValueUp = new KeyValue(titleImageView.translateYProperty(), -70); // Define key frame for moving the image up
        KeyValue keyValueDown = new KeyValue(titleImageView.translateYProperty(), 0); // Define key frame for moving the image down
        // Creating the key frames
        KeyFrame keyFrameUp = new KeyFrame(Duration.seconds(3), keyValueUp);
        KeyFrame keyFrameDown = new KeyFrame(Duration.seconds(3), keyValueDown);
        timeline.getKeyFrames().addAll(keyFrameUp, keyFrameDown); // Adding key frames to the timeline
        timeline.setCycleCount(Timeline.INDEFINITE); // to repeat animation indefinitely
        timeline.setAutoReverse(true); // reversing the animation to make it look like its "bouncing"
        timeline.play();
        
        // Box to hold all the menu items
        VBox menu = new VBox();
        menu.setAlignment(Pos.CENTER);

        // Single Player button
        var singlePlayerBtn = new Button("Single Player");
        menu.getChildren().addAll(singlePlayerBtn);
        singlePlayerBtn.getStyleClass().add("menuItem");
        // Bind the button action to the startSinglePlayerGame method in the menu
        singlePlayerBtn.setOnAction(this::startSinglePlayerGame);
        // set the border and background of button to transparent and the text colour to white
        singlePlayerBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // Multiplayer Button
        var multiplayerBtn = new Button("Multi Player");
        menu.getChildren().addAll(multiplayerBtn);
        multiplayerBtn.getStyleClass().add("menuItem");
        // Bind the button action to the startMultiplayerGame method in the menu
        multiplayerBtn.setOnAction(this::startMultiplayerGame);
        // set the border and background of button to transparent and the text colour to white
        multiplayerBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // Instructions Button
        var instructionsBtn = new Button("How to play");
        menu.getChildren().addAll(instructionsBtn);
        instructionsBtn.getStyleClass().add("menuItem");
        // Bind the button action to the displayInstructions method in the menu
        instructionsBtn.setOnAction(this::displayInstructions);
        // set the border and background of button to transparent and the text colour to white
        instructionsBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // Exit Button
        var exitBtn = new Button("Exit");
        menu.getChildren().addAll(exitBtn);
        exitBtn.getStyleClass().add("menuItem");
        // Bind the button action to the exit method in the menu
        exitBtn.setOnAction(this::exit);
        // set the border and background of button to transparent and the text colour to white
        exitBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // Formatting and displaying title and menu
        VBox titleAndMenu = new VBox(50);
        titleAndMenu.getChildren().addAll(title, menu);
        mainPane.setCenter(titleAndMenu);
        titleAndMenu.setTranslateY(100);
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        scene.setOnKeyPressed(this::handleKey);
    }

    /**
     * Handle when the "Single Player" button is pressed and open the challenge scene
     * @param event event
     */
    private void startSinglePlayerGame(ActionEvent event) {
        multimedia.stopBgMusic();
        logger.info("Starting single player game");
        gameWindow.startChallenge();
    }

    /**
     * Handle when the "Multi Player" button is pressed and open the multiplayer scene
     * @param event event
     */
    private void startMultiplayerGame(ActionEvent event){
        logger.info("Starting multiplayer game");
        gameWindow.showLobbyScene();
    }

    /**
     * Handle when the "How to play" button is pressed and open the instructions scene
     * @param event event
     */
    private void displayInstructions(ActionEvent event){
        logger.info("Displaying instructions");
        gameWindow.showInstructions();
    }

    /**
     * Handle when the "Exit" button is pressed and shut down the game
     * @param event event
     */
    private void exit(ActionEvent event){
        logger.info("Exiting game");
        shutDownGame();
    }

    /**
     * Handle when escape key is pressed and shut down the game
     * @param event escape key being pressed
     */
    public void handleKey(KeyEvent event){
        if (event.getCode() == KeyCode.ESCAPE){
            shutDownGame();
        }
    }

    /**
     * Exit the game and close the game window
     */
    public void shutDownGame(){
        System.exit(0);
    }

}
