package scrabble;

import java.io.*;
import java.util.*;

/**
 * ScoreChecker class for validating and scoring Scrabble plays.
 * This class reads game boards, compares them, and calculates scores for valid plays.
 */
public class ScoreChecker {
    private static ScoreChecker scoreChecker;
    private static ScoreMap scoreMap;
    private static Dictionary dict;
    private GameBoard originalGameBoard, resultGameBoard;
    LinkedList<int[]> coordinatesToScore = new LinkedList<>();
    List<Integer> individualScore = new LinkedList<>();

    /**
     * Constructs a ScoreChecker object by reading and processing input from a BufferedReader.
     *
     * @param br BufferedReader containing the input data
     * @throws IOException if there's an error reading from the BufferedReader
     */
    public ScoreChecker(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();

            if (line.isEmpty()) {
                if (originalGameBoard != null && resultGameBoard != null) {
                    getScore(originalGameBoard, resultGameBoard);
                    originalGameBoard = null;
                    resultGameBoard = null;
                    coordinatesToScore.clear();
                }
                continue;
            }

            if (line.length() < 3) {
                GameBoard newGameBoard = createBoard(br, Integer.parseInt(line));
                if (originalGameBoard == null) {
                    originalGameBoard = newGameBoard;
                } else {
                    resultGameBoard = newGameBoard;
                }
            }
        }
    }

    /**
     * Creates a GameBoard object from the input data.
     *
     * @param br BufferedReader containing the board data
     * @param dimn Dimension of the game board
     * @return A new GameBoard object
     * @throws IOException if there's an error reading from the BufferedReader
     */
    private GameBoard createBoard(BufferedReader br, int dimn) throws IOException {
        GameBoard gameBoard = new GameBoard(dimn);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < dimn; i++) {
            String boardLine = br.readLine();
            if (boardLine == null) {
                throw new IOException("Unexpected end of input while reading board");
            }
            sb.append(boardLine).append(' ');
        }

        gameBoard.configBoard(sb.toString(), scoreMap);
        return gameBoard;
    }

    /**
     * Calculates and prints the score for a play by comparing two game boards.
     *
     * @param originalGameBoard The original game board before the play
     * @param resultGameBoard The game board after the play
     */
    public void getScore(GameBoard originalGameBoard, GameBoard resultGameBoard) {
        // Print original and result boards
        System.out.printf("original board:\n%s", originalGameBoard.toString());
        System.out.printf("result board:\n%s", resultGameBoard.toString());

        int dim = originalGameBoard.dimn;
        LinkedList<int[]> diffs = new LinkedList<>();

        // Find differences between boards
        for (int row = 0; row < dim; row++) {
            for (int col = 0; col < dim; col++) {
                if (!originalGameBoard.getTile(row,col).getLetter().equals(resultGameBoard.getTile(row,col).getLetter())) {
                    diffs.add(new int[]{row,col});
                }
            }
        }

        // Check if play is empty
        if (diffs.isEmpty()) {
            System.out.printf("play is empty\nplay is not legal\n\n");
            return;
        }

        // Check board compatibility
        if (!boardsAreCompatible(originalGameBoard, resultGameBoard, diffs)) {
            return;
        }

        // Construct play description and word
        StringBuilder sb = new StringBuilder("play is");
        StringBuilder substring = new StringBuilder();
        for (int[] a : diffs) {
            String letter = resultGameBoard.getTile(a[0], a[1]).getLetter();
            sb.append(String.format(" %s at (%d, %d),", letter, a[0], a[1]));
            substring.append(letter);
        }
        String play = substring.toString();
        String str = sb.toString().trim();
        str = str.substring(0, str.length() - 1);
        System.out.println(str);

        // Check if play is valid
        if (!validPlay(originalGameBoard.getAnchorPositions(), diffs)) {
            System.out.printf("play is not legal\n\n");
            return;
        }

        // Handle legal play
        handleLegalPlay(resultGameBoard, diffs);
    }

    /**
     * Handles a legal play by determining the play direction, constructing the word,
     * checking adjacent words, and calculating the score.
     *
     * @param resultGameBoard The game board after the play
     * @param diffs The differences between the original and result boards
     */
    public void handleLegalPlay(GameBoard resultGameBoard, LinkedList<int[]> diffs) {
        char playDirection = determinePlayDirection(diffs);
        int[] first = diffs.getFirst();
        int[] last = diffs.getLast();

        String playPrefix = getPrefix(resultGameBoard, first, playDirection);
        String playSuffix = getSuffix(resultGameBoard, last, playDirection);

        String play = constructWord(originalGameBoard, resultGameBoard, first, last, playPrefix, playSuffix, playDirection);
        LinkedList<String> adjacentWords = getAdjacentWords(resultGameBoard, diffs, playDirection);

        if (!isPlayLegal(play, adjacentWords)) {
            System.out.println("play is not legal\n");
            return;
        }

        int score = calculateScore(originalGameBoard, resultGameBoard, coordinatesToScore, play.length(), adjacentWords);
        System.out.printf("play is legal\nscore is %d\n\n", score);
    }

    /**
     * Determines the direction of play (Horizontal, Vertical, or Across).
     *
     * @param diffs The differences between the original and result boards
     * @return 'H' for horizontal, 'V' for vertical, or 'A' for across (single letter)
     */
    private char determinePlayDirection(LinkedList<int[]> diffs) {
        if (diffs.size() < 2) {
            return 'A'; // Single letter play
        }
        return (diffs.get(0)[0] == diffs.get(1)[0]) ? 'H' : 'V';
    }

    /**
     * Checks if the main word and all adjacent words are legal according to the dictionary.
     *
     * @param mainWord The main word played
     * @param adjacentWords List of adjacent words formed by the play
     * @return true if all words are legal, false otherwise
     */
    private boolean isPlayLegal(String mainWord, LinkedList<String> adjacentWords) {
        if (!dict.isWordInDictionary(mainWord)) {
            return false;
        }
        for (String word : adjacentWords) {
            if (!dict.isWordInDictionary(word)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the total score for a play, including the main word and adjacent words.
     *
     * @param originalGameBoard The original game board before the play
     * @param resultboard The game board after the play
     * @param score LinkedList of coordinates to score
     * @param playLength Length of the main play
     * @param adjWords List of adjacent words formed by the play
     * @return The total score for the play
     */
    public int calculateScore(GameBoard originalGameBoard, GameBoard resultboard, LinkedList<int[]> score, int playLength, LinkedList<String> adjWords) {
        ArrayList<Integer> letterScores = new ArrayList<>();
        ArrayList<Integer> wordMultipliers = new ArrayList<>();
        ArrayList<Integer> wordScores = new ArrayList<>();

        int processedLetters = 0;
        int currentWordLength = playLength;

        while (!score.isEmpty()) {
            int[] tileCordinates = score.removeFirst();
            Tile originalTile = originalGameBoard.getTile(tileCordinates[0], tileCordinates[1]);
            Tile newTile = resultboard.getTile(tileCordinates[0], tileCordinates[1]);

            processTileScore(originalTile, newTile, letterScores, wordMultipliers);

            if (newTile.hasAdjacentTile()) {
                newTile.setHasAdjacentTile(false);
                score.addLast(tileCordinates);
            }

            processedLetters++;
            if (processedLetters == currentWordLength) {
                addWordScore(letterScores, wordMultipliers, wordScores);
                resetForNextWord(letterScores, wordMultipliers);

                processedLetters = 0;
                if (!adjWords.isEmpty()) {
                    currentWordLength = adjWords.removeFirst().length();
                }
            }
        }

        return sumWordScores(wordScores);
    }

    /**
     * Processes the score for a single tile, considering letter and word multipliers.
     *
     * @param originalTile The tile from the original board
     * @param newTile The tile from the result board
     * @param letterScores List to store letter scores
     * @param wordMultipliers List to store word multipliers
     */
    private void processTileScore(Tile originalTile, Tile newTile, ArrayList<Integer> letterScores, ArrayList<Integer> wordMultipliers) {
        int tileScore = newTile.getPointValue();

        if (originalTile.hasMulti()) {
            switch (originalTile.getMultiType()) {
                case 'L' -> letterScores.add(tileScore * originalTile.getMultiplier());
                case 'W' -> {
                    letterScores.add(tileScore);
                    wordMultipliers.add(originalTile.getMultiplier());
                }
            }
        } else {
            letterScores.add(tileScore);
        }
    }

    /**
     * Adds the score for a complete word to the wordScores list.
     *
     * @param letterScores List of letter scores for the word
     * @param wordmultipliers List of word multipliers
     * @param wordScores List to store the final word scores
     */
    private void addWordScore(ArrayList<Integer> letterScores, ArrayList<Integer> wordmultipliers, ArrayList<Integer> wordScores) {
        int wordScore = letterScores.stream().mapToInt(Integer::intValue).sum();
        int wordMultiplier = Math.max(1, wordmultipliers.stream().mapToInt(Integer::intValue).sum());
        wordScores.add(wordScore * wordMultiplier);
    }

    /**
     * Resets the letterScores and wordMultipliers lists for the next word.
     *
     * @param letterScores List of letter scores to reset
     * @param wordMultipliers List of word multipliers to reset
     */
    private void resetForNextWord(ArrayList<Integer> letterScores, ArrayList<Integer> wordMultipliers) {
        letterScores.clear();
        wordMultipliers.clear();
    }

    /**
     * Sums up all the word scores to get the total score for the play.
     *
     * @param wordScores List of individual word scores
     * @return The total score for the play
     */
    private int sumWordScores(ArrayList<Integer> wordScores) {
        return wordScores.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Constructs the complete word played, including prefix and suffix.
     *
     * @param originalGameBoard The original game board
     * @param resultGameBoard The result game board
     * @param first Coordinates of the first letter of the play
     * @param last Coordinates of the last letter of the play
     * @param prefix Prefix of the word (if any)
     * @param suffix Suffix of the word (if any)
     * @param playDirection Direction of the play ('H' for horizontal, 'V' for vertical)
     * @return The complete word played
     */
    public String constructWord(GameBoard originalGameBoard, GameBoard resultGameBoard, int[] first, int[] last, String prefix, String suffix, char playDirection) {
        StringBuilder mid = new StringBuilder();
        int row = first[0];
        int col = first[1];
        int endPosition = (playDirection == 'H') ? last[1] : last[0];

        while ((playDirection == 'H' ? col : row) <= endPosition) {
            mid.append(resultGameBoard.getTile(row, col).getLetter());
            coordinatesToScore.add(new int[]{row, col});
            if (playDirection == 'H') col++; else row++;
        }

        return prefix + mid.toString() + suffix;
    }

    /**
     * Finds all adjacent words formed by the play.
     *
     * @param resultGameBoard The result game board
     * @param diffs The differences between the original and result boards
     * @param playDirection Direction of the play ('H' for horizontal, 'V' for vertical)
     * @return A list of adjacent words formed by the play
     */
    public LinkedList<String> getAdjacentWords(GameBoard resultGameBoard, LinkedList<int[]> diffs, char playDirection) {
        LinkedList<String> adjacentWords = new LinkedList<>();
        char perpendicularDirection = (playDirection == 'H') ? 'V' : 'H';

        for (int[] d : diffs) {
            String prefix = getPrefix(resultGameBoard, d, perpendicularDirection);
            String suffix = getSuffix(resultGameBoard, d, perpendicularDirection);

            if (!prefix.isEmpty() || !suffix.isEmpty()) {
                StringBuilder word = new StringBuilder();
                if (!prefix.isEmpty()) {
                    word.append(prefix);
                }
                word.append(resultGameBoard.getTile(d[0], d[1]).getLetter());
                if (!suffix.isEmpty()) {
                    word.append(suffix);
                }

                adjacentWords.add(word.toString());
                resultGameBoard.getTile(d[0], d[1]).setHasAdjacentTile(true);
            }
        }

        return adjacentWords;
    }

    /**
     * Gets the suffix of a word in the specified direction.
     *
     * @param resultGameBoard The result game board
     * @param last Coordinates of the last letter
     * @param playDirection Direction to check for the suffix ('H' for horizontal, 'V' for vertical)
     * @return The suffix of the word
     */
    public String getSuffix(GameBoard resultGameBoard, int[] last, char playDirection) {
        StringBuilder suffix = new StringBuilder();
        int row = last[0], col = last[1];
        int step = (playDirection == 'H') ? col + 1 : row + 1;
        int maxDimension = resultGameBoard.getDimension() - 1;

        // Check if we're at the edge of the board
        if ((playDirection == 'H' && col == maxDimension) ||
                (playDirection == 'V' && row == maxDimension)) {
            return suffix.toString();
        }

        while (step <= maxDimension) {
            // Get the next tile in the direction of play
            Tile currentTile = (playDirection == 'H') ? resultGameBoard.getTile(row, step) : resultGameBoard.getTile(step, col);

            if (currentTile.isEmpty()) {
                break;
            }

            suffix.append(currentTile.getLetter());
            // Add the coordinates to be scored
            coordinatesToScore.add(new int[]{playDirection == 'H' ? row : step, playDirection == 'H' ? step : col});
            step++;
        }

        return suffix.toString();
    }

    /**
     * Gets the prefix of a word in the specified direction.
     *
     * @param resultGameBoard The result game board
     * @param start Coordinates of the first letter
     * @param playDirection Direction to check for the prefix ('H' for horizontal, 'V' for vertical)
     * @return The prefix of the word
     */
    public String getPrefix(GameBoard resultGameBoard, int[] start, char playDirection) {
        StringBuilder prefix = new StringBuilder();
        int row = start[0], col = start[1];
        boolean isHorizontal = playDirection == 'H';

        // Check if we're at the edge of the board
        if ((isHorizontal && col == 0) || (!isHorizontal && row == 0)) {
            return "";
        }

        // Iterate backwards to find the prefix
        for (int i = (isHorizontal ? col : row) - 1; i >= 0; i--) {
            if (!appendLetterIfNotEmpty(resultGameBoard, isHorizontal ? row : i, isHorizontal ? i : col, prefix)) {
                break;
            }
        }

        return prefix.reverse().toString();
    }

    /**
     * Appends a letter to the prefix if the tile is not empty.
     *
     * @param resultGameBoard The result game board
     * @param row Row of the tile
     * @param col Column of the tile
     * @param prefix StringBuilder to append the letter to
     * @return true if a letter was appended, false if the tile was empty
     */
    private boolean appendLetterIfNotEmpty(GameBoard resultGameBoard, int row, int col, StringBuilder prefix) {
        Tile tile = resultGameBoard.getTile(row, col);
        if (tile.isEmpty()) {
            return false;
        }
        prefix.append(tile.getLetter());
        coordinatesToScore.add(new int[]{row, col});
        return true;
    }

    /**
     * Checks if the play is valid by comparing it with anchor positions.
     *
     * @param anchors List of anchor positions
     * @param diffs List of positions where new tiles were placed
     * @return true if the play is valid, false otherwise
     */
    public boolean validPlay(LinkedList<int[]> anchors, LinkedList<int[]> diffs) {
        Set<Coordinate> anchorSet = new HashSet<>();
        for (int[] anchor : anchors) {
            anchorSet.add(new Coordinate(anchor[0], anchor[1]));
        }

        // Check if any of the new tile positions match an anchor position
        for (int[] diff : diffs) {
            if (anchorSet.contains(new Coordinate(diff[0], diff[1]))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Private inner class to represent a coordinate on the game board.
     * This class is used for efficient comparison of positions.
     */
    private static class Coordinate {
        final int x, y;

        Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Coordinate that = (Coordinate) o;
            return x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    // ====================================================================================================


    /**
     * Checks if the original and result game boards are compatible.
     * Boards are incompatible if tiles were removed or if there's a mismatch in multipliers.
     *
     * @param originalGameBoard The original game board
     * @param resultGameBoard The result game board after a play
     * @param diffs List of positions where new tiles were placed
     * @return true if the boards are compatible, false otherwise
     */
    public boolean boardsAreCompatible(GameBoard originalGameBoard, GameBoard resultGameBoard, LinkedList<int[]> diffs) {
        int dim = originalGameBoard.dimn;
        for (int r = 0; r < dim; r++) {
            for (int c = 0; c < dim; c++) {
                if (diffs.contains(new int[]{r,c})) {
                    continue; // Skip positions where new tiles were placed
                } else {
                    // Check if a tile was removed
                    if (!originalGameBoard.getTile(r,c).isEmpty() && resultGameBoard.getTile(r,c).isEmpty()) {
                        System.out.printf("Incompatible boards: tile removed at (%d, %d)\n", r, c);
                        return false;
                    }
                    // Check for mismatched multipliers
                    if ((originalGameBoard.getTile(r,c).isEmpty() && resultGameBoard.getTile(r,c).isEmpty()) &&
                            (originalGameBoard.getTile(r,c).hasMulti() && !resultGameBoard.getTile(r,c).hasMulti()) ||
                            (!originalGameBoard.getTile(r,c).hasMulti() && resultGameBoard.getTile(r,c).hasMulti())) {
                        System.out.printf("Incompatible boards: multiplier mismatch at (%d, %d)\n\n", r, c);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Main method to run the ScoreChecker program.
     *
     * @param args Command line arguments. Expects the dictionary file path as the first argument.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Scrabble.Checker <dictionary_file>");
            return;
        }

        String dictionaryFile = args[0];
        String tileScoreFile = "dictionaries_and_examples/scrabble_tiles.txt";

        try {
            loadScoreMap(tileScoreFile);
            loadDictionary(dictionaryFile);
            processInput();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads the score map from a file.
     *
     * @param filename Path to the file containing tile scores
     * @throws IOException If there's an error reading the file
     */
    private static void loadScoreMap(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            scoreMap = new ScoreMap(br);
        } catch (IOException e) {
            throw new IOException("Failed to load score map from " + filename, e);
        }
    }

    /**
     * Loads the dictionary from a file.
     *
     * @param filename Path to the dictionary file
     * @throws IOException If there's an error reading the file
     */
    private static void loadDictionary(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            dict = new Dictionary(br);
            if (dict.getTotalWordCount() == 0) {
                System.err.println("Warning: Dictionary loaded from " + filename + " is empty.");
            } else {
                System.out.println("Successfully loaded " + dict.getTotalWordCount() + " words from " + filename);
            }
        } catch (IOException e) {
            throw new IOException("Failed to load dictionary from " + filename, e);
        }
    }

    /**
     * Processes input from standard input to create and run the ScoreChecker.
     *
     * @throws IOException If there's an error reading from standard input
     */
    private static void processInput() throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            scoreChecker = new ScoreChecker(br);
        } catch (IOException e) {
            throw new IOException("Error processing input", e);
        }
    }
}