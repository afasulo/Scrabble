package scrabble;

import java.util.LinkedList;

public class Player {

    private Rack rack;
    private boolean isMyMove;
    private LinkedList<int[]> nextMoveCoordinates;
    private String nextMoveWord;

    /**
     * Constructs a Player instance.
     *
     * @param rack the rack that the player will use
     */
    public Player(Rack rack) {
        this.rack = rack;
        this.isMyMove = false;
        this.nextMoveCoordinates = new LinkedList<>();
        this.nextMoveWord = "";
    }

    /**
     * Sets the rack for the player.
     *
     * @param rack the rack to assign to the player
     */
    public void setRack(Rack rack) {
        this.rack = rack;
    }

    /**
     * Retrieves the player's rack.
     *
     * @return the current rack of the player
     */
    public Rack getRack() {
        return this.rack;
    }

    /**
     * Retrieves the coordinates of the next move on the board.
     *
     * @return a LinkedList of int arrays representing the coordinates of the next move
     */
    public LinkedList<int[]> getNextMoveCoordinates() {
        return this.nextMoveCoordinates;
    }

    /**
     * Sets the coordinates for the next move on the board.
     *
     * @param nextMoveCoordinates a LinkedList of int arrays representing the coordinates for the next move
     */
    public void setNextMoveCoordinates(LinkedList<int[]> nextMoveCoordinates) {
        this.nextMoveCoordinates = nextMoveCoordinates;
    }

    /**
     * Retrieves the word to be played in the next move.
     *
     * @return the word that is set for the next move
     */
    public String getNextMoveWord() {
        return this.nextMoveWord;
    }

    /**
     * Sets the word for the next move.
     *
     * @param nextMoveWord the word to be played in the next move
     */
    public void setNextMoveWord(String nextMoveWord) {
        this.nextMoveWord = nextMoveWord;
    }
}
