package scrabble;

import java.io.*;
import java.util.Map;

public class TileBag extends TileList {
    private LetterPointSystem letterPointSystem;

    public TileBag(String configFilePath) {
        this.letterPointSystem = initializePointSystem(configFilePath);
        populateBag();
    }

    private LetterPointSystem initializePointSystem(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return new LetterPointSystem(reader);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load letter point system: " + e.getMessage(), e);
        }
    }

    private void populateBag() {
        for (Map.Entry<String, LetterInfo> entry : letterPointSystem.getLetterInfoMap().entrySet()) {
            String letter = entry.getKey();
            LetterInfo info = entry.getValue();

            if (!letter.equals("*")) {
                for (int i = 0; i < info.getFrequency(); i++) {
                    addTile(createTile(letter, info.getPoints()));
                }
            }
        }

        addBlankTiles();
    }

    private void addBlankTiles() {
        LetterInfo blankInfo = letterPointSystem.getLetterInfoMap().get("*");
        if (blankInfo != null) {
            for (int i = 0; i < blankInfo.getFrequency(); i++) {
                addTile(createTile("*", blankInfo.getPoints()));
            }
        }
    }

    private Tile createTile(String letter, int points) {
        return new Tile(Tile.PLACEHOLDER, Tile.PLACEHOLDER, letter, points);
    }

    public int getPointsForLetter(char letter) {
        return letterPointSystem.getPoints(String.valueOf(letter));
    }

    public LetterPointSystem getLetterPointSystem() {
        return letterPointSystem;
    }

    @Override
    public String toString() {
        return String.format("TileBag: %s (Total: %d)", super.toString(), count());
    }

    private static class LetterInfo {
        private final int points;
        private final int frequency;

        public LetterInfo(int points, int frequency) {
            this.points = points;
            this.frequency = frequency;
        }

        public int getPoints() {
            return points;
        }

        public int getFrequency() {
            return frequency;
        }
    }

    private static class LetterPointSystem {
        private final Map<String, LetterInfo> letterInfoMap;

        public LetterPointSystem(BufferedReader reader) throws IOException {
            this.letterInfoMap = new java.util.HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 3) {
                    String letter = parts[0].toLowerCase();
                    int points = Integer.parseInt(parts[1]);
                    int frequency = Integer.parseInt(parts[2]);
                    letterInfoMap.put(letter, new LetterInfo(points, frequency));
                }
            }
        }

        public int getPoints(String letter) {
            LetterInfo info = letterInfoMap.get(letter.toLowerCase());
            return (info != null) ? info.getPoints() : 0;
        }

        public Map<String, LetterInfo> getLetterInfoMap() {
            return letterInfoMap;
        }
    }
}