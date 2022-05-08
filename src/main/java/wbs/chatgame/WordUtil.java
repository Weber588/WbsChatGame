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
        return Math.max(1,
                (int) Math.round(
                        Math.log(word.length() / 2.5) / Math.log(2) // log_2(length/3)
                )
        );
    }

    public static String scrambleString(String input) {
        if (input.length() <= 1) {
            return input;
        }

        char[] letters = input.toCharArray();

        boolean hasVariedCharacters = false;
        char lastChar = letters[0];

        List<Character> lettersList = new ArrayList<>();
        for (char letter : letters) {
            lettersList.add(letter);
            if (lastChar != letter) {
                hasVariedCharacters = true;
            }
        }

        // The string is only one character - don't try shuffling
        if (!hasVariedCharacters) {
            return input;
        }

        String output;

        do {
            Collections.shuffle(lettersList);
            int index = 0;
            for (char letter : lettersList) {
                letters[index] = letter;
                index++;
            }
            output = new String(letters);
        } while (output.equals(input)); // Don't need an escape as we verified that it can be varied above

        return output;
    }

    public static String reverseString(String input) {
        int length = input.length();
        char[] oldLetters = input.toCharArray();
        char[] newLetters = new char[length];
        for (int i = 0; i < length; i++) {
            newLetters[i] = oldLetters[length - i - 1];
        }

        return new String(newLetters);
    }
}
