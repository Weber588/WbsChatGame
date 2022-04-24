package wbs.chatgame.games.math;

public interface Solvable {
    Solution solve(boolean withPoints);

    String toString(boolean romanNumerals);
    String toRomanNumeralString();
}
