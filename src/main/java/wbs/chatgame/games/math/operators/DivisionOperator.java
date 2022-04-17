package wbs.chatgame.games.math.operators;

public class DivisionOperator extends Operator {
    public DivisionOperator() {
        super("/", "DIVISION");
    }

    @Override
    public double operate(double val1, double val2) {
        return val1 / val2;
    }

    @Override
    public int getDefaultPoints(double val1, double val2) {
        if (val1 % val2 != 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
