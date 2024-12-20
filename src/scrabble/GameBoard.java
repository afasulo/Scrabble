package scrabble;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.*;

public class GameBoard {
    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final String DEFAULT_POINT = "*";
    private static final String ANCHOR_POINT = "A";
    private static final Logger LOGGER = Logger.getLogger(GameGui.class.getName());
    private static boolean consoleLoggingEnabled = true;
    private static ConsoleHandler consoleHandler;
    private static FileHandler fileHandler;
    public final int dimn;
    private ScoreMap scoreMap;
    private Tile[][] gameBoard;
    private LinkedList<int[]> anchorPositions;
    private LinkedList<int[]> currentMove;
    private LinkedList<int[]> moveIndexes;
    private boolean ro;
    private Dictionary dictionary;
    private boolean movingDx;
    private boolean humanPlaying;
    private boolean newCharSelected;
    private String playedWord;
    private boolean firstMove;
    private LinkedList<int[]> boardConfig;
    private char currentPlayingChar;
    private String[][] anchorPoints;


    /**
     * Constructs a GameBoard with the specified dimension.
     * Initializes the logger and the game board.
     *
     * @param dimn the dimension of the game board
     * @throws IllegalArgumentException if the dimension is less than or equal to zero
     */
    public GameBoard(int dimn) {
        setupLogger();
        if (dimn <= 0) {
            throw new IllegalArgumentException("Board dimension must be positive");
        }
        this.dimn = dimn;
        initializeBoard();
    }

    /**
     * Constructs a GameBoard with the specified dimension and score map.
     *
     * @param dimn     the dimension of the game board
     * @param scoreMap the score map to be used for scoring tiles
     */
    public GameBoard(int dimn, ScoreMap scoreMap) {
        this(dimn);
        this.scoreMap = scoreMap;
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
     * Initializes the game board and related properties.
     * Sets up the initial state of the game, including the board configuration,
     * anchor positions, current move, move indexes, and anchor points.
     * Also sets the initial values for various flags such as firstMove, newCharSelected,
     * humanPlaying, and movingDx.
     */
    private void initializeBoard() {
        this.firstMove = true;
        this.newCharSelected = false;
        this.humanPlaying = false;
        this.movingDx = false;
        this.boardConfig = new LinkedList<>();
        this.gameBoard = new Tile[dimn][dimn];
        this.anchorPositions = new LinkedList<>();
        this.currentMove = new LinkedList<>();
        this.moveIndexes = new LinkedList<>();
        this.anchorPoints = new String[dimn][dimn];
        LOGGER.info("Board initialized with dimension " + dimn);
    }

    /**
     * Sets up the logger for the GameBoard class.
     * This method configures the logger to log messages to both a file and the console.
     * It removes any existing handlers, sets the logging level, and adds custom handlers
     * with a custom formatter. Console logging can be enabled or disabled based on a flag.
     *
     * @throws IOException if there is an error setting up the file handler
     */
    private void setupLogger() {
        try {
            // Remove all existing handlers
            for (Handler handler : LOGGER.getHandlers()) {
                LOGGER.removeHandler(handler);
            }

            LOGGER.setLevel(Level.ALL);

            // Create custom formatter
            GameGui.CustomLogFormatter formatter = new GameGui.CustomLogFormatter();

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
     * Configures the game board with the given input and score map.
     * If the score map is not already set, it will be initialized with the provided score map.
     * The input string should contain the tiles to be placed on the board, separated by spaces.
     * The number of tiles in the input must match the board dimensions.
     *
     * @param input    the string containing the tiles to be placed on the board
     * @param scoreMap the score map to be used for scoring tiles
     * @throws IllegalArgumentException if the input does not match the board dimensions
     */
    public void configBoard(String input, ScoreMap scoreMap) {
        if (this.scoreMap == null) {
            this.scoreMap = scoreMap;
        }
        String[] tiles = input.trim().split("\\s+");
        if (tiles.length != dimn * dimn) {
            throw new IllegalArgumentException("Input does not match board dimensions");
        }

        List<int[]> newTiles = new ArrayList<>();

        for (int r = 0; r < dimn; r++) {
            for (int c = 0; c < dimn; c++) {
                int index = r * dimn + c;
                String tileStr = tiles[index];
                Tile tile = createTile(r, c, tileStr, scoreMap);
                gameBoard[r][c] = tile;
                if (tileStr.length() == 1) {
                    newTiles.add(new int[]{r, c});
                }
            }
        }

        updateAnchorPositions(newTiles);
    }

    /**
     * Creates a Tile object based on the given letter.
     * letter comes from board file and is a string representation of the tile,
     * if letter == ".." then the tile is empty, if letter == "2." then the tile is a double word tile,
     * if letter == ".L" then the tile is a letter multiplier tile, if letter == "A" then the tile is an anchor point.
     * Otherwise, the tile is a regular tile with a letter and score.
     *
     * @param row      the row index of the tile
     * @param col      the column index of the tile
     * @param letter   the string representation of the tile
     * @param scoreMap the score map to be used for scoring tiles
     * @return a Tile object representing the given letter
     * @throws IllegalArgumentException if the letter representation is invalid
     */
    private Tile createTile(int row, int col, String letter, ScoreMap scoreMap) {
        if (letter.length() == 2) {
            return new Tile(row, col, letter, 0);
        } else if (letter.length() == 1) {
            int score = scoreMap.getScore(letter.toLowerCase());
            return new Tile(row, col, letter, score);
        } else {
            LOGGER.severe("Invalid tile format: " + letter);
            throw new IllegalArgumentException("Invalid tile format: " + letter);
        }
    }


    /**
     * Updates the positions of anchor points on the game board.
     * If no new tiles are provided, sets the anchor to the center of the board.
     *
     * @param newTiles a list of coordinates for the new tiles placed on the board
     */
    private void updateAnchorPositions(List<int[]> newTiles) {
        LOGGER.info("Updating anchors with new tiles: " + newTiles);
        if (newTiles.isEmpty()) {
            anchorPositions.add(new int[]{dimn / 2, dimn / 2});
            LOGGER.info("No new tiles. Anchor set to center: " + Arrays.toString(new int[]{dimn / 2, dimn / 2}));
            return;
        }

        LinkedList<int[]> newAnchors = new LinkedList<>(anchorPositions);
        for (int[] tile : newTiles) {
            newAnchors.removeIf(anchor -> Arrays.equals(anchor, tile));
            addEmptyAdjacentAnchors(newAnchors, tile);
            LOGGER.info("Added adjacent anchors for tile: " + Arrays.toString(tile));
        }
        anchorPositions = newAnchors;
        LOGGER.info("Anchors updated: " + anchorPositions);
    }

    private void addEmptyAdjacentAnchors(LinkedList<int[]> newAnchors, int[] tile) {
        for (int[] dir : DIRECTIONS) {
            int adjacentRow = tile[0] + dir[0];
            int adjacentCol = tile[1] + dir[1];
            int[] adjacent = {adjacentRow, adjacentCol};
            if (isWithinBoardBounds(adjacent) && getTile(adjacentRow, adjacentCol).isEmpty() && !containsPosition(newAnchors, adjacent)) {
                newAnchors.add(adjacent);
            }
        }
    }

    /**
     * Checks if the given position is within the bounds of the game board.
     *
     * @param pos an array containing the row and column indices
     * @return true if the position is within the board bounds, false otherwise
     */
    public boolean isWithinBoardBounds(int[] pos) {
        return pos[0] >= 0 && pos[0] < dimn && pos[1] >= 0 && pos[1] < dimn;
    }

    /**
     * Checks if the given position is contained within the list.
     *
     * @param list  the list of positions
     * @param array the position to check
     * @return true if the position is contained within the list, false otherwise
     */
    private boolean containsPosition(LinkedList<int[]> list, int[] array) {
        return list.stream().anyMatch(a -> Arrays.equals(a, array));
    }

    /**
     * Returns a string representation of the game board.
     * Each tile is represented by its letter, with special formatting for tiles with multipliers.
     * Empty tiles are represented by "..".
     *
     * @return a string representation of the game board
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dimn; i++) {
            for (int j = 0; j < dimn; j++) {
                String letter = gameBoard[i][j].getLetter();
                sb.append(letter.length() == 2 ? letter : " " + letter).append(j < dimn - 1 ? " " : "\n");
            }
        }
        return sb.toString();
    }


    /**
     * Retrieves the tile at the specified position on the game board.
     *
     * @param x the row index
     * @param y the column index
     * @return the Tile object at the specified position
     * @throws IllegalArgumentException if the position is out of bounds
     */
    public Tile getTile(int x, int y) {
        if (!isWithinBoardBounds(x, y)) {
            throw new IllegalArgumentException("Invalid position: (" + x + ", " + y + ")");
        }
        return gameBoard[x][y];
    }

    /**
     * Retrieves the current anchor positions on the game board.
     *
     * @return a LinkedList containing the coordinates of the anchor positions
     */
    public LinkedList<int[]> getAnchorPositions() {
        return new LinkedList<>(anchorPositions);
    }

//    public List<Tile> getAdjacentTiles(int x, int y) {
//        List<Tile> adjacentTiles = new ArrayList<>();
//        for (int[] dir : DIRECTIONS) {
//            int newX = x + dir[0];
//            int newY = y + dir[1];
//            if (isValidPosition(new int[]{newX, newY})) {
//                adjacentTiles.add(getTile(newX, newY));
//            }
//        }
//        return adjacentTiles;
//    }

    public int getDimension() {
        return dimn;
    }

    /**
     * Checks if the specified position on the game board is occupied.
     *
     * @param x the row index
     * @param y the column index
     * @return true if the position is occupied, false otherwise
     * @throws IllegalArgumentException if the coordinates are out of bounds
     */


    /**
     * Checks if the specified position on the game board is occupied.
     *
     * @param x the row index
     * @param y the column index
     * @return true if the position is occupied, false otherwise
     * @throws IllegalArgumentException if the coordinates are out of bounds
     */
    public boolean isOccupied(int x, int y) {
        if (x < 0 || x >= dimn || y < 0 || y >= dimn) {
            throw new IllegalArgumentException("Invalid coordinates: (" + x + ", " + y + ")");
        }
        return !gameBoard[x][y].isEmpty();
    }

    /**
     * Retrieves the game board.
     *
     * @return a 2D array representing the game board
     */
    public Tile[][] getGameBoard() {
        return gameBoard;
    }

    /**
     * Sets the first move flag.
     *
     * @param firstMove true if it is the first move, false otherwise
     */
    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }

    /**
     * Sets the dictionary to be used for validating words.
     *
     * @param dictionary the dictionary to be used
     */
    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * Sets the word played by the human and its corresponding indexes on the board.
     * This method constructs the word based on the current move's coordinates and
     * direction (horizontal or vertical) and updates the list of tile indexes that
     * are part of the move.
     */
    public void setPlayedWordAndIndexes() {
        LOGGER.info("Setting word and indexes for human move");
        moveIndexes.clear();
        StringBuilder stringBuilder = new StringBuilder();

        int x = currentMove.getFirst()[0];
        int y = currentMove.getFirst()[1];
        int x1 = x;
        int y1 = y;

        if (movingDx) { // Horizontal move
            stringBuilder.append(getAdjacentWord(x, y, 'L'));
            y1 -= stringBuilder.length();
            stringBuilder.append(getTile(x, y).getLetter());
            stringBuilder.append(getAdjacentWord(x, y, 'R'));
            playedWord = stringBuilder.toString();
            LOGGER.info("Word played horizontally: " + playedWord);
            for (int i = 0; i < stringBuilder.length(); i++) {
                moveIndexes.add(new int[]{x1, y1 + i});
            }
        } else { // Vertical move
            stringBuilder.append(getAdjacentWord(x, y, 'U'));
            x1 -= stringBuilder.length();
            stringBuilder.append(getTile(x, y).getLetter());
            stringBuilder.append(getAdjacentWord(x, y, 'D'));
            playedWord = stringBuilder.toString();
            LOGGER.info("Word played vertically: " + playedWord);
            for (int i = 0; i < stringBuilder.length(); i++) {
                moveIndexes.add(new int[]{x1 + i, y1});
            }
        }
    }


    /**
     * Sets the ScoreMap object for the current game.
     *
     * @param scoreMap the ScoreMap to set
     */
    public void setScoreMap(ScoreMap scoreMap) {
        this.scoreMap = scoreMap;
    }

    /**
     * Retrieves the word that has been played on the board.
     * Calls the method to update the word and its corresponding indexes before returning the word.
     *
     * @return the played word
     */
    public String getPlayedWord() {
        setPlayedWordAndIndexes();
        return playedWord;
    }

    /**
     * Sets the word that has been played on the board.
     *
     * @param playedWord the word to set
     */
    public void setPlayedWord(String playedWord) {
        this.playedWord = playedWord;
    }

    /**
     * Retrieves the indexes of the tiles that were part of the current move.
     *
     * @return a LinkedList of integer arrays representing the move indexes
     */
    public LinkedList<int[]> getMoveIndexes() {
        return moveIndexes;
    }

    /**
     * Checks if a new character has been selected by the player.
     *
     * @return true if a new character is selected, false otherwise
     */
    public boolean isNewCharSelected() {
        return newCharSelected;
    }

    /**
     * Sets the state of whether a new character is selected by the player.
     *
     * @param newCharSelected the new state to set
     */
    public void setNewCharSelected(boolean newCharSelected) {
        this.newCharSelected = newCharSelected;
    }

    /**
     * Retrieves the list of indexes for the current move.
     *
     * @return a LinkedList of integer arrays representing the current move's indexes
     */
    public LinkedList<int[]> getCurrentMove() {
        return currentMove;
    }

    /**
     * Checks if the specified tile at (x, y) has any adjacent non-empty tiles.
     * This is used to ensure that moves are placed adjacent to existing words.
     *
     * @param x the x-coordinate of the tile
     * @param y the y-coordinate of the tile
     * @return true if there is an adjacent tile, false otherwise
     */
    public boolean hasAdjacentTile(int x, int y) {
        for (int[] dir : DIRECTIONS) {
            int newX = x + dir[0];
            int newY = y + dir[1];

            // Ensure the tile is within the board bounds and is not empty
            if (isWithinBoardBounds(newX, newY) && !gameBoard[newX][newY].isEmpty()) {
                LOGGER.info("Adjacent play found at: (" + x + ", " + y + ")");
                return true;
            }
        }
        LOGGER.info("No adjacent play found at: (" + x + ", " + y + ")");
        return false;
    }

    /**
     * Checks if the specified coordinates are within the bounds of the board.
     *
     * @param x the x-coordinate to check
     * @param y the y-coordinate to check
     * @return true if the coordinates are within bounds, false otherwise
     */
    private boolean isWithinBoardBounds(int x, int y) {
        return x >= 0 && x < dimn && y >= 0 && y < dimn;
    }

    /**
     * Validates the current human move.
     * Ensures that the move is valid based on the rules of Scrabble, including that the tiles are placed
     * in a straight line, and that the move forms valid words and is adjacent to existing tiles if not the first move.
     *
     * @return true if the move is valid, false otherwise
     */
    public boolean isValidHumanMove() {
        if (currentMove.isEmpty() || currentPlayingChar == (char) 0) {
            return false;
        }

        int[] lastMove = currentMove.getLast();
        int row = lastMove[0];
        int col = lastMove[1];

        // For the first tile, any valid position is acceptable
        if (currentMove.size() == 1) {
            return true;
        }

        // For the second tile, check if it defines a valid orientation
        if (currentMove.size() == 2) {
            int[] firstMove = currentMove.getFirst();
            boolean isValidOrientation = (firstMove[0] == row || firstMove[1] == col);

            if (!isValidOrientation) {
                LOGGER.warning("Invalid tile placement: Must be in same row or column as first tile");
                return false;
            }
        }

        // For subsequent tiles, ensure they follow the established orientation
        if (currentMove.size() > 2) {
            if (movingDx && row != currentMove.getFirst()[0]) {
                LOGGER.warning("Invalid tile placement: Must maintain horizontal orientation");
                return false;
            }
            if (!movingDx && col != currentMove.getFirst()[1]) {
                LOGGER.warning("Invalid tile placement: Must maintain vertical orientation");
                return false;
            }
        }

        // Check adjacency for non-first moves
        if (!firstMove) {
            if (currentMove.size() == 1 && !hasAdjacentTile(row, col)) {
                return false;
            }
            return isValidDxWord(row, col, currentPlayingChar);
        }

        return true;
    }

    /**
     * Checks if all the moves in the current move are in the same row.
     *
     * @param row the row to check
     * @return true if all moves are in the same row, false otherwise
     */
    private boolean areMovesAlignedInRow(int row) {
        return currentMove.stream().allMatch(move -> move[0] == row);
    }

    /**
     * Checks if all the moves in the current move are in the same column.
     * Rotates the board to simplify checking column alignment.
     *
     * @param col the column to check
     * @return true if all moves are in the same column, false otherwise
     */
    private boolean areMovesInSameColumn(int col) {
        rotateBoardCCW();
        boolean result = currentMove.stream().allMatch(move -> (dimn - move[1] - 1) == (dimn - col - 1));
        rotateBoardClockwise();
        return result;
    }

    /**
     * Retrieves the character that the player is currently playing.
     *
     * @return the current playing character
     */
    public char getCurrentPlayingChar() {
        return currentPlayingChar;
    }

    /**
     * Sets the character that the player is currently playing.
     *
     * @param currentPlayingChar the character to set
     */
    public void setCurrentPlayingChar(char currentPlayingChar) {
        this.currentPlayingChar = currentPlayingChar;
    }

    /**
     * Places a tile with the given letter at the specified position on the board.
     * This method handles different types of tiles, such as empty tiles and multiplier tiles,
     * and throws an exception if the placement is invalid or the tile is non-empty.
     *
     * @param letter the letter to place on the tile
     * @param x      the x-coordinate of the tile
     * @param y      the y-coordinate of the tile
     * @throws IllegalArgumentException if the position is outside the board bounds
     * @throws IllegalStateException    if attempting to place a letter on a non-empty tile
     */
    public void placeTile(char letter, int x, int y) {
        LOGGER.info("Attempting to place character '" + letter + "' at position (" + x + ", " + y + ")");

        // Ensure the tile position is within the board bounds
        if (!isWithinBoardBounds(new int[]{x, y})) {
            throw new IllegalArgumentException("Invalid position: (" + x + ", " + y + ")");
        }

        Tile currentTile = gameBoard[x][y];
        String currentLetter = currentTile.getLetter();
        String newLetter = String.valueOf(letter);  // Convert char to String

        // Case 1: Empty tile (placeholder "..")
        if ("..".equals(currentLetter)) {
            gameBoard[x][y] = new Tile(x, y, newLetter, currentTile.getPointValue());
            LOGGER.info("Placed character '" + letter + "' on an empty tile at (" + x + ", " + y + ")");
            return;
        }

        // Case 2: Multiplier tile (e.g., "2." or "3L")
        if (currentLetter.length() == 2 && currentLetter.contains(".")) {
            int multiplier = Character.getNumericValue(currentLetter.charAt(0));
            char multiplierType = currentLetter.charAt(1) == '.' ? 'W' : 'L';
            gameBoard[x][y] = new Tile(x, y, newLetter, currentTile.getPointValue(), multiplier, multiplierType);
            LOGGER.info("Placed character '" + letter + "' on a multiplier tile at (" + x + ", " + y + ")");
            return;
        }

        // Case 3: Non-empty tile or illegal placement
        throw new IllegalStateException("Cannot place letter on non-empty tile at (" + x + ", " + y + ")");
    }

    /**
     * Removes a tile at the specified position on the board.
     * Handles removing letters from regular tiles or multiplier tiles, and restores placeholders.
     *
     * @param x the x-coordinate of the tile
     * @param y the y-coordinate of the tile
     * @throws IllegalArgumentException if the position is outside the board bounds
     */
    public void removeTile(int x, int y) {
        LOGGER.info("Attempting to remove character at position (" + x + ", " + y + ")");

        // Ensure the tile position is within the board bounds
        if (!isWithinBoardBounds(new int[]{x, y})) {
            throw new IllegalArgumentException("Invalid position: (" + x + ", " + y + ")");
        }

        Tile currentTile = gameBoard[x][y];
        String currentLetter = currentTile.getLetter();

        // Early return if the tile is already empty (placeholder)
        if ("..".equals(currentLetter)) {
            LOGGER.info("Tile at (" + x + ", " + y + ") is already empty.");
            return;
        }

        // Handle single character tiles (regular letters)
        if (currentLetter.length() == 1 && Character.isLetter(currentLetter.charAt(0))) {
            gameBoard[x][y] = new Tile(x, y, "..", 0);
            LOGGER.info("Removed character '" + currentLetter + "' from position (" + x + ", " + y + ")");
            return;
        }

        // Handle multiplier tiles (two-character tiles, like "2." or ".L")
        if (currentLetter.length() == 2) {
            char firstChar = currentLetter.charAt(0);
            char secondChar = currentLetter.charAt(1);

            // Replace the letter part of the tile with a placeholder
            if (Character.isLetter(firstChar)) {
                gameBoard[x][y] = new Tile(x, y, "." + secondChar, 0);
                LOGGER.info("Removed character '" + firstChar + "' from multiplier tile at position (" + x + ", " + y + ")");
            } else if (Character.isLetter(secondChar)) {
                gameBoard[x][y] = new Tile(x, y, firstChar + ".", 0);
                LOGGER.info("Removed character '" + secondChar + "' from multiplier tile at position (" + x + ", " + y + ")");
            }
        }
    }

    /**
     * Checks if a human is currently playing.
     *
     * @return true if a human is playing, false otherwise
     */
    public boolean isHumanPlaying() {
        return humanPlaying;
    }

    /**
     * Sets whether a human is currently playing.
     *
     * @param humanPlaying true if a human is playing, false otherwise
     */
    public void setHumanPlaying(boolean humanPlaying) {
        this.humanPlaying = humanPlaying;
    }

    /**
     * Checks if the current move is in the horizontal direction.
     *
     * @return true if the move is horizontal, false otherwise
     */
    public boolean isMovingDx() {
        return movingDx;
    }

    /**
     * Sets the direction of the current move.
     *
     * @param movingDx true if the move is horizontal, false if vertical
     */
    public void setMovingDx(boolean movingDx) {
        this.movingDx = movingDx;
    }

    /**
     * Rotates the game board 90 degrees counterclockwise.
     */
    public void rotateBoardCCW() {
        Tile[][] temp = new Tile[dimn][dimn];
        for (int i = 0; i < dimn; i++) {
            for (int j = 0; j < dimn; j++) {
                temp[dimn - j - 1][i] = gameBoard[i][j];
            }
        }
        this.gameBoard = temp;
        ro = true;
    }


    /**
     * Retrieves the letter of the tile at the specified position on the game board.
     * Handles different types of tiles, including empty tiles and multiplier tiles.
     *
     * @param x the x-coordinate of the tile
     * @param y the y-coordinate of the tile
     * @return the letter of the tile, or '\0' if the tile is empty or no letter is found
     * @throws IllegalArgumentException if the position is outside the board bounds
     */
    public char getTileLetter(int x, int y) {

        if (!isWithinBoardBounds(new int[]{x, y})) {
            LOGGER.severe("Invalid position: (" + x + ", " + y + ")");
            throw new IllegalArgumentException("Invalid position: (" + x + ", " + y + ")");
        }

        Tile tile = gameBoard[x][y];
        String letter = tile.getLetter();

        // Return early if the tile is empty
        if (letter.equals("..")) {
            return '\0';
        }

        // If it's a single character, return it
        if (letter.length() == 1) {
            return letter.charAt(0);
        }

        // Handle multiplier tiles with two characters
        char firstChar = letter.charAt(0);
        char secondChar = letter.charAt(1);

        if (Character.isLetter(firstChar)) {
            return firstChar;
        } else if (Character.isLetter(secondChar)) {
            return secondChar;
        }

        // No letter found, return null character
        LOGGER.info("No letter found at (" + x + ", " + y + "), returning null character.");
        return '\0';
    }


    /**
     * Checks if there is already an existing letter in the given index.
     * This method verifies if the specified position on the game board contains a letter.
     * It handles both single-character tiles and two-character multiplier tiles.
     *
     * @param x the row index
     * @param y the column index
     * @return true if there is an existing letter, false otherwise
     */
    public boolean hasExistingLetter(int x, int y) {
        if (x < 0 || x >= dimn || y < 0 || y >= dimn) {
            return false; // Out of bounds, treat as if no character exists
        }

        String letter = gameBoard[x][y].getLetter();
        if (letter.length() == 1) {
            char c = letter.charAt(0);
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
        } else if (letter.length() == 2) {
            char c1 = letter.charAt(0);
            char c2 = letter.charAt(1);
            return ((c1 >= 'a' && c1 <= 'z') || (c1 >= 'A' && c1 <= 'Z')) || ((c2 >= 'a' && c2 <= 'z') || (c2 >= 'A' && c2 <= 'Z'));
        }
        return false;
    }


    /**
     * Checks if the words being made vertically are valid words for a horizontal move.
     * This method ensures that each letter in the given word forms a valid horizontal word
     * when placed at the specified end coordinates.
     *
     * @param word the word to be checked
     * @param endX row index of the last letter of the word
     * @param endY column index of the last letter of the word
     * @return true if the words being made vertically are valid words, false otherwise
     */
    public boolean isValidDyWord(String word, int endX, int endY) {
        for (int i = 0; i < word.length(); i++) {
            // Check each letter in the word to ensure vertical moves are legal
            if (!isValidDxWord(endX, endY - i, word.charAt(word.length() - 1 - i))) {
                return false;
            }
        }
        return true;
    }


    /**
     * For a letter to be played, checks if the move creates a legal horizontal word.
     *
     * @param i      the row index of the tile
     * @param j      the column index of the tile
     * @param letter the letter to be played
     * @return true if the move creates a legal horizontal word, false otherwise
     */
    public boolean isValidDxWord(int i, int j, char letter) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean hasAbove = i - 1 >= 0 && hasExistingLetter(i - 1, j);
        boolean hasBelow = i + 1 < dimn && hasExistingLetter(i + 1, j);
        // Check if there are letters both above and below the current position
        if (hasAbove || hasBelow) {
            if (hasAbove) {
                stringBuilder.append(getAdjacentWord(i, j, 'U'));
            }
            stringBuilder.append(letter);
            if (hasBelow) {
                stringBuilder.append(getAdjacentWord(i, j, 'D'));
            }
            if (ro) {
                stringBuilder.reverse();
            }
            // Check if the word exists in the dictionary
            return dictionary.isWordInDictionary(stringBuilder.toString().toLowerCase());
        }
        return true;
    }


    /**
     * Rotate the board back to its original position
     */
    public void rotateBoardClockwise() {
        Tile[][] temp = new Tile[dimn][dimn];
        for (int i = 0; i < dimn; i++) {
            for (int j = 0; j < dimn; j++) {
                temp[j][dimn - i - 1] = gameBoard[i][j];
            }
        }
        this.gameBoard = temp;
        ro = false;
    }

    /**
     * For a word and index of its last letter finds the indexes of the whole word
     *
     * @param x         row index of the last letter
     * @param y         column index of the last letter
     * @param isRotated if the board was rotated at the time of choosing the word
     * @param word      the word
     * @return the indexes of the whole word
     */
    public LinkedList<int[]> calculateWordCoordinates(int x, int y, boolean isRotated, String word) {
        LinkedList<int[]> list = new LinkedList<>();
        for (int i = 0; i < word.length(); i++) {
            if (isRotated) {
                int[] arr = rotateCoordinateCW(new int[]{x, y - i});
                list.addFirst(arr);
            } else {
                list.addFirst(new int[]{x, y - i});
            }
        }
        return list;
    }

    /**
     * Convert an index on the rotated board to index on the original board
     *
     * @param arr index x, y array
     * @return new index
     */
    public int[] rotateCoordinateCW(int[] arr) {
        int x = arr[1];
        int y = dimn - arr[0] - 1;
        return new int[]{x, y};
    }

    /**
     * Updates the board by putting a word
     *
     * @param word        the word to be put
     * @param coordinates the indexes of the word
     */
    public void updateBoard(String word, LinkedList<int[]> coordinates) {
        int putCount = 0;
        for (int[] arr : coordinates) {
            if (!hasExistingLetter(arr[0], arr[1])) {
                placeTile(word.charAt(putCount), arr[0], arr[1]);
                putCount++;
            } else {
                putCount++;
            }
        }
    }


    /**
     * Sets the anchor points on the game board.
     * This method initializes all points as non-anchors and then iterates through the board
     * to identify positions adjacent to existing letters. These positions are marked as anchor points.
     */
    public void updateAnchorPoints() {
        // Initialize all points as non-anchors
        for (String[] row : anchorPoints) {
            Arrays.fill(row, "*");
        }

        for (int i = 0; i < dimn; i++) {
            for (int j = 0; j < dimn; j++) {
                if (hasExistingLetter(i, j)) {
                    for (int[] dir : DIRECTIONS) {
                        int newI = i + dir[0];
                        int newJ = j + dir[1];
                        if (isWithinBoardBounds(new int[]{newI, newJ}) && !hasExistingLetter(newI, newJ)) {
                            anchorPoints[newI][newJ] = "A";
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the board is currently rotated.
     *
     * @return true if the board is rotated, false otherwise
     */
    public boolean isRo() {
        return ro;
    }

    /**
     * Finds the number of empty squares to the left of a given square
     *
     * @param x row index of a given square
     * @param y column index of a given square
     * @return the number of empty squares
     */
    public int countEmptySqauresLeft(int x, int y) {
        int count = 0;
        if (y - 1 >= 0 && !hasExistingLetter(x, y - 1)) {
            while (y - 1 >= 0 && !hasExistingLetter(x, y - 1)) {
                count++;
                y--;
            }
            if (y - 1 >= 0 && hasExistingLetter(x, y - 1)) {
                count--;
            }
        }
        return count;
    }

    public String[][] getAnchorPoints() {
        return anchorPoints;
    }


    /**
     * Retrieves the adjacent word in the specified direction from the given coordinates.
     *
     * @param x   the row index of the starting tile
     * @param y   the column index of the starting tile
     * @param dir the direction to search for the adjacent word ('R' for right, 'L' for left, 'U' for up, 'D' for down)
     * @return the adjacent word found in the specified direction
     * @throws IllegalArgumentException if the direction is invalid
     */
    public String getAdjacentWord(int x, int y, char dir) {
        int k1 = 0;
        int k2 = 0;
        switch (dir) {
            case 'R':
                k2 = 1;
                break;
            case 'L':
                k2 = -1;
                break;
            case 'U':
                k1 = -1;
                break;
            case 'D':
                k1 = 1;
                break;
            default:
                throw new IllegalArgumentException("Invalid direction: " + dir);
        }

        int i = x + k1;
        int j = y + k2;
        StringBuilder str = new StringBuilder();

        // Traverse in the specified direction until an empty tile or board boundary is reached
        while (i >= 0 && i < dimn && j >= 0 && j < dimn && !getTile(i, j).isEmpty()) {
            str.append(getTile(i, j).getLetter());
            i += k1;
            j += k2;
        }

        // Reverse the string if the direction is left or up
        if (k1 < 0 || k2 < 0) {
            str.reverse();
        }

        return str.toString();
    }

    /**
     * Get score of a letter on a given square, considering multipliers
     *
     * @param c the letter
     * @param x row index
     * @param y column index
     * @return the score
     */
    private int calculateLetterScore(char c, int x, int y) {
        // Convert uppercase letters to lowercase
        char letter = Character.toLowerCase(c);

        // Get base score from scoreMap
        int score = this.scoreMap.getScore(String.valueOf(letter));

        Tile tile = getTile(x, y);

        // Apply letter multiplier if applicable
        if (tile.hasMulti() && tile.getMultiType() == 'L') {
            score *= tile.getMultiplier();
        }

        return score;
    }

    /**
     * Calculates the score of a word without considering the across words.
     * This method takes into account letter multipliers and word multipliers
     * on the game board to compute the total score for the given word.
     *
     * @param coordinates the indexes of the word on the board
     * @param word        the word to calculate the score for
     * @return the total score of the word
     */
    public int calculateWordScore(LinkedList<int[]> coordinates, String word) {
        int score = 0;
        int wordMultiplier = 1;
        boolean wasRotated = ro;

        // Rotate the board back to its original position if it was rotated
        if (ro) {
            rotateBoardClockwise();
        }

        try {
            for (int i = 0; i < word.length(); i++) {
                char letter = word.charAt(i);
                int[] position = coordinates.get(i);
                int x = position[0], y = position[1];

                // Calculate the score for the current letter
                score += calculateLetterScore(letter, x, y);

                Tile tile = getTile(x, y);
                // Apply word multiplier if applicable
                if (tile.isEmpty() && tile.hasMulti() && tile.getMultiType() == 'W') {
                    wordMultiplier *= tile.getMultiplier();
                }
            }

            // Apply the word multiplier to the total score
            score *= wordMultiplier;
        } finally {
            // Rotate the board back to its rotated position if it was rotated
            if (wasRotated) {
                rotateBoardCCW();
            }
        }

        return score;
    }

    /**
     * Calculate a word score considering the across words.
     * This method calculates the total score for a given word, including any new words formed
     * by placing the tiles on the board. It also considers any multipliers and bonuses.
     *
     * @param coordinates  indexes of the word on the board
     * @param word         the word to calculate the score for
     * @param allTilesUsed if all the tiles have been used to make the move
     * @return the total score
     */
    public int calculateTotalScore(LinkedList<int[]> coordinates, String word, boolean allTilesUsed) {
        int score = 0;
        List<LinkedList<int[]>> allWordCoordinates = new ArrayList<>();
        List<String> allWords = new ArrayList<>();

        // Add the main word and its coordinates to the lists
        allWordCoordinates.add(coordinates);
        allWords.add(word);

        boolean wasRotated = ro;
        if (ro) {
            rotateBoardClockwise();
        }

        try {
            // Determine the direction of the main word
            char direction = determineDirection(coordinates);

            // Check for any new words formed by placing the tiles
            for (int i = 0; i < coordinates.size(); i++) {
                int[] position = coordinates.get(i);
                int x = position[0], y = position[1];
                char letter = word.charAt(i);

                if (!hasExistingLetter(x, y)) {
                    String newWord = constructNewWord(x, y, letter, direction);
                    if (newWord.length() > 1) {
                        allWordCoordinates.add(calculateWordCordinates(x, y, newWord, direction));
                        allWords.add(newWord);
                    }
                }
            }

            // Calculate the score for all words
            for (int i = 0; i < allWords.size(); i++) {
                score += calculateWordScore(allWordCoordinates.get(i), allWords.get(i));
            }

            // Add bonus for using all tiles
            if (allTilesUsed) {
                score += 50; // Bonus for using all tiles
            }

            return score;
        } finally {
            // Rotate the board back to its rotated position if it was rotated
            if (wasRotated) {
                rotateBoardCCW();
            }
        }
    }

    /**
     * Determines the direction of the word based on the coordinates.
     *
     * @param coordinates the list of coordinates of the word
     * @return 'H' if the word is horizontal, 'V' if the word is vertical
     */
    private char determineDirection(LinkedList<int[]> coordinates) {
        return coordinates.getFirst()[0] == coordinates.get(1)[0] ? 'H' : 'V';
    }

    /**
     * Constructs a new word by adding a letter at the specified position.
     *
     * @param x         the row index of the tile
     * @param y         the column index of the tile
     * @param newLetter the new letter to be added
     * @param direction the direction of the word ('H' for horizontal, 'V' for vertical)
     * @return the constructed word
     */
    private String constructNewWord(int x, int y, char newLetter, char direction) {
        StringBuilder word = new StringBuilder();
        if (direction == 'H') {
            // Append letters above and below the new letter
            word.append(getAdjacentWord(x, y, 'U')).append(newLetter).append(getAdjacentWord(x, y, 'D'));
        } else {
            // Append letters to the left and right of the new letter
            word.append(getAdjacentWord(x, y, 'L')).append(newLetter).append(getAdjacentWord(x, y, 'R'));
        }
        return word.toString();
    }

    /**
     * Calculates the coordinates of a word on the board.
     *
     * @param x         the row index of the starting tile
     * @param y         the column index of the starting tile
     * @param newWord   the word to be placed
     * @param direction the direction of the word ('H' for horizontal, 'V' for vertical)
     * @return a list of coordinates for the word
     */
    private LinkedList<int[]> calculateWordCordinates(int x, int y, String newWord, char direction) {
        LinkedList<int[]> index = new LinkedList<>();
        int newX = x, newY = y;
        if (direction == 'H') {
            // Adjust starting position for horizontal word
            newX -= getAdjacentWord(x, y, 'U').length();
        } else {
            // Adjust starting position for vertical word
            newY -= getAdjacentWord(x, y, 'L').length();
        }
        for (int i = 0; i < newWord.length(); i++) {
            // Add coordinates for each letter in the word
            index.add(new int[]{direction == 'H' ? newX + i : newX, direction == 'H' ? newY : newY + i});
        }
        return index;
    }

    public void determineOrientation(int newRow, int newCol) {
        if (!getCurrentMove().isEmpty() && getCurrentMove().size() == 1) {
            int[] firstMove = getCurrentMove().getFirst();
            // If same row, it's horizontal. If same column, it's vertical.
            setMovingDx(firstMove[0] == newRow);
            LOGGER.info("Move orientation determined: " + (isMovingDx() ? "Horizontal" : "Vertical"));
        }
    }


}
