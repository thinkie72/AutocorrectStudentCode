import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 * @author Zach Blick
 * @author Tyler Hinkie
 */
public class Autocorrect {
    private String[] dictionary;
    private int threshold;

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */
    public Autocorrect(String[] words, int threshold) {
        dictionary = words;
        this.threshold = threshold;
    }

    public boolean isWord(String typed) {
        // Create a Trie for the dictionary
        Trie dict = new Trie();
        // Insert each word in the dictionary into the trie version
        for (String word : dictionary) dict.insert(word);

        return dict.lookup(typed);
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
                arr[index] = t.remove(0);
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
        while (true) {
            System.out.print("Enter word: ");
            String typed = s.nextLine();
            System.out.println();
            System.out.println("---");
            System.out.println();
            if (a.isWord(typed)) System.out.println("Congrats! Your word is, well, a word.");
            else {
                String[] correct = a.runTest(typed);
                for (String x : correct) {
                    System.out.println(x);
                }
            }
            System.out.println();
        }
    }
}