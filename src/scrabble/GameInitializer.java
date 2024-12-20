package scrabble;

import java.io.*;

public class GameInitializer {
    private static final String TILE_CONFIG_FILE = "dictionaries_and_examples/scrabble_tiles.txt";
    private static final int DEFAULT_BOARD_DIMENSION = 15;

    public ScoreMap initializeScoreMap() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(TILE_CONFIG_FILE))) {
            return new ScoreMap(br);
        }
    }

    public Dictionary initializeDictionary(String dictionaryFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(dictionaryFile))) {
            return new Dictionary(br);
        }
    }

    public GameBoard initializeBoard(String boardConfigFile, ScoreMap scoreMap) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(boardConfigFile))) {
            String dimensionLine = br.readLine();
            int dimension = dimensionLine != null ? Integer.parseInt(dimensionLine.trim()) : DEFAULT_BOARD_DIMENSION;
            GameBoard gameBoard = new GameBoard(dimension);

            StringBuilder boardConfig = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                boardConfig.append(line).append(" ");
            }
            gameBoard.setScoreMap(scoreMap);
            gameBoard.configBoard(boardConfig.toString().trim(), scoreMap);
            return gameBoard;
        }
    }

    public TileBag initializeTileBag() {
        return new TileBag(TILE_CONFIG_FILE);
    }
}