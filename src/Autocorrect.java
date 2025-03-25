import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
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
    private Trie dict;
    private ArrayList<String>[][] candidates;

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     */
    public Autocorrect(String[] words) {

        candidates = new ArrayList[LONGEST_WORD][MAX_TWO_GRAMS];

        for (int i = 0; i < LONGEST_WORD; i++) {
            for (int j = 0; j < MAX_TWO_GRAMS; j++) {
                candidates[i][j] = new ArrayList<>();
            }
        }
        int length;
        for (String word : words) {
            length = word.length();
            for (int i = 0; i < length - 2; i++) {
                candidates[length - 1][hash(word.substring(i, i + 2))].add(word);
            }
        }

        int twoGram;

        // Create a Trie for the dictionary
        Trie dict = new Trie();
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

    public boolean isWord(String typed) {
        return dict.lookup(typed);
    }

    public ArrayList<String> chooseCandidates(String typed) {
        int length = typed.length();
        length--;
        ArrayList<String> out = new ArrayList<>();

        int twoGram;

        if (length > 1 && length < 43) {
            for (int i = 0; i < length; i++) {
                twoGram = hash(typed.substring(i, i + 2));
                out.addAll(candidates[length][twoGram]);
                for (int j = 1; j <= 2; j++) {
                    out.addAll(candidates[length - i][twoGram]);
                    out.addAll(candidates[length + i][twoGram]);
                }
            }
        } else {
            for (ArrayList<String> a : candidates[length]) out.addAll(a);
        }
        return out.toString();
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        ArrayList<String>[] test = new ArrayList[threshold];

        for (int i = 0; i < test.length; i++) {
            test[i] = new ArrayList<String>();
        }

        int ed;
        int arrLen = 0;
        for (String word : dictionary) {
            ed = editDistance(typed, word);
            if (ed <= threshold) {
                test[ed - 1].add(word);
                arrLen++;
            }
        }

        String[] arr = new String[arrLen];
        int index = 0;
        for (ArrayList<String> t : test) {
            // Maybe: sort t alphabetically here
            Collections.sort(t);
            while (!t.isEmpty()) {
                arr[index] = t.removeFirst();
                index++;
            }
        }

        return arr;
    }

    public int editDistance(String typed, String word) {
        int[][] tabulation = new int[typed.length() + 1][word.length() + 1];
        for (int i = 0; i < tabulation.length; i++) {
            tabulation[i][0] = i;
        }

        for (int i = 0; i < tabulation[0].length; i++) {
            tabulation[0][i] = i;
        }

        for (int i = 1; i < tabulation.length; i++) {
            for (int j = 1; j < tabulation[0].length; j++) {
                if (typed.charAt(i - 1) == word.charAt(j - 1)) {
                    tabulation[i][j] = tabulation[i - 1][j - 1];
                } else {
                    tabulation[i][j] = Math.min(tabulation[i][j - 1], tabulation[i - 1][j]);
                    tabulation[i][j] = 1 + Math.min(tabulation[i - 1][j - 1], tabulation[i][j]);
                }
            }
        }
        return tabulation[tabulation.length - 1][tabulation[0].length - 1];
    }


    /**
     * Loads a dictionary of words from the provided textfiles in the dictionaries directory.
     * @param dictionary The name of the textfile, [dictionary].txt, in the dictionaries directory.
     * @return An array of Strings containing all words in alphabetical order.
     */
    private static String[] loadDictionary(String dictionary)  {
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
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Autocorrect a = new Autocorrect(loadDictionary("large"), 2);
        Scanner s = new Scanner(System.in);
        a.makeDict();
        a.makeCandidates();
        while (true) {
            System.out.print("Enter word: ");
            String typed = s.nextLine();
            System.out.println();
            System.out.println("---");
            System.out.println();
            if (a.isWord(typed)) System.out.println("Congrats! Your word is, well, a word.");
            else if (!typed.isEmpty()){
                String[] correct;
                if (typed.length() == 1) correct = a.runTest(typed);
                else correct = a.chooseCandidates(typed);
                for (String x : correct) {
                    System.out.println(x);
                }
            } else System.out.println("Bruh. Enter a word.");
            System.out.println();
        }
    }
}