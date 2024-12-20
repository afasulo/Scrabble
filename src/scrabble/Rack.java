package scrabble;

/**
 * Represents a rack of tiles in a Scrabble game.
 * Extends the TileList class.
 */
public class Rack extends TileList {
    private static final int RACK_SIZE = 7;  // lol

    /**
     * Constructs a Rack and fills it with random tiles from the given TileBag.
     *
     * @param tileBag the TileBag to draw tiles from
     */
    public Rack(TileBag tileBag) {
        appendRandomTiles(RACK_SIZE, tileBag);
    }

    /**
     * Checks if the rack is full.
     *
     * @return true if the rack is full, false otherwise
     */
    public boolean isFull() {
        return count() == RACK_SIZE;
    }

    /**
     * Refills the rack with random tiles from the given TileBag until it is full.
     *
     * @param tileBag the TileBag to draw tiles from
     */
    public void refill(TileBag tileBag) {
        int tilesToAdd = RACK_SIZE - count();
        appendRandomTiles(tilesToAdd, tileBag);
    }

    /**
     * Returns a string representation of the rack.
     *
     * @return a string representation of the rack
     */
    @Override
    public String toString() {
        return "Tray: " + super.toString();
    }
}