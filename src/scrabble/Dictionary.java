package scrabble;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class Dictionary {

    private final Trie trie;
    private final Set<String> wordSet;

    /**
     * Constructs a Dictionary instance by loading words from a BufferedReader.
     *
     * @param br the BufferedReader providing the dictionary words
     * @throws IOException if an error occurs during reading
     */
    public Dictionary(BufferedReader br) throws IOException {
        this.trie = new Trie();
        this.wordSet = new HashSet<>();
        populateDictionary(br);
    }

    /**
     * Reads and processes words from the provided BufferedReader into the dictionary.
     *
     * @param br the BufferedReader containing dictionary words
     * @throws IOException if an error occurs during reading
     */
    private void populateDictionary(BufferedReader br) throws IOException {
        String line;
        int lineNumber = 0;

        // Process each line from the BufferedReader
        while ((line = br.readLine()) != null) {
            lineNumber++;
            line = line.toLowerCase().trim();

            if (line.isEmpty()) {
                continue; // Skip empty lines
            }

            try {
                // Insert the word into the trie and wordSet if valid
                if (trie.insertWord(line)) {
                    wordSet.add(line);
                }
            } catch (IllegalArgumentException e) {
                // Handle invalid words with a warning
                System.err.printf("Warning: Invalid word at line %d: '%s'. Error: %s%n", lineNumber, line, e.getMessage());
            }
        }
    }

    /**
     * Checks if a word exists in the dictionary.
     *
     * @param word the word to search for
     * @return true if the word exists, false otherwise
     */
    public boolean isWordInDictionary(String word) {
        return trie.containsWord(word);
    }

    /**
     * Checks if a given prefix is valid (i.e., it exists as a start of some words).
     *
     * @param prefix the prefix to search for
     * @return true if the prefix is valid, false otherwise
     */
    public boolean isValidPrefix(String prefix) {
        return trie.isPrefix(prefix);
    }

    /**
     * Returns an unmodifiable view of the word set.
     *
     * @return a set of words in the dictionary
     */
    public Set<String> getAllWords() {
        return Collections.unmodifiableSet(wordSet);
    }

    /**
     * Gets the total number of words stored in the trie.
     *
     * @return the total word count
     */
    public int getTotalWordCount() {
        return trie.getWordCount();
    }

    /**
     * Adds a new word to the dictionary.
     *
     * @param word the word to be added
     * @return true if the word was successfully added, false otherwise
     */
    public boolean insertWordIntoDictionary(String word) {
        try {
            if (trie.insertWord(word)) {
                wordSet.add(word.toLowerCase().trim());
                return true;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Failed to add word: " + e.getMessage());
        }
        return false;
    }

    /**
     * Finds and returns anagrams of a given word from the dictionary.
     * Anagrams are words that contain the same characters as the given word.
     *
     *
     * @param word the word to find anagrams for
     * @return a set of anagrams found in the dictionary
     */
    public Set<String> getAnagrams(String word) {
        Set<String> anagrams = new HashSet<>();
        char[] chars = word.toLowerCase().toCharArray();
        Arrays.sort(chars);

        for (String dictWord : wordSet) {
            if (dictWord.length() == word.length()) {
                char[] dictChars = dictWord.toCharArray();
                Arrays.sort(dictChars);
                if (Arrays.equals(chars, dictChars)) {
                    anagrams.add(dictWord);
                }
            }
        }

        return anagrams;
    }

    /**
     * Finds words in the dictionary that match a given regex pattern.
     *
     * @param pattern the regex pattern to match
     * @return a set of words that match the pattern
     */
    public Set<String> findWordsMatchingPattern(String pattern) {
        Set<String> matchingWords = new HashSet<>();
        for (String word : wordSet) {
            if (word.matches(pattern)) {
                matchingWords.add(word);
            }
        }
        return matchingWords;
    }

    /**
     * Finds and returns a list of words that start with a given prefix.
     *
     * @param prefix the prefix to search for
     * @return a list of words that start with the prefix
     */
    public List<String> getWordsStartingWith(String prefix) {
        return trie.findWordsWithPrefix(prefix);
    }

    /**
     * Retrieves the underlying Trie structure of the dictionary.
     *
     * @return the Trie object
     */
    public Trie getTrie() {
        return this.trie;
    }
}
