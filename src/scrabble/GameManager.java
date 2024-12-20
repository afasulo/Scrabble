package scrabble;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * GameManager manages the flow of the Scrabble game, including player turns,
 * scoring, and game state. It coordinates interactions between the game board,
 * players, and tiles.
 */
public class GameManager {
    private static final Logger LOGGER = Logger.getLogger(GameManager.class.getName());
    private static final int DEFAULT_BOARD_DIMENSION = 15;
    private static final String TILE_CONFIG_FILE = "dictionaries_and_examples/scrabble_tiles.txt";

    public boolean debugMode = false;

    private Dictionary dictionary;
    private ScoreMap scoreMap;
    private GameBoard gameBoard;
    private Rack humanRack;
    private Rack computerRack;
    private TileBag tileBag;
    private Player humanPlayer;
    private ComputerPlayer computerPlayer;
    private char turn;
    private boolean isGameOver;
    private int humanScore;
    private int computerScore;
    private String lastWordPlayedByHuman;
    private String lastWordPlayedByComputer;
    private Player winner;
    private boolean humanGaveUpTurn;
    private boolean isFirstMove;

    /**
     * Constructs a GameManager instance with the specified dictionary and board configuration files.
     *
     * @param dictionaryFile the file containing the dictionary for the game
     * @param boardConfigFile the file containing the board configuration
     * @throws IOException if there is an error reading the files
     */
    public GameManager(String dictionaryFile, String boardConfigFile) throws IOException {
        LOGGER.info("Initializing GameManager with dictionary: " + dictionaryFile + ", board config: " + boardConfigFile);

        this.computerScore = 0;
        try {
            initializeScoreMap();
            initializeDictionary(dictionaryFile);
            initializeBoard(boardConfigFile);

            LOGGER.info("Initializing game components...");
            tileBag = initializeTileBag();
            humanRack = new Rack(tileBag);
            computerRack = new Rack(tileBag);
            humanPlayer = new Player(humanRack);
            computerPlayer = new ComputerPlayer(computerRack, gameBoard, dictionary);

            LOGGER.log(Level.INFO, "Initial human rack: {0}",
                    humanRack.getLetters().stream().collect(Collectors.joining(", ")));
            LOGGER.log(Level.INFO, "Initial computer rack: {0}",
                    computerRack.getLetters().stream().collect(Collectors.joining(", ")));

            turn = 'H';
            isGameOver = false;
            humanScore = 0;
            lastWordPlayedByHuman = "";
            lastWordPlayedByComputer = "";
            winner = null;
            humanGaveUpTurn = false;
            this.isFirstMove = true;

            gameBoard.setDictionary(dictionary);
            LOGGER.info("GameManager initialization complete");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize GameManager", e);
            throw e;
        }
    }

    /**
     * Initializes the score map from the tile configuration file.
     *
     * @throws IOException if there is an error reading the score configuration file
     */
    private void initializeScoreMap() throws IOException {
        LOGGER.info("Initializing ScoreMap from: " + TILE_CONFIG_FILE);
        try (BufferedReader br = new BufferedReader(new FileReader(TILE_CONFIG_FILE))) {
            scoreMap = new ScoreMap(br);
            LOGGER.info("ScoreMap initialized successfully");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load ScoreMap from " + TILE_CONFIG_FILE, e);
            throw new IOException("Error loading scrabble_tiles.txt: " + e.getMessage(), e);
        }
    }

    /**
     * Initializes the dictionary from the specified file.
     *
     * @param dictionaryFile the file containing the dictionary words
     * @throws IOException if there is an error reading the dictionary file
     */
    private void initializeDictionary(String dictionaryFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(dictionaryFile))) {
            dictionary = new Dictionary(br);
        } catch (IOException e) {
            throw new IOException("Error loading dictionary file: " + e.getMessage());
        }
    }

    /**
     * Initializes the game board from the specified configuration file.
     *
     * @param boardConfigFile the file containing the board configuration
     * @throws IOException if there is an error reading the board configuration file
     */
    private void initializeBoard(String boardConfigFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(boardConfigFile))) {
            String dimensionLine = br.readLine();
            int dimension = dimensionLine != null ? Integer.parseInt(dimensionLine.trim()) : DEFAULT_BOARD_DIMENSION;
            gameBoard = new GameBoard(dimension);  // Create board without ScoreMap

            StringBuilder boardConfig = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                boardConfig.append(line).append(" ");
            }
            gameBoard.setScoreMap(scoreMap);  // Set ScoreMap after creation
            gameBoard.configBoard(boardConfig.toString().trim(), scoreMap);  // Pass scoreMap here if needed
        } catch (IOException e) {
            throw new IOException("Error loading board configuration: " + e.getMessage());
        }
    }

    /**
     * Initializes the tile bag using the score map.
     *
     * @return a new TileBag instance populated with tiles
     */
    private TileBag initializeTileBag() {
        // We're already reading the TILE_CONFIG_FILE in initializeScoreMap()
        // So we can use the existing scoreMap to create the TileBag
        return new TileBag(TILE_CONFIG_FILE);
    }

    /**
     * Prints a debug message if debug mode is enabled.
     *
     * @param message the message to print
     */
    public void debugPrint(String message) {
        if (debugMode) {
            System.out.println("[DEBUG] " + message);
        }
    }

    /**
     * Prints the current game state for debugging purposes.
     *
     * @param context the context in which the game state is printed
     */
    public void debugPrintGameState(String context) {
        if (!debugMode) return;

        System.out.println("===== Debug Game State: " + context + " =====");
        System.out.println("Human Tray: " + humanRack);
        System.out.println("Computer Tray: " + computerRack);
        System.out.println("Tile Bag: " + tileBag);
        System.out.println("Is First Move: " + isFirstMove);
        System.out.println("Human Score: " + humanScore);
        System.out.println("Computer Score: " + computerScore);
        System.out.println("Last Word Played By Human: " + lastWordPlayedByHuman);
        System.out.println("Last Word Played By Computer: " + lastWordPlayedByComputer);
        System.out.println("Is Game Over: " + isGameOver);
        System.out.println("Current Turn: " + (turn == 'H' ? "Human" : "Computer"));
        System.out.println("Board State:");
        System.out.println(gameBoard.toString());
        System.out.println("============================");
    }

    /**
     * Returns the dictionary used in the game.
     *
     * @return the dictionary
     */
    public Dictionary getDictionary() {
        return dictionary;
    }

    /**
     * Returns the score map used for scoring tiles.
     *
     * @return the score map
     */
    public ScoreMap getScoreMap() {
        return scoreMap;
    }

    /**
     * Returns the game board.
     *
     * @return the game board
     */
    public GameBoard getBoard() {
        return gameBoard;
    }

    /**
     * Suggests a move for the human player based on the current game state.
     */
    public void debugSuggestHumanMove() {
        if (!debugMode) return;

        System.out.println("===== Suggesting Move for Human Player =====");

        // Create a temporary solver for the human player
        ScrabbleMoveGenerator humanScrabbleMoveGenerator = new ScrabbleMoveGenerator(gameBoard, dictionary);
        humanScrabbleMoveGenerator.setAvailableTiles(convertTileListToCharList(humanRack.getAllTiles()));

        // Find the best move
        humanScrabbleMoveGenerator.determineBestMove();

        String suggestedWord = humanScrabbleMoveGenerator.getBestWord();
        LinkedList<int[]> suggestedCoordinates = humanScrabbleMoveGenerator.getBestMoveCoordinates();
        int suggestedScore = humanScrabbleMoveGenerator.getScore();

        if (suggestedWord.isEmpty()) {
            System.out.println("No valid moves found for the human player.");
        } else {
            System.out.println("Suggested word: " + suggestedWord);
            System.out.println("Suggested coordinates: " + formatCoordinates(suggestedCoordinates));
            System.out.println("Potential score: " + suggestedScore);
        }

        System.out.println("===========================================");
    }

    /**
     * Formats the coordinates for display.
     *
     * @param coordinates the list of coordinates to format
     * @return a string representation of the coordinates
     */
    private String formatCoordinates(LinkedList<int[]> coordinates) {
        StringBuilder sb = new StringBuilder("[");
        for (int[] coord : coordinates) {
            sb.append("(").append(coord[0]).append(",").append(coord[1]).append("), ");
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);  // Remove the last ", "
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Converts a list of Tile objects to a list of Characters representing their letters.
     *
     * @param tileList the list of Tile objects to convert
     * @return a LinkedList of Characters corresponding to the letters of the tiles
     */
    private LinkedList<Character> convertTileListToCharList(LinkedList<Tile> tileList) {
        LinkedList<Character> charList = new LinkedList<>();
        for (Tile tile : tileList) {
            charList.add(tile.getLetter().charAt(0));
        }
        return charList;
    }

    /**
     * Sets whether this is the first move of the game.
     *
     * @param isFirstMove true if it is the first move, false otherwise
     */
    public void setFirstMove(boolean isFirstMove) {
        this.isFirstMove = isFirstMove;
    }

    /**
     * Checks if it is the first move of the game.
     *
     * @return true if it is the first move, false otherwise
     */
    public boolean isFirstMove() {
        return isFirstMove;
    }

    /**
     * Returns the TileBag instance used in the game.
     *
     * @return the TileBag containing available tiles
     */
    public TileBag getTileBag() {
        return tileBag;
    }

    /**
     * Returns the current score of the human player.
     *
     * @return the human player's score
     */
    public int getHumanScore() {
        return humanScore;
    }

//    public void setHumanScore(int humanScore) {
//        this.humanScore = humanScore;
//    }

    /**
     * Returns the tile rack (Tray) of the human player.
     *
     * @return the human player's tile rack
     */
    public Rack getHumanRack() {
        return humanRack;
    }

    /**
     * Checks if the word played by the human player is a valid word according to the dictionary.
     *
     * @param word the word to check
     * @return true if the word is valid, false otherwise
     */
    public boolean isValidWordByHuman(String word) {
        return dictionary.isWordInDictionary(word);
    }


    /**
     * Checks if the game is over based on the current state of the players and the tile bag.
     *
     * @param humanPlayer the human player
     * @param computerPlayer the computer player
     * @return true if the game is over, false otherwise
     */
    public boolean checkGameOver(Player humanPlayer, Player computerPlayer) {
        if (tileBag.count() == 0 && (humanPlayer.getRack().count() == 0 || computerPlayer.getRack().count() == 0)) {
            return true;
        }
        if (lastWordPlayedByComputer.equals("") && lastWordPlayedByHuman.equals("")) {
            return true;
        }
        return false;
    }

    /**
     * Determines the winner of the game based on the final scores of the players.
     * This method also adjusts scores based on the remaining tiles in each player's rack.
     */
    public void setWinner() {
        int comDeductScore = 0;
        int humDeductScore = 0;
        int comAddScore = 0;
        int humAddScore = 0;

        // Deduct scores for the remaining tiles in the computer's rack
        for (String tile : computerRack.getLetters()) {
            comDeductScore += scoreMap.getScore(tile);
        }

        // Deduct scores for the remaining tiles in the human's rack
        for (String tile : humanRack.getLetters()) {
            humDeductScore += scoreMap.getScore(tile);
        }

        // Adjust scores if racks are empty
        if (computerRack.count() == 0) {
            comAddScore = humDeductScore;
        }
        if (humanRack.count() == 0) {
            humAddScore = comDeductScore;
        }

        // Calculate final scores
        int comFinScore = computerScore - comDeductScore + comAddScore;
        int humFinScore = humanScore - humDeductScore + humAddScore;

        // Determine the winner based on final scores
        if (comFinScore != humFinScore) {
            if (comFinScore > humFinScore) {
                computerScore = comFinScore;
                winner = computerPlayer;
            } else {
                humanScore = humFinScore;
                winner = humanPlayer;
            }
        } else {
            if (computerScore > humanScore) {
                winner = computerPlayer;
            } else if (humanScore > computerScore) {
                winner = humanPlayer;
            }
        }

        isGameOver = true; // Set the game as over
    }


    /**
     * Returns the computer player instance.
     *
     * @return the computer player
     */
    public Player getComputerPlayer() {
        return computerPlayer;
    }

    /**
     * Returns the human player instance.
     *
     * @return the human player
     */
    public Player getHumanPlayer() {
        return humanPlayer;
    }


    /**
     * Checks if the human player's move is continuous without gaps.
     *
     * @return true if the human's move is continuous, false otherwise
     */
    public boolean continuousHumanMove() {
        LinkedList<int[]> moveIndex = gameBoard.getCurrentMove();
        LinkedList<int[]> wordIndex = gameBoard.getMoveIndexes();

        for (int[] index : moveIndex) {
            boolean hasArr = false;
            for (int[] word : wordIndex) {
                if (index[0] == word[0] && index[1] == word[1]) {
                    hasArr = true;
                    break;
                }
            }
            if (!hasArr) {
                return false;
            }
        }
        return true;
    }


    /**
     * Sets the computer player's tile rack based on the played word and coordinates.
     *
     * @param word the word played by the computer player
     * @param coordinates the coordinates where the word is placed on the board
     */
    public void setComputerTray(String word, LinkedList<int[]> coordinates) {
        LinkedList<Character> list = new LinkedList<>();
        int count = 0;
        for (int[] arr : coordinates) {
            if (gameBoard.hasExistingLetter(arr[0], arr[1])) {
                count++;
            } else {
                list.add(word.charAt(count));
                count++;
            }
        }
        for (Character c : list) {
            if (c >= 'A' && c <= 'Z') {
                computerRack.extractTileByLetter("*");
            } else {
                computerRack.extractTileByLetter(String.valueOf(c));
            }
        }
        if (tileBag.count() >= list.size()) {
            computerRack.appendRandomTiles(list.size(), tileBag);
        } else if (tileBag.count() > 0) {
            computerRack.appendRandomTiles(tileBag.count(), tileBag);
        }

        computerPlayer.setRack(computerRack);
    }

    /**
     * Returns the current score of the computer player.
     *
     * @return the computer player's score
     */
    public int getComputerScore() {
        return computerScore;
    }

    /**
     * Returns the winner of the game.
     *
     * @return the winning player
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Checks if the game is over.
     *
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return isGameOver;
    }

    /**
     * Handles the main game loop for playing turns.
     * Updates the game state based on the current player's turn and checks for game over conditions.
     */
    public void play() {
        if (!isGameOver && turn == 'H') {
            LOGGER.info("Starting human turn");
            if (humanGaveUpTurn) {
                LOGGER.info("Human gave up turn");
                lastWordPlayedByHuman = "";
                turn = 'C';
            } else {
                processHumanTurn();
            }
        }

        if (!isGameOver && turn == 'C') {
            LOGGER.info("Starting computer turn");
            processComputerTurn();
        }

        if (checkGameOver(humanPlayer, computerPlayer)) {
            LOGGER.info("Game is over");
            isGameOver = true;
            setWinner();
            LOGGER.log(Level.INFO, "Winner: {0}, Final Scores - Human: {1}, Computer: {2}",
                    new Object[]{winner == humanPlayer ? "Human" : "Computer", humanScore, computerScore});
        }
    }

    private void processHumanTurn() {
        String word = humanPlayer.getNextMoveWord();
        LinkedList<int[]> coordinates = humanPlayer.getNextMoveCoordinates();

        if (!word.isEmpty()) {
            LOGGER.log(Level.INFO, "Human played word: {0} at coordinates: {1}",
                    new Object[]{word, formatCoordinates(coordinates)});
            gameBoard.updateBoard(word, coordinates);
            turn = 'C';
        } else {
            LOGGER.info("Human made no move");
            turn = 'C';
        }
    }

    private void processComputerTurn() {
        computerPlayer.generateNextMove();
        if (computerPlayer.hasNoValidMoves()) {
            LOGGER.info("Computer has no valid moves");
            lastWordPlayedByComputer = "";
            turn = 'H';
        } else {
            String word = computerPlayer.getNextMoveWord();
            LinkedList<int[]> coordinates = computerPlayer.getNextMoveCoordinates();

            LOGGER.log(Level.INFO, "Computer playing word: {0} at coordinates: {1}",
                    new Object[]{word, formatCoordinates(coordinates)});

            setComputerTray(word, coordinates);

            if (!word.isEmpty()) {
                gameBoard.updateBoard(word, coordinates);
                int moveScore = computerPlayer.getBestMoveScore();
                computerScore += moveScore;
                LOGGER.log(Level.INFO, "Computer score updated: +{0} to {1}",
                        new Object[]{moveScore, computerScore});
                turn = 'H';
            } else {
                LOGGER.info("Computer made no move");
                turn = 'C';
            }
            lastWordPlayedByComputer = word;
        }
    }

    /**
     * Handles the human player's move by updating the game board and scores.
     * call this a bunch of times to see how computer plays
     *
     * @param word the word played by the human player
     * @param coordinates the coordinates where the word is placed on the board
     */
    public void handleHumanMove(String word, LinkedList<int[]> coordinates) {
        if (isGameOver || turn != 'H') {
            return;
        }

        if (word.isEmpty()) {
            lastWordPlayedByHuman = "";
        } else {
            gameBoard.updateBoard(word, coordinates);
            humanScore += gameBoard.calculateTotalScore(coordinates, word, humanRack.count() == 0);
            lastWordPlayedByHuman = word;
        }

        turn = 'C'; // Switch turn to computer
        play(); // Trigger computer's turn
    }

    /**
     * Handles the scenario when the human player gives up their turn.
     */
    public void handleHumanGiveUpTurn() {
        if (isGameOver || turn != 'H') {
            return;
        }

        lastWordPlayedByHuman = "";
        turn = 'C'; // Switch turn to computer
        play(); // Trigger computer's turn
    }

    /**
     * Sets the difficulty level for the computer player.
     *
     * @param difficulty the difficulty level to set for the computer player
     */
    public void setAIDifficulty(String difficulty) {
        if (computerPlayer instanceof ComputerPlayer) {
            ComputerPlayer computerAI = (ComputerPlayer) computerPlayer;
            computerAI.setComputerPlayerDifficulty(difficulty);
        }
    }




}