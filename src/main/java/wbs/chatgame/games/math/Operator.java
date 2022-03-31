package wbs.chatgame.games.math;

/**
 * Defines mathematical operators that accept two values and return a {@link Solution},
 * which defines how many points that solution is worth.
 * <br/>
 * The order of declaration defines the order of operations, where operations defined
 * earlier are processed first.
 */
public enum Operator {
    EXPONENTIATION('^'),
    MULTIPLICATION('*'),
    DIVISION('/'),
    SUBTRACTION('-'),
    ADDITION('+'),
    ;

    final char operator;
    Operator(char operator) {
        this.operator = operator;
    }

    public Solution operate(double val1, double val2) {
        return switch (this) {
            case EXPONENTIATION -> {
                if (val1 != 1 && val2 != 1) {
                    yield new Solution(Math.pow(val1, val2),
                            (int) (Math.ceil(val2) + (val1 / 4)));
                } else {
                    yield new Solution(val1, 0);
                }
            }
            case ADDITION ->
                    new Solution(val1 + val2,
                            val1 > 100 && val2 > 100
                                    ? 1 : 0);
            case SUBTRACTION ->
                    new Solution(val1 - val2,
                            val2 > 25 && val2 > val1
                                    ? 1 : 0);
            case MULTIPLICATION ->
                    new Solution(val1 * val2,
                            (val1 > 6 && val2 > 6) || val1 + val2 > 10
                                    ? (int) Math.log10(val1 * val2) : 0);
            case DIVISION ->
                    new Solution(val1 / val2,
                            val1 % val2 != 0
                                    ? 1 : 0);
        };
    }

    public static Operator fromChar(char match) {
        return switch (match) {
            case '*' -> MULTIPLICATION;
            case '/' -> DIVISION;
            case '+' -> ADDITION;
            case '-' -> SUBTRACTION;
            case '^' -> EXPONENTIATION;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return String.valueOf(operator);
    }
}
