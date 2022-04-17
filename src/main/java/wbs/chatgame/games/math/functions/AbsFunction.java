package wbs.chatgame.games.math.functions;

import wbs.chatgame.games.math.Solvable;

public class AbsFunction extends CGFunction {
    public AbsFunction(Solvable operand) {
        super(operand);
    }

    @Override
    public double operateOn(double value) {
        return Math.abs(value);
    }

    @Override
    protected int getDefaultPoints(double val) {
        return 0;
    }
}
