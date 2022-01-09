package wbs.chatgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WordUtil {
    private WordUtil() {}

    public static String stripSyntax(String toStrip) {
        return toStrip.replace(" ", "")
                .replace("-", "")
                .replace("_", "")
                .toLowerCase();
    }

    public static int scramblePoints(String word) {
        int points = Math.max(1,
                (int) Math.round(
                        Math.log(word.length() / 2.0) / Math.log(2) // log_2(length/2)
                )
        );

        if (word.contains(" ")) {
            points++;
        }
        return points;
    }

    public static String scrambleString(String input) {
        char[] letters = input.toCharArray();

        List<Character> lettersList = new ArrayList<>();
        for (char letter : letters) {
            lettersList.add(letter);
        }
        String output;

        // If a word has n+2 spaces where n is the number of non-space characters, it will always have 2 spaces in a row.
        int escape = 0;
        do {
            escape++;
            Collections.shuffle(lettersList);
            int index = 0;
            for (char letter : lettersList) {
                letters[index] = letter;
                index++;
            }
            output = new String(letters);

        } while ((output.contains("  ") || output.toLowerCase().equalsIgnoreCase(input)) && escape < 100);

        // if escape is 100 here, it was likely impossible to make the string have no consecutive spaces, just return the string

        return output;
    }
}
