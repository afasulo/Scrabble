package scrabble;

import java.util.ArrayList;
import java.util.List;

public class Trie {
    private static final int ALPHABET_SIZE = 26;
    private static final char FIRST_LETTER = 'a';

    private  TrieNode root;
    private int wordCount;

    public Trie() {
        root = new TrieNode();
        wordCount = 0;
    }

    public boolean insertWord(String word) throws IllegalArgumentException {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("Word cannot be null or empty");
        }

        TrieNode current = root;
        boolean isNewWord = false;

        for (char c : word.toLowerCase().toCharArray()) {
            int index = getCharIndex(c);
            if (current.getChild(index) == null) {
                current.setChild(new TrieNode(), index);
            }
            current = current.getChild(index);
        }

        if (!current.isCompleteWord()) {
            current.markAsCompleteWord(true);
            wordCount++;
            isNewWord = true;
        }

        return isNewWord;
    }

    public boolean containsWord(String word) {
        TrieNode node = findNode(word);
        return node != null && node.isCompleteWord();
    }

    public boolean isPrefix(String prefix) {
        return findNode(prefix) != null;
    }

    public List<String> findWordsWithPrefix(String prefix) {
        List<String> words = new ArrayList<>();
        TrieNode prefixNode = findNode(prefix);

        if (prefixNode != null) {
            findAllWords(prefixNode, new StringBuilder(prefix), words);
        }

        return words;
    }

    public int getWordCount() {
        return wordCount;
    }

    private TrieNode findNode(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        TrieNode current = root;
        for (char c : str.toLowerCase().toCharArray()) {
            int index = getCharIndex(c);
            if (current.getChild(index) == null) {
                return null;
            }
            current = current.getChild(index);
        }
        return current;
    }

    private void findAllWords(TrieNode node, StringBuilder prefix, List<String> words) {
        if (node.isCompleteWord()) {
            words.add(prefix.toString());
        }

        for (int i = 0; i < ALPHABET_SIZE; i++) {
            if (node.getChild(i) != null) {
                char childChar = (char) (FIRST_LETTER + i);
                prefix.append(childChar);
                findAllWords(node.getChild(i), prefix, words);
                prefix.setLength(prefix.length() - 1);
            }
        }
    }

    private int getCharIndex(char c) {
        int index = c - FIRST_LETTER;
        if (index < 0 || index >= ALPHABET_SIZE) {
            throw new IllegalArgumentException("Invalid character: " + c);
        }
        return index;
    }

    public TrieNode getRoot() {
        return root;
    }

    class TrieNode {
        private final TrieNode[] children;
        private boolean isCompleteWord;

        TrieNode() {
            children = new TrieNode[ALPHABET_SIZE];
            isCompleteWord = false;
        }

        void setChild(TrieNode node, int index) {
            children[index] = node;
        }

        void markAsCompleteWord(boolean bool) {
            isCompleteWord = bool;
        }

        boolean isCompleteWord() {
            return isCompleteWord;
        }

        TrieNode getChild(int index) {
            return children[index];
        }
    }
}