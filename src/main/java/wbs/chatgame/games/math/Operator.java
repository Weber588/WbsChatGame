package wbs.chatgame.games.math;

public enum Operator {
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
            default -> null;
        };
    }
}
