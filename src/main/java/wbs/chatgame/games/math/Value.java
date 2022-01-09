package wbs.chatgame.games.math;

/**
 * Wrapper class for a double (Solution) to make equations easier to solve
 */
public class Value implements Solveable {

    private final Solution value;

    public Value(double value) {
        this.value = new Solution(value, 0);
    }

    @Override
    public Solution solve() {
        return value;
    }
}
