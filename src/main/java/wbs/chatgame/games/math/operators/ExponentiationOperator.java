package wbs.chatgame.games.math.operators;

public class ExponentiationOperator extends Operator {
    public ExponentiationOperator() {
        super("^", "EXPONENTIATION");
    }

    @Override
    public double operate(double val1, double val2) {
        return Math.pow(val1, val2);
    }

    @Override
    public int getDefaultPoints(double val1, double val2) {
        if (val1 != 1 && val2 != 1) {
            return (int) (Math.ceil(val2) + (val1 / 4));
        } else {
            return 0;
        }
    }
}
