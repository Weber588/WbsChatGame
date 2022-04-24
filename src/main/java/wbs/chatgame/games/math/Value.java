package wbs.chatgame.games.math;

import wbs.utils.util.string.RomanNumerals;
import wbs.utils.util.string.WbsStrings;

/**
 * Wrapper class for a double (Solution) to make equations easier to solve
 */
public class Value implements Solvable {

    private final Solution value;

    public Value(double value) {
        this.value = new Solution(value, 0);
    }

    @Override
    public Solution solve(boolean withPoints) {
        return value;
    }

    @Override
    public String toString(boolean romanNumerals) {
        if (romanNumerals) {
            return toRomanNumeralString();
        } else {
            return toString();
        }
    }

    @Override
    public String toRomanNumeralString() {
        int whole = (int) value.value();
        double decimal = (value.value() - whole);
        String decimalString = String.valueOf(decimal).substring(2);
        
        int decimalAsWhole = -1;
        try {
            decimalAsWhole = Integer.parseInt(decimalString);
        } catch (NumberFormatException ignored) {}

        if (decimalAsWhole <= 0) {
            return RomanNumerals.toRoman(whole);
        } else {
            return RomanNumerals.toRoman(whole) + "." + RomanNumerals.toRoman(decimalAsWhole);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value.value());
    }
}
