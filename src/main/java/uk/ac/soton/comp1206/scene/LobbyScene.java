package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LobbyScene extends BaseScene implements CommunicationsListener{

    private static final Logger logger = LogManager.getLogger(LobbyScene.class);

    /**
     * Communicator
     */
    protected Communicator communicator = gameWindow.getCommunicator();

    /**
     * Title
     */
    protected Text title = new Text("Multiplayer");

    /**
     * Current games heading
     */
    protected Text currentGameHeading = new Text("Current Games");

    protected VBox channelsBox = new VBox(5);

    /**
     * Create a new Lobby scene for multiplayer games
     * @param gameWindow gameWindow
     */
    public LobbyScene(GameWindow gameWindow){
        super(gameWindow);
    }

    public void build(){
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var lobbyPane = new StackPane();
        lobbyPane.setMaxWidth(gameWindow.getWidth());
        lobbyPane.setMaxHeight(gameWindow.getHeight());
        lobbyPane.getStyleClass().add("lobby-background");
        root.getChildren().add(lobbyPane);

        var mainPane = new BorderPane();
        lobbyPane.getChildren().add(mainPane);

        title.setTranslateX(320);

        // left box (setLeft of mainPane)
        VBox leftBox = new VBox(5);
        mainPane.setLeft(leftBox);
        leftBox.setPadding(new Insets(10, 10, 10, 10)); // add padding to the left box
        // Current games heading
        currentGameHeading.getStyleClass().add("heading");
        leftBox.getChildren().add(currentGameHeading);
        // Contains all elements relating to hosting a new game
        VBox hostGameBox = new VBox(3);
        leftBox.getChildren().add(hostGameBox);
        // channels VBox
        leftBox.getChildren().add(channelsBox);


        // title
        title.getStyleClass().add("title");
        mainPane.setTop(title);

        // Host new games button
        var hostNewGamesBtn = new Button("Host new game");
        hostGameBox.getChildren().addAll(hostNewGamesBtn);
        hostNewGamesBtn.getStyleClass().add("small-heading");
        // button styling
        hostNewGamesBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        hostNewGamesBtn.setTextFill(Color.WHITE);

        // Displaying text field when hostNewGamesBtn is clicked
        hostNewGamesBtn.setOnAction(event -> {
            TextField channelNameTextField = new TextField();
            Button createButton = new Button("Create"); // TODO: style this
            hostGameBox.getChildren().add(channelNameTextField);
            hostGameBox.getChildren().add(createButton);

            createButton.setOnAction(createGameEvent -> {
                String channelName = channelNameTextField.getText(); // store the channel name user entered
                logger.info("Hosting new game: " + channelName);

                // remove text field and button
                hostGameBox.getChildren().remove(channelNameTextField);
                hostGameBox.getChildren().remove(createButton);

                // send request to create new game
                communicator.send("CREATE " + channelName);
            });
        });
    }

    public void initialise(){

        // adding listener to receive messages from server
        communicator.addListener(this::receiveCommunication);

        // get a list of channels from server at regular intervals
        Timer timer = new Timer();
        TimerTask getChannels = new TimerTask() {
            public void run() {
                requestChannels(); // ask server for the list of channels available
            }
        };
        timer.scheduleAtFixedRate(getChannels, 0, 5000L); // channels will be requested every 5 seconds

        // handle keyboard keys pressed
        scene.setOnKeyPressed(this::handleKey);
    }

    /**
     * Handle keys being pressed
     * @param event the key pressed
     */
    public void handleKey(KeyEvent event){
        if (event.getCode() == KeyCode.ESCAPE){
            logger.info("Escape pressed, going to menu scene");
            gameWindow.cleanup(); // clean up the window before going back to the menu scene
            communicator.send("QUIT");// quit  from the server
            gameWindow.startMenu();
        }
    }

    /**
     * Request a list of current channels from the server
     */
    public void requestChannels(){
        communicator.send("LIST");
    }

    /**
     * Receives communication from the sever
     * @param communication the message that was received
     */
    public void receiveCommunication(String communication){

        if (communication.contains("CHANNELS")){
            displayChannels(communication);
        } else if (communication.contains("JOIN")) {
            logger.info("Communication: " + communication);
        }

    }

    /**
     * Display all the channels current being hosted 
     * @param channels communication message sent by the server containing all channels currently available
     */
    public void displayChannels(String channels){
        logger.info("Displaying channels:" + channels);
        Platform.runLater(() -> {
            channelsBox.getChildren().clear(); // clear any previous channels being displayed
            String[] splitChannelNames = channels.split(" ");
            ArrayList<String> channelsList = new ArrayList<>(); // arraylist containing all the channels

            for (int i = 1; i < splitChannelNames.length; i++) {
                channelsList.add(splitChannelNames[i]);  // Add each channel name to the list
            }

            // displaying every channel name
            for (String channel : channelsList){
                Button channelNameBtn = new Button(channel);
                channelNameBtn.getStyleClass().add("small-heading");
                channelsBox.getChildren().add(channelNameBtn);
                channelNameBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                channelNameBtn.setTextFill(Color.WHITE);
            }

        });

    }

}
