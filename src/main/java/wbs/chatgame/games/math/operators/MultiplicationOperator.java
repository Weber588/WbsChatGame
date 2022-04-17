package wbs.chatgame.games.math.operators;

public class MultiplicationOperator extends Operator {
    public MultiplicationOperator() {
        super("*", "MULTIPLICATION");
    }

    @Override
    public double operate(double val1, double val2) {
        return val1 * val2;
    }

    @Override
    public int getDefaultPoints(double val1, double val2) {
        if ((val1 > 6 && val2 > 6) || val1 + val2 > 10) {
            double log = Math.log10(Math.abs(val1 * val2));
            if (Double.isNaN(log) || Double.isInfinite(log)) {
                return 0;
            } else {
                return (int) log;
            }
        } else {
            return 0;
        }
    }
}
