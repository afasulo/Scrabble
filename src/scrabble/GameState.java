package scrabble;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean isGameOver;
    private int humanScore;
    private int computerScore;
    private String lastWordPlayedByHuman;
    private String lastWordPlayedByComputer;
    private Player winner;
    private boolean isFirstMove;
    private char currentTurn;
    private boolean humanGaveUpTurn;

    public GameState() {
        this.isGameOver = false;
        this.humanScore = 0;
        this.computerScore = 0;
        this.lastWordPlayedByHuman = "";
        this.lastWordPlayedByComputer = "";
        this.winner = null;
        this.isFirstMove = true;
        this.currentTurn = 'H';
    }

    // Getters and setters
    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean gameOver) { isGameOver = gameOver; }
    public int getHumanScore() { return humanScore; }
    public void addToHumanScore(int score) { this.humanScore += score; }
    public int getComputerScore() { return computerScore; }
    public void addToComputerScore(int score) { this.computerScore += score; }
    public String getLastWordPlayedByHuman() { return lastWordPlayedByHuman; }
    public void setLastWordPlayedByHuman(String word) { this.lastWordPlayedByHuman = word; }
    public String getLastWordPlayedByComputer() { return lastWordPlayedByComputer; }
    public void setLastWordPlayedByComputer(String word) { this.lastWordPlayedByComputer = word; }
    public Player getWinner() { return winner; }
    public void setWinner(Player winner) { this.winner = winner; }
    public boolean isFirstMove() { return isFirstMove; }
    public void setFirstMove(boolean firstMove) { isFirstMove = firstMove; }
    public char getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(char currentTurn) { this.currentTurn = currentTurn; }

    public boolean isHumanGaveUpTurn() {
        return humanGaveUpTurn;
    }

    public void setHumanGaveUpTurn(boolean humanGaveUpTurn) {
        this.humanGaveUpTurn = humanGaveUpTurn;
    }

    public void setHumanScore(int humanScore) {
        this.humanScore = humanScore;
    }
}