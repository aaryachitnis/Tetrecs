package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
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
     * Will contain all the messages sent and received
     */
    protected Text messages = new Text();

    /**
     * To send messages
     */
    protected TextField messageTextField = new TextField();

    /**
     * Start game button for users that are hosts of the game
     */
    protected Button startGameBtn = new Button("Start game");

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
    protected HBox playerBox = new HBox();

    /**
     * Will contain the messages sent between players
     */
    protected VBox messagesBox = new VBox(2);

    /**
     * Will contain the start game (if isHost = true) and the leave game buttons
     */
    protected HBox chatBoxBtnsBox = new HBox(315);

    /**
     * Arraylist containing all the nicknames
     */
    ArrayList<String> nicknameList = new ArrayList<>();

    /**
     * Arraylist containing all the usernames, will be used when displaying scores
     */
    ArrayList<String> userList = new ArrayList<>();

    BooleanProperty hasJoinedGameProperty = new SimpleBooleanProperty(false);

    /**
     * Whether the user is hosting a game or not
     * Bindable property so that the visibility of the button can be dynamically changed
     */
    BooleanProperty isHostProperty = new SimpleBooleanProperty(false);

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
        rightBox.visibleProperty().bind(hasJoinedGameProperty);
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
        playerBox.setPrefWidth(450);
        gameDescription.getStyleClass().add("game-description");
        chatBox.getChildren().add(gameDescription);
        chatBox.getChildren().add(messagesBox);
        messagesBox.setPrefWidth(500);
        messagesBox.setPrefHeight(350);
        messagesBox.getChildren().add(messages);
        messages.getStyleClass().add("messages");
        chatBox.getChildren().add(messageTextField);
        chatBox.getChildren().add(chatBoxBtnsBox);
        chatBoxBtnsBox.setPrefWidth(480);

        // Bind the visibility of the button to the value of isHostProperty
        startGameBtn.visibleProperty().bind(isHostProperty);
        chatBoxBtnsBox.getChildren().add(startGameBtn);

        startGameBtn.setOnAction(actionEvent -> {
            // TODO: start the multiplayer game
        });

        // Leave button in the chatBox
        var leaveGameBtn = new Button("Leave game");
        chatBoxBtnsBox.getChildren().add(leaveGameBtn);

        leaveGameBtn.setOnAction(actionEvent -> {
            communicator.send("PART"); // leave game
        });


        // Sending messages when user hits enter
        messageTextField.setOnAction(event -> {
            String enteredMsg = messageTextField.getText();
            messageTextField.clear();
            logger.info("Entered text: " + enteredMsg);
            communicator.send("MSG " + enteredMsg);
        });


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
            hostGameBox.getChildren().add(channelNameTextField);

            channelNameTextField.setOnAction(creatGameEvent -> {
                String channelName = channelNameTextField.getText(); // store the channel name user entered
                logger.info("Hosting new game: " + channelName);

                // remove text field
                hostGameBox.getChildren().remove(channelNameTextField);

                // send request to create new game
                communicator.send("CREATE " + channelName);
            });

        });
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
            isHostProperty.set(true);
        } else if (communication.contains("NICK")){
            getNicknameList(communication);
        } else if (communication.contains("USERS")) {
            getUserList(communication);
        } else if (communication.contains("PARTED")) {
            leaveChannel();
        } else if (communication.contains("MSG")) {
            messages(communication);
        } else if (communication.contains("ERROR")) {
            handleError(communication);
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

                channelNameBtn.setOnAction(createGameEvent -> {
                    communicator.send("JOIN " + channel);
                });
            }
        });
    }

    /**
     * Displays channel name
     * @param gameInfo channel name
     */
    public void gameJoined(String gameInfo){
        // channelName would be "Game: <channel name>"
        String channelName = currentChannelName.getText() + gameInfo.split(" ")[1];
        currentChannelName.setText(channelName);
        hasJoinedGameProperty.set(true);
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
            String players = users.substring(users.indexOf(' ') + 1);
            // Replace all newlines with commas to format the string
            players = players.replaceAll("\n", ", ");
            logger.info("Players display: " + players);
            playerNames.setText("Players: " + players);
        });
    }

    /**
     * Clears the relevant parts of the lobby scene after user has parted from a game
     */
    public void leaveChannel(){
        // The channel is already removed from the server by this point
        // just need to clear everything
        currentChannelName.setText("Game: ");
        playerNames.setText("Players: ");
        messages.setText("");
        userList.clear();
        isHostProperty.set(false);
        hasJoinedGameProperty.set(false);
    }

    /**
     * Display messages received in the chat box
     * @param communication String containing player name and their message
     */
    public void messages(String communication){
        logger.info("Message comm display: " + communication);
        String[] comm = communication.split(":");
        String name = comm[0].split(" ")[1]; // to remove the "MSG" part of the communication
        String msg = comm[1];
        messages.setText(messages.getText() + name + ": " + msg + "\n");
    }

    /**
     * Displays a pop-up window displaying the error message
     * @param errorMsg the error message
     */
    public void handleError(String errorMsg){
        String[] parts = errorMsg.split(" ", 2);
        String error = parts[1];
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(error);
            alert.setHeaderText(null);  // Optional: remove the header
            alert.showAndWait();
        });
    }
}
