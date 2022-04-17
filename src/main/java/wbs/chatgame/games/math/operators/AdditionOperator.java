package wbs.chatgame.games.math.operators;

public class AdditionOperator extends Operator {
    public AdditionOperator() {
        super("+", "ADDITION");
    }

    @Override
    public double operate(double val1, double val2) {
        return val1 + val2;
    }

    @Override
    public int getDefaultPoints(double val1, double val2) {
        if (val1 > 100 && val2 > 100) {
            return 1;
        } else {
            return 0;
        }
    }
}
