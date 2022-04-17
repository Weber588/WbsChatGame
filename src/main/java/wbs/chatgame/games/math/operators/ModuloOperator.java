package wbs.chatgame.games.math.operators;

public class ModuloOperator extends Operator {
    public ModuloOperator() {
        super("%", "MODULO");
    }

    @Override
    public double operate(double val1, double val2) {
        return val1 % val2;
    }

    @Override
    public int getDefaultPoints(double val1, double val2) {
        if (val1 < val2) {
            return 0;
        }

        if (Math.abs(val1 - val2) < val2 / 4) {
            return 0;
        }

        return (int) (val1 / val2) / 4;
    }
}
