package scrabble;

public class Tile {
    public static final int PLACEHOLDER = -1;
    private final int row;
    private final int col;
    private int multiplier;
    private final int pointValue;
    private final String letter;
    private char multiType;
    private boolean isEmpty, hasMulti, hasAdjacentTile;


    public Tile(int row, int col, String letter, int pointValue) {
        this.row = row;
        this.col = col;
        this.letter = letter;
        this.pointValue = pointValue;
        this.hasAdjacentTile = false;

        if (letter.length() == 1) {
            this.isEmpty = false;
            this.hasMulti = false;
            this.multiplier = 1;
            this.multiType = 'N';
        } else if (letter.length() == 2) {
            if (letter.equals("..")) {
                this.isEmpty = true;
                this.hasMulti = false;
                this.multiType = 'N';
                this.multiplier = 1;
            } else if (letter.charAt(1) == '.') {
                this.isEmpty = true;
                this.hasMulti = true;
                this.multiType = 'W';
                this.multiplier = Character.getNumericValue(letter.charAt(0));
            } else if (letter.charAt(0) == '.') {
                this.isEmpty = true;
                this.hasMulti = true;
                this.multiType = 'L';
                this.multiplier = Character.getNumericValue(letter.charAt(1));
            } else {
                throw new IllegalArgumentException("Invalid tile type: " + letter);
            }
        } else {
            throw new IllegalArgumentException("Invalid letter length: " + letter);
        }
    }
    public Tile(int row, int col, String letter, int pointValue, int multiplier, char multiType) {
        this.row = row;
        this.col = col;
        this.letter = letter;
        this.pointValue = pointValue;
        this.hasAdjacentTile = false;
        this.isEmpty = false;
        this.hasMulti = true;
        this.multiplier = multiplier;
        this.multiType = multiType;
    }


    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }
    public String getLetter() {
        return letter;
    }
    public char getMultiType() {
        return multiType;
    }
    public int getMultiplier() {
        return multiplier;
    }
    public int getPointValue() {
        return pointValue;
    }
    public boolean hasMulti() {
        return hasMulti;
    }
    public boolean isEmpty() {
        return isEmpty;
    }
    public boolean hasAdjacentTile() {
        return hasAdjacentTile;
    }
    public void setHasAdjacentTile(boolean hasAdjacentTile) {
        this.hasAdjacentTile = hasAdjacentTile;
    }

    public void hasLetter() {isEmpty = false;}

}