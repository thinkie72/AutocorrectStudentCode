import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 *
 * @author Zach Blick
 * @author Tyler Hinkie
 */
public class Autocorrect {
    // The longest word in the english dictionary (large.txt): pneumonoultramicroscopicsilicovolcanoconiosis
    private final int LONGEST_WORD = 45;
    // 28^2 or number of possible 2-grams
    private final int MAX_TWO_GRAMS = 784;
    // 28 possible characters (letters & ' & -)
    private final int NUM_LETTERS = 28;
    // The Trie is just used for checking if the typed word is a word
    private Trie dict;
    // Big data structure to sort all the candidates by word length and two-grams
    private ArrayList<String>[][] candidatesSorted;
    // String[] for the words to run in runTest()
    private String[] chosen;

    /**
     * Constucts an instance of the Autocorrect class.
     *
     * @param words The dictionary of acceptable words.
     */
    public Autocorrect(String[] words, int threshold) {
        candidatesSorted = new ArrayList[LONGEST_WORD][MAX_TWO_GRAMS];
        for (int i = 0; i < LONGEST_WORD; i++) {
            for (int j = 0; j < MAX_TWO_GRAMS; j++) {
                candidatesSorted[i][j] = new ArrayList<>();
            }
        }
        int length;

        // Sends each two-gram to be hashed for words to be sorted
        for (String word : words) {
            length = word.length();
            for (int i = 0; i < length - 2; i++) {
                candidatesSorted[length - 1][hash(word.substring(i, i + 2))].add(word);
            }
        }

        int twoGram;

        // Create a Trie for the dictionary
        dict = new Trie();
        // Insert each word in the dictionary into the trie version
        for (String word : words) dict.insert(word);
    }

    // Converts a character into an integer for our alphabet (letters & - & ')
    private int charToInt(char c) {
        if (c == '-') return 26;
        if (c == '\'') return 27;
        return c - 'a';
    }

    // Compute two-gram hash using Horner’s Method with base 28
    private int hash(String word) {
        int first = charToInt(word.charAt(0));
        int second = charToInt(word.charAt(1));
        return (first * NUM_LETTERS + second) % MAX_TWO_GRAMS;
    }

    // Checks if the typed word is a word
    public boolean isWord(String typed) {
        return dict.lookup(typed);
    }

    // Selects candidates for each typed word to be later compared for results
    public void chooseCandidates(String typed) {
        int length = typed.length();
        // Makes sure the lengths are on same scale as they were initialized
        length--;

        ArrayList<String> candidates = new ArrayList<>();

        int twoGram;

        // Just decided to ignore edge case of longest things (you'll just get nulls)
        if (length < 43) {
            for (int i = 0; i < length; i++) {
                twoGram = hash(typed.substring(i, i + 2));
                candidates.addAll(this.candidatesSorted[length][twoGram]);
                for (int j = 1; j <= 2; j++) {
                    candidates.addAll(this.candidatesSorted[length - i][twoGram]);
                    candidates.addAll(this.candidatesSorted[length + i][twoGram]);
                }
            }
        } else {
            for (ArrayList<String> a : this.candidatesSorted[length]) candidates.addAll(a);
        }

        // Gets rid of duplicates (fast but at expense of space)
        Set<String> set = new LinkedHashSet<>(candidates);
        candidates.clear();
        candidates.addAll(set);
        ;

        String[] out = new String[candidates.size()];
        int i = 0;
        while (!candidates.isEmpty()) {
            out[i++] = candidates.removeFirst();
        }

        chosen = out;
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     *
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed) {

        if (chosen == null) return null;

        ArrayList<String>[] test = new ArrayList[LONGEST_WORD];

        for (int i = 0; i < test.length; i++) {
            test[i] = new ArrayList<String>();
        }

        int ed;
        int arrLen = 0;

        // Calculates edit distance for each candidate to be "sorted"
        for (String word : chosen) {
            ed = editDistance(typed, word);
            test[ed - 1].add(word);
            arrLen++;
        }

        // Only sends out 10 words
        String[] out = new String[10];

        int index = 0;
        for (ArrayList<String> t : test) {
            while (!t.isEmpty() && index < 10) out[index++] = t.removeFirst();
        }

        return out;
    }

    // Finds the edit distance between two words
    public int editDistance(String typed, String word) {
        // Array to hold all the edit distances (with padding)
        int[][] tabulation = new int[typed.length() + 1][word.length() + 1];

        // Base case
        for (int i = 0; i < tabulation.length; i++) {
            tabulation[i][0] = i;
        }

        // Base case
        for (int i = 0; i < tabulation[0].length; i++) {
            tabulation[0][i] = i;
        }

        for (int i = 1; i < tabulation.length; i++) {
            for (int j = 1; j < tabulation[0].length; j++) {
                // If they have teh same letter than look upper left diagonal
                if (typed.charAt(i - 1) == word.charAt(j - 1)) {
                    tabulation[i][j] = tabulation[i - 1][j - 1];
                }
                // Otherwise look at the minimum of upper and left neighbors
                else {
                    tabulation[i][j] = Math.min(tabulation[i][j - 1], tabulation[i - 1][j]);
                    tabulation[i][j] = 1 + Math.min(tabulation[i - 1][j - 1], tabulation[i][j]);
                }
            }
        }

        return tabulation[tabulation.length - 1][tabulation[0].length - 1];
    }


    /**
     * Loads a dictionary of words from the provided textfiles in the dictionaries directory.
     *
     * @param dictionary The name of the textfile, [dictionary].txt, in the dictionaries directory.
     * @return An array of Strings containing all words in alphabetical order.
     */
    private static String[] loadDictionary(String dictionary) {
        try {
            String line;
            BufferedReader dictReader = new BufferedReader(new FileReader("dictionaries/" + dictionary + ".txt"));
            line = dictReader.readLine();

            // Update instance variables with test data
            int n = Integer.parseInt(line);
            String[] words = new String[n];

            for (int i = 0; i < n; i++) {
                line = dictReader.readLine();
                words[i] = line;
            }
            return words;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Autocorrect a = new Autocorrect(loadDictionary("large"), 0);
        Scanner s = new Scanner(System.in);
        // Runs forever until a stop
        while (true) {
            System.out.print("Enter word: ");
            String typed = s.nextLine();
            System.out.println();
            System.out.println("---");
            System.out.println();

            if (a.isWord(typed)) System.out.println("Congrats! Your word is, well, a word.");

            else if (typed.length() == 1) System.out.println("Bruh. Enter a longer word.");

                // Runs if word is misspelled and prints out autocorrected results
            else if (!typed.isEmpty()) {
                String[] correct;
                a.chooseCandidates(typed);
                correct = a.runTest(typed);
                if (correct == null) System.out.println("Try Again.");
                else {
                    for (String x : correct) {
                        System.out.println(x);
                    }
                }
            }
            // Trolling to prevent people from trying to take advantage of my code
            // (and people shouldn't be using this for one-letter words anyway)
            else System.out.println("Bruh. Enter a word.");
            System.out.println();
            System.out.println("*****");
            System.out.println();
        }
    }
}