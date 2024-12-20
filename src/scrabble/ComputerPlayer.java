package scrabble;

import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ComputerPlayer extends Player {
    private static final Logger LOGGER = Logger.getLogger(ComputerPlayer.class.getName());

    private Rack rack;
    private ScrabbleMoveGenerator moveGenerator;
    private int score;
    public boolean hasNoValidMove;

    public ComputerPlayer(Rack rack, GameBoard gameBoard, Dictionary dictionary) {
        super(rack);
        LOGGER.info("Initializing ComputerPlayer");
        initializeComputerPlayer(rack, gameBoard, dictionary);
    }

    private void initializeComputerPlayer(Rack playerRack, GameBoard gameBoard, Dictionary dictionary) {
        this.rack = playerRack;
        this.moveGenerator = new ScrabbleMoveGenerator(gameBoard, dictionary);
        LinkedList<Character> tiles = convertTilesToLettersList(playerRack.getAllTiles());
        moveGenerator.setAvailableTiles(tiles);
        this.hasNoValidMove = false;
        this.score = 0;

        LOGGER.log(Level.INFO, "ComputerPlayer initialized with rack: {0}",
                tiles.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")));
    }

    public void setComputerPlayerDifficulty(String difficulty) {
        LOGGER.log(Level.INFO, "Setting computer difficulty to: {0}", difficulty);
        if (moveGenerator instanceof ScrabbleMoveGenerator) {
            moveGenerator.setDifficulty(difficulty);
        }
    }

    public void generateNextMove() {
        LOGGER.info("Generating next computer move");
        moveGenerator.determineBestMove();
        String bestWord = moveGenerator.getBestWord();
        LinkedList<int[]> coordinates = moveGenerator.getBestMoveCoordinates();

        if (!bestWord.isEmpty()) {
            LOGGER.log(Level.INFO, "Found move: word={0}, score={1}, coordinates={2}",
                    new Object[]{bestWord, moveGenerator.getScore(),
                            coordinates.stream()
                                    .map(coord -> String.format("(%d,%d)", coord[0], coord[1]))
                                    .collect(Collectors.joining(", "))});
        } else {
            LOGGER.info("No valid moves found");
        }

        setNextMove(coordinates, bestWord);
    }

    private void setNextMove(LinkedList<int[]> coordinates, String word) {
        setNextMoveCoordinates(coordinates);
        setNextMoveWord(word);
        hasNoValidMove = word.isEmpty();
    }

    public boolean hasNoValidMoves() {
        return hasNoValidMove;
    }

    @Override
    public void setRack(Rack rack) {
        LOGGER.log(Level.INFO, "Updating rack: {0}",
                rack.getLetters().stream().collect(Collectors.joining(", ")));
        this.rack = rack;
        moveGenerator.setAvailableTiles(convertTilesToLettersList(rack.getAllTiles()));
    }

    public int getBestMoveScore() {
        return moveGenerator.getScore();
    }

    public Rack getRack() {
        return rack;
    }

    private LinkedList<Character> convertTilesToLettersList(LinkedList<Tile> tileList) {
        LinkedList<Character> letterList = new LinkedList<>();
        for (Tile tile : tileList) {
            letterList.add(tile.getLetter().charAt(0));
        }
        return letterList;
    }

    public void updateScore(int additionalScore) {
        int oldScore = this.score;
        this.score += additionalScore;
        LOGGER.log(Level.INFO, "Score updated: {0} -> {1} (+{2})",
                new Object[]{oldScore, this.score, additionalScore});
    }

    public int getTotalScore() {
        return this.score;
    }

    public void refreshRack(TileBag tileBag) {
        LOGGER.log(Level.INFO, "Refreshing rack. Before refresh: {0}",
                rack.getLetters().stream().collect(Collectors.joining(", ")));

        rack.refill(tileBag);
        moveGenerator.setAvailableTiles(convertTilesToLettersList(rack.getAllTiles()));

        LOGGER.log(Level.INFO, "After refresh: {0}",
                rack.getLetters().stream().collect(Collectors.joining(", ")));
    }
}