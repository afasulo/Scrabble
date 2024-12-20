package scrabble;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.effect.DropShadow;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;


import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * The GameGui class is the main entry point for the Scrabble game GUI.
 * It extends the JavaFX Application class and sets up the game interface,
 * including the board, human tray, score board, and various controls.
 */
public class GameGui extends Application {
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 800;

    private static final String HORIZONTAL = "Horizontal";
    private static final String VERTICAL = "Vertical";
    private static final String WARNING_TITLE = null;
    private static final String EMPTY_TILE_TITLE = "Empty tile";
    private static final String GAME_OVER_TITLE = "Game is over";

    private static final Logger LOGGER = Logger.getLogger(GameGui.class.getName());
    private static boolean consoleLoggingEnabled = false;
    private static ConsoleHandler consoleHandler;
    private static FileHandler fileHandler;

    private GameManager gameManager;
    private BoardGui boardGui;
    private RackBuilder humanTray;
    private  ScoreBoard scoreBoard;

    private static String dictionaryFile;
    private static String boardConfigFile;

    private Button makeMoveButton;
    private Button undoLastMoveButton;
    private ChoiceDialog<String> choosePlayDirectionDialog;
    private ChoiceDialog<Character> emptyTileDialog;
    private Alert noPlayDirectionAlert;
    private Alert notMakingMoveAlert;
    private Alert illegalChoiceAlert;
    private Alert noWordAlert;
    private Alert notValidWordAlert;
    private Alert gameOverAlert;
    private Button executeMoveButton;
    private Stage stage;
    private Button giveUpTurnButton;
    private Alert noGiveUpAlert;
    private HBox controlPanel;
    private ComboBox<String> difficultySelector;



    @Override
    /**
 * Starts the JavaFX application and sets up the game GUI.
 *
 * @param primaryStage the primary stage for this application
 */
public void start(Stage primaryStage) {
    setupLogger(); // Initialize the logger

    this.stage = primaryStage;
    try {
        // Initialize the game manager with the dictionary and board configuration files
        gameManager = new GameManager(dictionaryFile, boardConfigFile);
        LOGGER.info("Game manager initialized");

        // Initialize and draw the board GUI
        boardGui = new BoardGui(gameManager.getBoard());
        boardGui.draw();
        LOGGER.info("Board GUI initialized and drawn");

        // Initialize and draw the human tray
        humanTray = new RackBuilder(gameManager.getHumanRack());
        humanTray.draw();
        LOGGER.info("Human tray initialized and drawn");

        // Initialize and update the score board
        scoreBoard = new ScoreBoard();
        updateScoreBoard();
        LOGGER.info("Score board initialized and drawn");

        // Setup dialogs, buttons, and interactions
        setupDialogs();
        setupButtons();
        setupBoardInterface();
        setupHumanTrayInteraction();
        setupDifficultySelector();

        // Setup the main layout
        BorderPane root = new BorderPane();
        setupLayout(root);

        // Create and set the scene
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Scrabble");
        primaryStage.setScene(scene);

        // Show the primary stage
        primaryStage.show();
        LOGGER.info("Game GUI displayed");
    } catch (IOException e) {
        // Log any initialization errors
        LOGGER.log(Level.SEVERE, "Error initializing game: " + e.getMessage(), e);
    }
}

   /**
 * Sets up the logger for the application.
 * This method configures the logger to log all levels of messages,
 * creates and adds a custom formatter, and sets up both file and console handlers.
 * Console logging is enabled or disabled based on the initial flag value.
 * Parent handlers are disabled to prevent double logging.
 */
private void setupLogger() {
    try {
        // Remove all existing handlers
        for (Handler handler : LOGGER.getHandlers()) {
            LOGGER.removeHandler(handler);
        }

        LOGGER.setLevel(Level.ALL);

        // Create custom formatter
        CustomLogFormatter formatter = new CustomLogFormatter();

        // Create and add FileHandler (always active)
        fileHandler = new FileHandler("scrabble_game.log", true);
        fileHandler.setFormatter(formatter);
        LOGGER.addHandler(fileHandler);

        // Create ConsoleHandler (will be added/removed based on flag)
        consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);

        // Set console logging based on initial flag value
        setConsoleLogging(consoleLoggingEnabled);

        // Disable parent handlers
        LOGGER.setUseParentHandlers(false);

        LOGGER.info("Logger setup completed.");
    } catch (IOException e) {
        System.err.println("Failed to set up logger: " + e.getMessage());
    }
}



   /**
 * Enables or disables console logging.
 *
 * @param enabled true to enable console logging, false to disable it
 */
public static void setConsoleLogging(boolean enabled) {
    consoleLoggingEnabled = enabled;
    if (enabled && !Arrays.asList(LOGGER.getHandlers()).contains(consoleHandler)) {
        LOGGER.addHandler(consoleHandler);
        LOGGER.info("Console logging enabled");
    } else if (!enabled) {
        LOGGER.removeHandler(consoleHandler);
        LOGGER.info("Console logging disabled");
    }
}


/**
 * CustomLogFormatter is a custom formatter for the logger.
 * It formats log records to include the date, time, log level, source class,
 * source method, and the log message.
 */
public static class CustomLogFormatter extends java.util.logging.Formatter {
    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS ", new Date(record.getMillis())));
        sb.append(String.format("%-7s", record.getLevel().getName()));
        sb.append(" [").append(record.getSourceClassName()).append(".");
        sb.append(record.getSourceMethodName()).append("] ");
        sb.append(formatMessage(record));
        sb.append("\n");
        return sb.toString();
    }
}

    /**
 * Sets up the various dialogs used in the game GUI.
 * This includes the dialog for choosing play direction,
 * the dialog for selecting a character for an empty tile,
 * and various alert dialogs for different game scenarios.
 */
private void setupDialogs() {
    setupChoosePlayDirectionDialog();
    setupEmptyTileDialog();
    setupAlerts();
}

    /**
 * Sets up the difficulty selector ComboBox.
 * This ComboBox allows the user to select the AI difficulty level.
 * The default difficulty is set to "Hard".
 * A tooltip is added to provide additional information to the user.
 */
private void setupDifficultySelector() {
    difficultySelector = new ComboBox<>();
    difficultySelector.getItems().addAll("Easy", "Medium", "Hard");
    difficultySelector.setValue("Hard"); // Default difficulty
    difficultySelector.setOnAction(event -> handleDifficultyChange());

    // Add tooltip
    Tooltip tooltip = new Tooltip("Select AI difficulty");
    difficultySelector.setTooltip(tooltip);
}

    /**
 * Handles the change in AI difficulty level.
 * This method is triggered when the user selects a different difficulty level
 * from the difficulty selector ComboBox.
 */
private void handleDifficultyChange() {
    String selectedDifficulty = difficultySelector.getValue();
    gameManager.setAIDifficulty(selectedDifficulty);
    LOGGER.info("AI difficulty changed to: " + selectedDifficulty);
}

/**
 * Sets up the dialog for choosing the play direction.
 * This dialog allows the user to choose whether they want to play horizontally or vertically.
 */
private void setupChoosePlayDirectionDialog() {
    choosePlayDirectionDialog = new ChoiceDialog<>(HORIZONTAL, HORIZONTAL, VERTICAL);
    choosePlayDirectionDialog.setTitle(null);
    choosePlayDirectionDialog.setHeaderText("Do you want to play horizontally or vertically?\n" +
            "If you are playing one letter, is your intended word vertical or horizontal?");
}

/**
 * Sets up the dialog for selecting a character for an empty tile.
 * This dialog allows the user to choose a letter to represent an empty tile.
 */
private void setupEmptyTileDialog() {
    List<Character> alphabet = IntStream.rangeClosed('A', 'Z')
            .mapToObj(ch -> (char) ch)
            .collect(Collectors.toList());
    emptyTileDialog = new ChoiceDialog<>('A', alphabet);
    emptyTileDialog.setTitle(EMPTY_TILE_TITLE);
    emptyTileDialog.setHeaderText("What do you want to play for empty tile?");
}

    /**
 * Sets up the various alert dialogs used in the game GUI.
 * This includes alerts for no play direction, not making a move,
 * illegal choices, no word formed, invalid words, and no give up on the first move.
 */
private void setupAlerts() {
    noPlayDirectionAlert = createWarningAlert("You did not choose alignment. Choose Horizontal or Vertical\nPlease try again");
    notMakingMoveAlert = createWarningAlert("You are not making a move. Please click on \"Place Tiles\" button.");
    illegalChoiceAlert = createWarningAlert("Illegal choice.\nPlease try again");
    noWordAlert = createWarningAlert("No word formed. Please try again");
    notValidWordAlert = createWarningAlert("Not a valid word in: " + dictionaryFile + "\nPlease try again");
    noGiveUpAlert = createWarningAlert("You cannot forfeit the turn on the first move. Please make a move or click on \"Place Tiles\" button.");

    gameOverAlert = new Alert(Alert.AlertType.INFORMATION);
    gameOverAlert.setHeaderText(GAME_OVER_TITLE);
}

/**
 * Creates a warning alert with the specified content.
 *
 * @param content the content text for the alert
 * @return the created Alert object
 */
private Alert createWarningAlert(String content) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setHeaderText(WARNING_TITLE);
    alert.setContentText(content);
    return alert;
}


    /**
 * Sets up the buttons used in the game GUI.
 * This method initializes and styles the buttons, and sets their action handlers.
 */
private void setupButtons() {
    // Common style for all buttons
    String buttonStyle = "-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-size: 16px; " +
            "-fx-padding: 10px 20px; -fx-background-radius: 5px;";

    makeMoveButton = createStyledButton("Place Tiles", buttonStyle);
    makeMoveButton.setOnAction(event -> handleMakeMoveButtonClick());

    undoLastMoveButton = createStyledButton("Undo Last Move", buttonStyle);
    undoLastMoveButton.setOnAction(event -> handleUndoLastMove());

    executeMoveButton = createStyledButton("Make Move", buttonStyle);
    executeMoveButton.setOnAction(event -> handleMakeMove());

    giveUpTurnButton = createStyledButton("Forfeit Turn", buttonStyle);
    giveUpTurnButton.setOnAction(event -> handleGiveUpTurn());
}

/**
 * Creates a styled button with the specified text and style.
 * This method also adds hover and click effects, and a drop shadow to the button.
 *
 * @param text the text to display on the button
 * @param style the CSS style to apply to the button
 * @return the created Button object
 */
private Button createStyledButton(String text, String style) {
    Button button = new Button(text);
    button.setStyle(style);
    button.setFont(Font.font("Arial", 16));
    button.setTextFill(Color.WHITE);

    // Add hover effect
    button.setOnMouseEntered(e -> button.setStyle(style + "-fx-background-color: #3a7ac8;"));
    button.setOnMouseExited(e -> button.setStyle(style));

    // Add click effect
    button.setOnMousePressed(e -> button.setStyle(style + "-fx-background-color: #2a5a98;"));
    button.setOnMouseReleased(e -> button.setStyle(style + "-fx-background-color: #3a7ac8;"));

    // Add drop shadow
    DropShadow shadow = new DropShadow();
    shadow.setColor(Color.gray(0.5));
    button.setEffect(shadow);

    return button;
}

    /**
 * Handles the action when the "Place Tiles" button is clicked.
 * This method prompts the user to choose the play direction (horizontal or vertical).
 * If a direction is chosen, it sets the game state to allow the human player to make a move.
 * If no direction is chosen, it shows an alert and logs a warning.
 */
    private void handleMakeMoveButtonClick() {
        if (!gameManager.getBoard().isHumanPlaying()) {
            gameManager.getBoard().setHumanPlaying(true);
            humanTray.setMakeMoveOn(true);
            LOGGER.info("Human player started move");
            gameManager.debugSuggestHumanMove();
        }
    }

/**
 * Handles the action when the "Undo Last Move" button is clicked.
 * This method discards the last move made by the human player, updates the board and human tray,
 * and logs the actions taken.
 */
private void handleUndoLastMove() {
    if (gameManager.getBoard().isHumanPlaying() && !gameManager.getBoard().getCurrentMove().isEmpty()) {
        int[] lastMove = gameManager.getBoard().getCurrentMove().getLast();
        LOGGER.info("Discarding last move: " + Arrays.toString(lastMove));
        gameManager.getBoard().removeTile(lastMove[0], lastMove[1]);
        LOGGER.info("Board after discarding move: " + gameManager.getBoard());
        boardGui.draw();
        gameManager.getBoard().getCurrentMove().removeLast();
        humanTray.getPlayedTileIndices().remove(humanTray.getPlayedTileIndices().size() - 1);
        humanTray.draw();
        LOGGER.info("Human tray after discarding: " + gameManager.getHumanRack());
    }
}


    /**
 * Sets up the board interface for the game.
 * This method adds a mouse click event handler to the board GUI.
 * When the board is clicked, it checks if the human player is making a move,
 * validates the move, and updates the board and human tray accordingly.
 */
    private void setupBoardInterface() {
        boardGui.setOnMouseClicked(event -> {
            if (gameManager.getBoard().isHumanPlaying() &&
                    gameManager.getBoard().isNewCharSelected() &&
                    !humanTray.isLastTilePlayed()) {

                int indexX = (int) (event.getY() / BoardGui.SQUARE_SIZE);
                int indexY = (int) (event.getX() / BoardGui.SQUARE_SIZE);

                if (gameManager.getBoard().hasExistingLetter(indexX, indexY)) {
                    illegalChoiceAlert.showAndWait();
                    LOGGER.warning("Illegal choice: Tile already exists at: " + indexX + ", " + indexY);
                    return;
                }

                // Determine orientation after second tile
                gameManager.getBoard().determineOrientation(indexX, indexY);

                gameManager.getBoard().getCurrentMove().add(new int[]{indexX, indexY});

                if (gameManager.getBoard().isValidHumanMove()) {
                    LOGGER.info("Move is legal. Placing tile at: " + indexX + ", " + indexY);
                    gameManager.getBoard().placeTile(gameManager.getBoard().getCurrentPlayingChar(), indexX, indexY);
                    gameManager.getBoard().setNewCharSelected(false);
                    humanTray.setLastTilePlayed(true);
                    boardGui.draw();
                } else {
                    LOGGER.warning("Illegal move at: " + indexX + ", " + indexY);
                    gameManager.getBoard().getCurrentMove().removeLast();
                    humanTray.setLastTilePlayed(true);
                    gameManager.getBoard().setNewCharSelected(false);
                    humanTray.getPlayedTileIndices().remove(humanTray.getPlayedTileIndices().size() - 1);
                    humanTray.draw();
                    illegalChoiceAlert.showAndWait();
                }
            }
        });
    }

    /**
 * Sets up the interaction for the human tray.
 * This method adds a mouse click event handler to the human tray.
 * When the human tray is clicked, it checks if the human player is making a move,
 * and handles the tile selection accordingly.
 */
private void setupHumanTrayInteraction() {
    humanTray.setOnMouseClicked(event -> {
        if (humanTray.isMakeMoveOn()) {
            handleTileSelection(event.getX());
        } else {
            notMakingMoveAlert.showAndWait();
        }
    });
}

/**
 * Handles the tile selection when the human tray is clicked.
 * This method determines the clicked tile index, updates the played tiles,
 * and selects the character for the clicked tile.
 *
 * @param clickX the x-coordinate of the mouse click
 */
private void handleTileSelection(double clickX) {
    LinkedList<Tile> tileList = gameManager.getHumanRack().getAllTiles();
    int clickedIndex = (int) (clickX / 40);
    List<Integer> playedTiles = humanTray.getPlayedTileIndices();
    LOGGER.info("Tile clicked at index: " + clickedIndex);

    if (clickX <= 280 && !playedTiles.contains(clickedIndex)) {
        updatePlayedTiles(playedTiles, clickedIndex);
        char selectedChar = selectCharacter(tileList.get(clickedIndex).getLetter().charAt(0));
        LOGGER.info("Selected character: " + selectedChar);

        if (selectedChar != (char) 0) {
            gameManager.getBoard().setNewCharSelected(true);
            gameManager.getBoard().setCurrentPlayingChar(selectedChar);
            humanTray.draw();
            LOGGER.info("Human tray after selecting tile: " + gameManager.getHumanRack());
        }
    }
}
    /**
 * Updates the list of played tiles.
 * If the last tile was not played, it removes the last tile from the list.
 * Adds the clicked index to the list of played tiles.
 *
 * @param playedTiles the list of played tile indices
 * @param clickedIndex the index of the clicked tile
 */
private void updatePlayedTiles(List<Integer> playedTiles, int clickedIndex) {
    if (!humanTray.isLastTilePlayed()) {
        playedTiles.remove(playedTiles.size() - 1);
    }
    humanTray.setLastTilePlayed(false);
    playedTiles.add(clickedIndex);
    LOGGER.info("Played tiles: " + playedTiles);
}

/**
 * Selects a character for the given tile.
 * If the tile is an empty tile ('*'), it shows a dialog to select a character.
 * If a character is selected, it returns the selected character.
 * If no character is selected, it removes the last played tile index and returns 0.
 * Logs the selected character.
 *
 * @param c the character of the tile
 * @return the selected character or 0 if no character is selected
 */
private char selectCharacter(char c) {
    if (c == '*') {
        Optional<Character> result = emptyTileDialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        } else {
            humanTray.getPlayedTileIndices().remove(humanTray.getPlayedTileIndices().size() - 1);
            return (char) 0;
        }
    }
    LOGGER.info("Selected empty character: " + c);
    return c;
}

/**
 * Handles the action when the "Make Move" button is clicked.
 * Validates the move and processes it if valid.
 * Shows appropriate alerts and logs warnings for invalid moves.
 * Resets the move state after processing the move.
 */
private void handleMakeMove() {
    if (humanTray.isMakeMoveOn()) {
        String word = gameManager.getBoard().getPlayedWord();
        if (gameManager.getBoard().getCurrentMove().isEmpty()) {
            noWordAlert.showAndWait();
        } else if (!gameManager.continuousHumanMove()) {
            removeInvalidMove();
            LOGGER.warning("Illegal choice: Tiles are not placed in a continuous line");
            illegalChoiceAlert.showAndWait();
        } else if (isInvalidFirstMove()) {
            removeInvalidMove();
            LOGGER.warning("Illegal choice: First move must be at least 2 tiles long and must contain the center tile");
            illegalChoiceAlert.showAndWait();
        } else if (!gameManager.isValidWordByHuman(word)) {
            removeInvalidMove();
            notValidWordAlert.showAndWait();
            LOGGER.warning("Not a valid word: " + word);
        } else {
            processMakeMove(word);
            LOGGER.info("Human move processed: " + word);

            // Set isFirstMove to false after the first valid move
            if (gameManager.isFirstMove()) {
                gameManager.setFirstMove(false);
                LOGGER.info("First move completed and isFirstMove set to " + gameManager.isFirstMove());
            }
        }
        resetMoveState();
        LOGGER.info("Move state reset");
    }
}

    /**
     * Removes the invalid move from the board.
     * Iterates through the current move and removes each tile from the board.
     * Redraws the board after removing the invalid move.
     */
    private void removeInvalidMove() {
        for (int[] a : gameManager.getBoard().getCurrentMove()) {
            gameManager.getBoard().removeTile(a[0], a[1]);
            LOGGER.info("Removing invalid move at: " + a[0] + ", " + a[1]);
        }
        boardGui.draw();
        LOGGER.info("Board after removing invalid move: " + gameManager.getBoard());
    }

    /**
     * Checks if the first move is invalid.
     * The first move is invalid if it does not contain the center tile or is less than 2 tiles long.
     *
     * @return true if the first move is invalid, false otherwise
     */
    private boolean isInvalidFirstMove() {
        LOGGER.info("Checking if first move is invalid");
        return gameManager.isFirstMove() &&
                (!gameManager.getBoard().hasExistingLetter(gameManager.getBoard().getDimension() / 2, gameManager.getBoard().getDimension() / 2) ||
                        gameManager.getBoard().getCurrentMove().size() < 2);
    }

    /**
     * Processes the human player's move.
     * Handles the move, updates the human tray and score board, and checks if the game is over.
     *
     * @param word the word formed by the human player's move
     */
    private void processMakeMove(String word) {
        gameManager.setFirstMove(false);
        LinkedList<int[]> coordinates = gameManager.getBoard().getMoveIndexes();
        gameManager.handleHumanMove(word, coordinates);
        LOGGER.info("Human move handled");
        updateHumanTray();
        updateScoreBoard();
        LOGGER.info("Score board updated");
        LOGGER.info("Human tray updated: " + gameManager.getHumanRack());
        if (gameManager.isGameOver()) {
            showGameOverDialog();
        }
    }

    /**
     * Updates the human player's tray.
     * Replaces played tiles with empty tiles, removes empty tiles from the tray,
     * and adds random tiles from the tile bag to the tray.
     */
    private void updateHumanTray() {
        for (int i : humanTray.getPlayedTileIndices()) {
            gameManager.getHumanPlayer().getRack().getAllTiles().set(i, new Tile(-1, -1, "?", 0));
            LOGGER.info("Tile at index " + i + " replaced with empty tile");
        }
        gameManager.getHumanPlayer().getRack().getAllTiles().removeIf(tile -> tile.getLetter().equals("?"));
        LOGGER.info("Empty tiles removed from human tray");

        int tilesToAdd = Math.min(humanTray.getPlayedTileIndices().size(), gameManager.getTileBag().count());
        gameManager.getHumanRack().appendRandomTiles(tilesToAdd, gameManager.getTileBag());
        LOGGER.info("Random tiles added to human tray: " + tilesToAdd);
    }

    /**
     * Handles the action when the "Forfeit Turn" button is clicked.
     * If it is the first move, shows an alert and logs a warning.
     * Otherwise, handles the human player's turn forfeit, updates the score board,
     * checks if the game is over, and resets the move state.
     */
    private void handleGiveUpTurn() {
        LOGGER.info("Give up turn button clicked");
        if (gameManager.isFirstMove()) {
            noGiveUpAlert.showAndWait();
            LOGGER.warning("Cannot forfeit turn on first move");
        } else {
            gameManager.handleHumanGiveUpTurn();
            updateScoreBoard();
            LOGGER.info("Human turn forfeited");
            if (gameManager.isGameOver()) {
                showGameOverDialog();
                LOGGER.info("Game is over");
            }

            resetMoveState();
            LOGGER.info("Move state reset after forfeiting turn");
        }
    }

    /**
 * Sets up the layout for the game GUI.
 * This method configures the main content area, control panel, and right panel,
 * and adds them to the root BorderPane.
 *
 * @param root the root BorderPane to set up the layout
 */
private void setupLayout(BorderPane root) {
    // Create a VBox for the main content area
    VBox mainContent = new VBox(20);
    mainContent.setAlignment(Pos.CENTER);
    mainContent.setPadding(new Insets(20));
    mainContent.getChildren().add(boardGui);

    // Create a more flexible control panel using FlowPane
    FlowPane controlPanel = new FlowPane(Orientation.HORIZONTAL, 10, 10);
    controlPanel.setAlignment(Pos.CENTER);
    controlPanel.setPadding(new Insets(15));
    controlPanel.setStyle("-fx-background-color: #113745; -fx-background-radius: 10;");
    controlPanel.getChildren().addAll(
            humanTray,
            makeMoveButton,
            undoLastMoveButton,
            executeMoveButton,
            giveUpTurnButton
    );
    LOGGER.info("Control panel initialized");

    // Add the control panel to the main content
    mainContent.getChildren().add(controlPanel);

    // Create a styled VBox for the right panel
    VBox rightPanel = new VBox(20);
    rightPanel.setAlignment(Pos.TOP_CENTER);
    rightPanel.setPadding(new Insets(20));
    rightPanel.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 10;");

    // Add a title to the score board
    Label scoreBoardTitle = new Label("Score Board");
    scoreBoardTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    rightPanel.getChildren().addAll(scoreBoardTitle, scoreBoard);

    // Set up the root BorderPane
    root.setCenter(mainContent);
    root.setRight(rightPanel);

    controlPanel.getChildren().add(difficultySelector);

    // Add some overall styling to the root
    root.setStyle("-fx-background-color: #e0e0e0;");

    LOGGER.info("Layout setup completed");
}

    /**
     * Updates the score board with the current scores and remaining tiles.
     * This method retrieves the computer score, human score, and the count of remaining tiles
     * from the game manager and updates the score board accordingly.
     * It also logs the updated scores and remaining tiles.
     */
    private void updateScoreBoard() {
        scoreBoard.setComputerScore(gameManager.getComputerScore());
        scoreBoard.setHumanScore(gameManager.getHumanScore());
        scoreBoard.setRemainingTiles(gameManager.getTileBag().count());
        LOGGER.info("Score board updated. Computer score: " + gameManager.getComputerScore() +
                ", Human score: " + gameManager.getHumanScore() +
                ", Remaining tiles: " + gameManager.getTileBag().count());
    }

    /**
     * Displays the game over dialog with the final scores.
     * This method constructs a message with the human and computer scores,
     * determines the winner, and sets the appropriate content text for the game over alert.
     * It then shows the alert and closes the stage.
     * It also logs the display of the game over dialog and the closure of the game.
     */
    private void showGameOverDialog() {
        String str = "Your score: " + gameManager.getHumanScore() + "\nComputer score: " + gameManager.getComputerScore();
        if (gameManager.getWinner().equals(gameManager.getComputerPlayer())) {
            gameOverAlert.setContentText("Sorry! You lost!\n" + str);
        } else if (gameManager.getWinner().equals(gameManager.getHumanPlayer())) {
            gameOverAlert.setContentText("Congratulations! You won!\n" + str); // not gonna happen
        } else {
            gameOverAlert.setContentText("Scores tied\n" + str); // no shot but do it anyway
        }
        gameOverAlert.showAndWait();
        stage.close();
        LOGGER.info("Game over dialog displayed and game closed");
    }



    /**
 * Resets the state of the current move.
 * This method clears the human tray and board states related to the current move,
 * including played tiles, move indexes, and the played word.
 * It also redraws the board and human tray to reflect the reset state.
 */
private void resetMoveState() {
    humanTray.setLastTilePlayed(true);
    humanTray.setMakeMoveOn(false);
    gameManager.getBoard().setNewCharSelected(false);
    gameManager.getBoard().setMovingDx(false);
    gameManager.getBoard().setHumanPlaying(false);
    gameManager.getBoard().getCurrentMove().clear();
    gameManager.getBoard().getMoveIndexes().clear();
    gameManager.getBoard().setPlayedWord("");
    humanTray.getPlayedTileIndices().clear();
    boardGui.draw();
    humanTray.draw();
}


    /**
 * The main method is the entry point of the application.
 * It expects two command-line arguments: the dictionary file and the board configuration file.
 * If the arguments are not provided, it prints the usage message and exits.
 * Otherwise, it initializes the dictionary and board configuration files and launches the JavaFX application.
 *
 * @param args command-line arguments: <dictionary_file> <board_config_file>
 */
public static void main(String[] args) {
    if (args.length != 2) {
        System.out.println("Usage: java -jar scrabble.jar <dictionary_file> <board_config_file>");
        System.exit(1);
    }
    dictionaryFile = args[0];
    boardConfigFile = args[1];
    launch(args);
}
}