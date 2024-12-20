package scrabble;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Solver {
    private Dictionary dictionary;
    private ScoreMap scoreMap;
    private String tileConfigFile;

    public Solver(String dictionaryFile, String tileConfigFile) throws IOException {
        this.tileConfigFile = tileConfigFile;
        initializeDictionary(dictionaryFile);
        initializeScoreMap(tileConfigFile);
    }

    private void initializeDictionary(String dictionaryFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(dictionaryFile))) {
            dictionary = new Dictionary(br);
        }
    }

    private void initializeScoreMap(String tileConfigFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(tileConfigFile))) {
            scoreMap = new ScoreMap(br);
        }
    }

    public void solve(BufferedReader input) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            int dimension = Integer.parseInt(line.trim());
            GameBoard gameBoard = new GameBoard(dimension, scoreMap);
            StringBuilder boardConfig = new StringBuilder();

            for (int i = 0; i < dimension; i++) {
                boardConfig.append(input.readLine()).append(" ");
            }

            gameBoard.configBoard(boardConfig.toString().trim(), scoreMap);
            gameBoard.setDictionary(dictionary);

            String tray = input.readLine().trim();

            printInputBoard(gameBoard, tray);

            ComputerPlayer computerPlayer = new ComputerPlayer(new Rack(new TileBag(tileConfigFile)), gameBoard, dictionary);
            computerPlayer.getRack().getAllTiles().clear();
            for (char c : tray.toCharArray()) {
                computerPlayer.getRack().addTile(new Tile(-1, -1, String.valueOf(c), scoreMap.getScore(String.valueOf(c))));
            }

            computerPlayer.generateNextMove();

            if (computerPlayer.hasNoValidMoves()) {
                System.out.println("No valid moves found.");
            } else {
                String word = computerPlayer.getNextMoveWord();
                int score = computerPlayer.getBestMoveScore();
                System.out.printf("Solution %s has %d points%n", word, score);

                gameBoard.updateBoard(word, computerPlayer.getNextMoveCoordinates());
                printSolutionBoard(gameBoard);
            }

            System.out.println(); // Print blank line between solutions
        }
    }

    private void printInputBoard(GameBoard gameBoard, String tray) {
        System.out.println("Input Board:");
        System.out.print(gameBoard.toString());
        System.out.println("Tray: " + tray);
    }

    private void printSolutionBoard(GameBoard gameBoard) {
        System.out.println("Solution Board:");
        System.out.print(gameBoard.toString());
    }

    public static void main(String[] args) {
        if (args.length != 2 && args.length != 3) {
            System.out.println("Usage: java -jar solver.jar <dictionary_file> <tile_config_file> [<input_file>]");
            System.exit(1);
        }

        try {
            Solver solver = new Solver(args[0], args[1]);
            BufferedReader input;
            if (args.length == 3) {
                // If an input file is provided, read from it
                input = new BufferedReader(new FileReader(args[2]));
            } else {
                // Otherwise, read from standard input
                input = new BufferedReader(new InputStreamReader(System.in));
            }
            solver.solve(input);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}