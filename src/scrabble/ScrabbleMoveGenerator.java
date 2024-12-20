package scrabble;

import java.util.*;

public class ScrabbleMoveGenerator {
    private int score;
    private GameBoard gameBoard;
    private List<Character> availableTiles;
    private Dictionary dictionary;
    private LinkedList<int[]> anchorPoints;
    private LinkedList<String> leftPartialWords;
    private TreeMap<Integer, LinkedHashMap<String, LinkedHashMap<int[], Boolean>>> possibleMoves;
    private LinkedList<Trie.TrieNode> trieNodes;
    private LinkedList<int[]> bestMoveCoordinates;
    private String bestWord;
    private DifficultyLevel difficulty;

    private enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }

    public ScrabbleMoveGenerator(GameBoard gameBoard, Dictionary dictionary) {
        this.score = 0;
        this.gameBoard = gameBoard;
        this.availableTiles = new LinkedList<>();
        this.dictionary = dictionary;
        this.anchorPoints = gameBoard.getAnchorPositions();
        this.leftPartialWords = new LinkedList<>();
        this.possibleMoves = new TreeMap<>(Collections.reverseOrder());
        this.trieNodes = new LinkedList<>();
        this.bestMoveCoordinates = new LinkedList<>();
        this.bestWord = "";
        this.difficulty = DifficultyLevel.HARD;
    }

    private void generateLeftPartialWords(String partialWord, Trie.TrieNode node, int limit,
                                          int row, int col, boolean isRotated, int anchorRow, int anchorCol) {
        if (limit > 0) {
            for (char c = 'a'; c <= 'z'; c++) {
                if (processCharacter(partialWord, node, limit, row, col, isRotated, anchorRow, anchorCol, c)) {
                    break;
                }
            }
        }

        leftPartialWords.add(partialWord);
        trieNodes.add(node);

        if (gameBoard.isValidDyWord(partialWord, row, col - 1)) {
            extendWordRight(partialWord, node, row, col, isRotated, anchorRow, anchorCol);
        }
    }

    private boolean processCharacter(String partialWord, Trie.TrieNode node, int limit,
                                     int row, int col, boolean isRotated, int anchorRow, int anchorCol, char c) {
        Trie.TrieNode childNode = node.getChild(c - 'a');
        if (childNode != null && (availableTiles.contains(c) || availableTiles.contains('*'))) {
            boolean usingWildTile = !availableTiles.contains(c);
            char character = usingWildTile ? Character.toUpperCase(c) : c;

            Character tileToRemove = usingWildTile ? '*' : c;
            availableTiles.remove(tileToRemove);

            generateLeftPartialWords(partialWord + character, childNode, limit - 1,
                    row, col, isRotated, anchorRow, anchorCol);

            availableTiles.add(tileToRemove);

            return true;
        }
        return false;
    }

    public void extendWordRight(String partialWord, Trie.TrieNode node, int row, int col,
                                boolean isRotated, int anchorRow, int anchorCol) {
        if (isValidCompleteWord(partialWord, node, row, col, anchorRow, anchorCol)) {
            recordMove(partialWord, row, col, isRotated);
        }

        if (col >= 0 && col < gameBoard.getDimension()) {
            if (!gameBoard.hasExistingLetter(row, col)) {
                extendWithNewCharacter(partialWord, node, row, col, isRotated, anchorRow, anchorCol);
            } else {
                extendWithExistingCharacter(partialWord, node, row, col, isRotated, anchorRow, anchorCol);
            }
        }
    }

    private boolean isValidCompleteWord(String word, Trie.TrieNode node, int row, int col, int anchorRow, int anchorCol) {
        return (row > anchorRow || col > anchorCol) && !gameBoard.hasExistingLetter(row, col) && node.isCompleteWord();
    }

    private void recordMove(String word, int row, int col, boolean isRotated) {
        LinkedHashMap<int[], Boolean> map = new LinkedHashMap<>();
        LinkedList<int[]> index = gameBoard.calculateWordCoordinates(row, col - 1, isRotated, word);
        boolean allTilesUsed = availableTiles.isEmpty();
        int moveScore = gameBoard.calculateTotalScore(index, word, allTilesUsed);
        map.put(new int[]{row, col - 1, moveScore}, isRotated);
        LinkedHashMap<String, LinkedHashMap<int[], Boolean>> wordXYMap = new LinkedHashMap<>();
        wordXYMap.put(word, map);
        possibleMoves.computeIfAbsent(moveScore, k -> new LinkedHashMap<>()).putAll(wordXYMap);
    }

    private void extendWithNewCharacter(String partialWord, Trie.TrieNode node, int row, int col,
                                        boolean isRotated, int anchorRow, int anchorCol) {
        for (char c = 'a'; c <= 'z'; c++) {
            Trie.TrieNode childNode = node.getChild(c - 'a');
            if (childNode != null && (availableTiles.contains(c) || availableTiles.contains('*')) && gameBoard.isValidDxWord(row, col, c)) {
                boolean usingWildTile = !availableTiles.contains(c);
                char character = usingWildTile ? Character.toUpperCase(c) : c;

                Character tileToRemove = usingWildTile ? '*' : c;
                availableTiles.remove(tileToRemove);

                extendWordRight(partialWord + character, childNode, row, col + 1, isRotated, anchorRow, anchorCol);

                availableTiles.add(tileToRemove);
            }
        }
    }

    private void extendWithExistingCharacter(String partialWord, Trie.TrieNode node, int row, int col,
                                             boolean isRotated, int anchorRow, int anchorCol) {
        char c = gameBoard.getTileLetter(row, col);
        Trie.TrieNode childNode = node.getChild(Character.toLowerCase(c) - 'a');
        if (childNode != null) {
            extendWordRight(partialWord + c, childNode, row, col + 1, isRotated, anchorRow, anchorCol);
        }
    }

    public void generateMovesForCurrentOrientation() {
        for (int i = 0; i < gameBoard.getDimension(); i++) {
            for (int j = 0; j < gameBoard.getDimension(); j++) {
                if (gameBoard.getAnchorPoints()[i][j].equals("A")) {
                    processAnchorPoint(i, j);
                }
            }
        }
    }

    private void processAnchorPoint(int row, int col) {
        int leftEmptySqr = gameBoard.countEmptySqauresLeft(row, col);
        if (leftEmptySqr == 0) {
            processExistingWord(row, col);
        } else {
            int limit = Math.min(leftEmptySqr, 7);
            generateLeftPartialWords("", dictionary.getTrie().getRoot(), limit, row, col, gameBoard.isRo(), row, col);
        }
    }

    private void processExistingWord(int row, int col) {
        String leftPart = gameBoard.getAdjacentWord(row, col, 'L');
        Trie.TrieNode node = dictionary.getTrie().getRoot();
        for (int k = 0; k < leftPart.length(); k++) {
            node = node.getChild(leftPart.toLowerCase().charAt(k) - 'a');
            if (node == null) {
                return;
            }
        }
        extendWordRight(leftPart, node, row, col, gameBoard.isRo(), row, col);
    }

    public void generateAllPossibleMoves() {
        possibleMoves.clear();
        gameBoard.updateAnchorPoints();
        generateMovesForCurrentOrientation();
        gameBoard.rotateBoardCCW();
        gameBoard.updateAnchorPoints();
        generateMovesForCurrentOrientation();
        gameBoard.rotateBoardClockwise();
        gameBoard.updateAnchorPoints();
    }

    private boolean isAnchorPoint(int i, int j) {
        return anchorPoints.stream().anyMatch(anchor -> anchor[0] == i && anchor[1] == j);
    }

    public void determineBestMove() {
        generateAllPossibleMoves();
        if (possibleMoves.isEmpty()) {
            resetBestMove();
            return;
        }

        Map.Entry<Integer, LinkedHashMap<String, LinkedHashMap<int[], Boolean>>> highestScoringEntry = possibleMoves.firstEntry();
        this.score = highestScoringEntry.getKey();

        LinkedHashMap<String, LinkedHashMap<int[], Boolean>> wordMap = highestScoringEntry.getValue();
        String highestScoringWord = wordMap.keySet().iterator().next();
        LinkedHashMap<int[], Boolean> positionMap = wordMap.get(highestScoringWord);

        Map.Entry<int[], Boolean> positionEntry = positionMap.entrySet().iterator().next();
        int[] position = positionEntry.getKey();
        boolean isRotated = positionEntry.getValue();

        int x = position[0];
        int y = position[1];
        bestMoveCoordinates = gameBoard.calculateWordCoordinates(x, y, isRotated, highestScoringWord);
        bestWord = highestScoringWord;

        adjustMoveBasedOnDifficulty();
    }

    private void resetBestMove() {
        bestWord = "";
        bestMoveCoordinates.clear();
        score = 0;
    }

    private void adjustMoveBasedOnDifficulty() {
        switch (difficulty) {
            case EASY:
                reduceScoreRandomly(0.5, 0.8);
                break;
            case MEDIUM:
                reduceScoreRandomly(0.8, 0.95);
                break;
            case HARD:
                // No adjustment needed
                break;
        }
    }

    private void reduceScoreRandomly(double minFactor, double maxFactor) {
        double randomFactor = minFactor + Math.random() * (maxFactor - minFactor);
        this.score = (int) (this.score * randomFactor);
    }

    public int getScore() {
        return score;
    }

    public LinkedList<int[]> getBestMoveCoordinates() {
        return new LinkedList<>(bestMoveCoordinates);
    }

    public String getBestWord() {
        return bestWord;
    }

    public void setAvailableTiles(LinkedList<Character> availableTiles) {
        this.availableTiles = new LinkedList<>(availableTiles);
    }

    public void setDifficulty(String difficultyLevel) {
        this.difficulty = DifficultyLevel.valueOf(difficultyLevel.toUpperCase());
    }
}