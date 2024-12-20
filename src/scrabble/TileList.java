package scrabble;

import java.util.LinkedList;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;


public class TileList {
    private LinkedList<Tile> tileList;

    public TileList() {
        tileList = new LinkedList<Tile>();
    }

    public int count() {
        return tileList.size();
    }

    public LinkedList<Tile> getAllTiles() {
        return tileList;
    }

    public void appendRandomTiles(int numTiles, TileList list) {
    List<Tile> tiles = new ArrayList<>(list.getAllTiles());
    Collections.shuffle(tiles);
    for (int i = 0; i < numTiles && !tiles.isEmpty(); i++) {
        this.tileList.add(tiles.remove(0));
    }
    list.getAllTiles().clear();
    list.getAllTiles().addAll(tiles);
}

    public void addTile(Tile tile) {
        tileList.add(tile);
    }

    public boolean isEmpty() {
        return tileList.isEmpty();
    }

    public Tile removeRandomTile() {
        if (isEmpty()) {
            return null;
        }
        int index = new Random().nextInt(count());
        return tileList.remove(index);
    }

    public Tile extractTileByLetter(String letter) {
        for (int i = 0; i < count(); i++) {
            Tile tile = tileList.get(i);
            if (tile.getLetter().equalsIgnoreCase(letter)) {
                return tileList.remove(i);
            }
        }
        return null;
    }

    public boolean containsLetter(String letter) {
        for (Tile tile : tileList) {
            if (tile.getLetter().equalsIgnoreCase(letter)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getLetters() {
        List<String> letters = new ArrayList<>();
        for (Tile tile : tileList) {
            letters.add(tile.getLetter());
        }
        return letters;
    }

    @Override
    public String toString() {
    return tileList.stream()
                   .map(Tile::getLetter)
                   .collect(Collectors.joining(" ", "TileList: ", ""));
    }
}