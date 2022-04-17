package wbs.chatgame.games.math.operators;

public class SubtractionOperator extends Operator {
    public SubtractionOperator() {
        super("-", "SUBTRACTION");
    }

    @Override
    public double operate(double val1, double val2) {
        return val1 - val2;
    }

    @Override
    public int getDefaultPoints(double val1, double val2) {
        if (val2 > 25 && val2 > val1) {
            return 1;
        } else {
            return 0;
        }
    }
}
