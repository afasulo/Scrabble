package scrabble;

import java.io.*;
import java.util.*;

public class ScoreMap {
    private final Map<String, Integer> scores;
    private final Map<String, Integer> frequencies;
    private final int totalTiles;

    // Constructor initializes score and frequency maps based on the buffered input
    public ScoreMap(BufferedReader br) throws IOException {
        scores = new HashMap<>();
        frequencies = new HashMap<>();
        int tileCount = 0;

        String lineData;
        int currentLine = 0;

        // Read input line-by-line
        while ((lineData = br.readLine()) != null) {
            currentLine++;
            lineData = lineData.trim();

            if (lineData.isEmpty()) continue; // Skip if the line is blank

            String[] elements = lineData.split("\\s+");
            if (elements.length != 3) {
                throw new IOException("Format error at line " + currentLine + ": " + lineData);
            }

            String letterKey = elements[0].toLowerCase();
            int letterScore, letterFrequency;

            try {
                letterScore = Integer.parseInt(elements[1]);
                letterFrequency = Integer.parseInt(elements[2]);
            } catch (NumberFormatException ex) {
                throw new IOException("Invalid number at line " + currentLine + ": " + lineData, ex);
            }

            if (letterScore < 0 || letterFrequency < 0) {
                throw new IOException("Negative values found at line " + currentLine + ": " + lineData);
            }

            scores.put(letterKey, letterScore);
            frequencies.put(letterKey, letterFrequency);
            tileCount += letterFrequency;
        }

        if (scores.isEmpty()) {
            throw new IOException("No valid input found in the file");
        }

        totalTiles = tileCount;
    }

    // Return the sum of letter scores considering frequencies
    public int getAllLetterScores() {
        return scores.keySet()
                .stream()
                .mapToInt(letter -> scores.get(letter) * frequencies.get(letter))
                .sum();
    }

    // Return the total count of letter frequencies
    public int getAllLetterFrequencies() {
        return frequencies.values().stream().mapToInt(Integer::intValue).sum();
    }

    // Retrieve score of a single letter
    public int getScore(String character) {
        String validLetter = validateAndExtractLetter(character);
        return scores.getOrDefault(validLetter, 0);
    }

    // Retrieve frequency of a single letter
    public int getFrequency(String character) {
        String validLetter = validateAndExtractLetter(character);
        return frequencies.getOrDefault(validLetter, 0);
    }

    // Verify if the given character is a valid letter
    public boolean isValidLetter(String character) {
        if (character == null || character.isEmpty()) {
            return false;
        }
        String letter = character.toLowerCase().substring(0, 1);
        return scores.containsKey(letter);
    }

    // Get total number of tiles
    public int getTotalTiles() {
        return totalTiles;
    }

    // Return the set of all unique letters
    public Set<String> getAllLetters() {
        return Collections.unmodifiableSet(scores.keySet());
    }

    // Return an unmodifiable view of the score map
    public Map<String, Integer> getScoreMap() {
        return Collections.unmodifiableMap(scores);
    }

    // Helper method to validate and extract the first letter of input
    private String validateAndExtractLetter(String character) {
        if (character == null || character.isEmpty()) {
            throw new IllegalArgumentException("Input can't be null or empty");
        }
        return character.toLowerCase().substring(0, 1);
    }

    // Custom toString method for representation of score map and frequency
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ScoreMap{\n");
        getAllLetters().forEach(letter -> sb.append(String.format("  %s: score=%d, frequency=%d\n",
                letter, getScore(letter), getFrequency(letter))));
        sb.append(String.format("  Total tiles: %d\n", getTotalTiles()));
        sb.append("}");
        return sb.toString();
    }
}
