package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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

    /**
     * When a user joins a game, this will hold the name of that channel
     */
    protected Text currentChannelName = new Text("Game: ");

    /**
     * Heading for players in the game
     */
    protected Text playerNames = new Text("Players: ");

    /**
     * Explains how to change nickname
     */
    protected Text gameDescription = new Text("Welcome to the lobby!\nType /nick NewName to change your name");

    /**
     * Contains all the available channels
     */
    protected VBox channelsBox = new VBox(5);

    /**
     * Will contain the channel name and messages of the game a user joins
     */
    protected VBox rightBox = new VBox(2);

    /**
     * This will contain messages within a game
     */
    protected VBox chatBox = new VBox(5);

    /**
     * Will hold the names of the players in the game
     */
    protected VBox playerBox = new VBox();

    /**
     * Will contain the messages sent between players
     */
    protected VBox messagesBox = new VBox(2);

    /**
     * Will contain the start game (if isHost = true) and the leave game buttons
     */
    protected HBox chatBoxBtnsBox = new HBox(450);

    /**
     * Arraylist containing all the nicknames
     */
    ArrayList<String> nicknameList = new ArrayList<>();

    /**
     * Arraylist containing all the usernames, will be used when displaying scores
     */
    ArrayList<String> userList = new ArrayList<>();


    /**
     * Whether the user is hosting a game or not
     */
    protected boolean isHost = false;
    // TODO: change this to false when the user hits the leave game btn

    /**
     * Create a new Lobby scene for multiplayer games
     * @param gameWindow gameWindow
     */
    public LobbyScene(GameWindow gameWindow){
        super(gameWindow);
    }

    /**
     * Building the Lobby Scene
     */
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

        // right box
        mainPane.setRight(rightBox);
        rightBox.getChildren().add(currentChannelName);
        currentChannelName.getStyleClass().add("heading");
        rightBox.getChildren().add(chatBox);
        chatBox.getStyleClass().add("gameBox");
        chatBox.setPrefWidth(500);
        chatBox.setPrefHeight(500);
        chatBox.setTranslateY(10);
        rightBox.setTranslateX(-5);

        // Chat box
        playerBox.getChildren().add(playerNames);
        playerNames.getStyleClass().add("player-names");
        chatBox.getChildren().add(playerBox);
        gameDescription.getStyleClass().add("game-description");
        chatBox.getChildren().add(gameDescription);
        chatBox.getChildren().add(messagesBox);
        messagesBox.setPrefWidth(500);
        messagesBox.setPrefHeight(400);
        chatBox.getChildren().add(chatBoxBtnsBox);

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

        // Leave button in the chatBox
        var leaveGameBtn = new Button("Leave game");
        chatBoxBtnsBox.getChildren().add(leaveGameBtn);

        leaveGameBtn.setOnAction(actionEvent -> {
            communicator.send("PART"); // leave game
        });

        // Start game button if user is the host
        // TODO: show this if you are the host, set to bottom
        var startGameBtn = new Button("Start game");

    }

    public void initialise(){

        // adding listener to receive messages from server
        communicator.addListener(this::receiveCommunication);

        // get a list of channels from server at regular intervals
        requestChannels();

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
        Timer timer = new Timer();
        TimerTask getChannels = new TimerTask() {
            public void run() {
                communicator.send("LIST");
            }
        };
        timer.scheduleAtFixedRate(getChannels, 0, 5000L); // channels will be requested every 5 seconds
    }

    /**
     * Receives communication from the sever
     * @param communication the message that was received
     */
    public void receiveCommunication(String communication){

        if (communication.contains("CHANNELS")){
            displayChannels(communication);
        } else if (communication.contains("JOIN")) {
            gameJoined(communication);
        } else if (communication.contains("HOST")) {
            isHost = true;
        } else if (communication.contains("NICK")){
            getNicknameList(communication);
        } else if (communication.contains("USERS")) {
            getUserList(communication);
        } else if (communication.contains("PART")) {
            leaveChannel();
        }

    }

    /**
     * Display all the channels current being hosted 
     * @param channels communication message sent by the server containing all channels currently available
     */
    public void displayChannels(String channels){
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
                channelNameBtn.getStyleClass().add("channelItem");
                channelsBox.getChildren().add(channelNameBtn);
                channelNameBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                channelNameBtn.setTextFill(Color.WHITE);
            }
        });
    }

    public void gameJoined(String gameInfo){
        // channelName would be "Game: <channel name>"
        String channelName = currentChannelName.getText() + gameInfo.split(" ")[1];
        currentChannelName.setText(channelName);
    }

    /**
     * Handles how to display nicknames in the chatBox
     * @param nicknames list of nicknames
     */
    public void getNicknameList(String nicknames){
        nicknameList.clear(); // clear any previous nicknames list
        Platform.runLater(() -> {
            String[] splitNickNames = nicknames.split(" ");

            for (int i = 1; i < splitNickNames.length; i++) {
                nicknameList.add(splitNickNames[i]);  // Add each channel name to the list
            }
        });
    }

    /**
     * Handles how to display users
     * @param users list of users in a game
     */
    public void getUserList(String users){
        userList.clear(); // clear any previous user list

        Platform.runLater(() -> {
            String[] splitNames = users.split(" ");
            String players = new String();
            for (int i = 1; i < splitNames.length; i++) {
                players = playerNames.getText() + splitNames[i] + ", ";
                userList.add(splitNames[i]);  // Add each channel name to the list
            }
            playerNames.setText(players);
        });
    }

    public void leaveChannel(){
        // TODO: clear everything that needs to be cleared and make the right box disappear
    }
}
