package wbs.chatgame.games.math.functions;

import wbs.chatgame.games.math.Solvable;

public class NegateFunction extends CGFunction {
    public NegateFunction(Solvable operand) {
        super(operand);
    }

    @Override
    public double operateOn(double value) {
        return -value;
    }

    @Override
    protected int getDefaultPoints(double val) {
        return 0;
    }
}
