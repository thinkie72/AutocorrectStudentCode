// Used by Tyler Hinkie in March 2025
public class Trie {
    // Instance variables
    private TrieNode root;

    // Constructor
    public Trie() {
        root = new TrieNode();
    }

    // Methods
    public void insert(String word) {
        TrieNode node = root;
        char[] letters = word.toCharArray();
        for (char letter : letters) {
            if (node.getNext()[letter] == null) node.getNext()[letter] = new TrieNode();
            node = node.getNext()[letter];
        }
        node.setWord();
    }

    public boolean lookup(String word) {
        TrieNode node = root;
        char[] letters = word.toCharArray();
        for (char letter : letters) {
            if (node.getNext()[letter] == null) return false;
            node = node.getNext()[letter];
        }
        return node.isWord();
    }
}
