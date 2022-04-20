package wbs.chatgame.games.math.functions;

import wbs.chatgame.games.math.Solvable;

public class RoundFunction extends CGFunction {
    public RoundFunction(Solvable operand) {
        super(operand);
    }

    @Override
    public double operateOn(double value) {
        return Math.round(value);
    }

    @Override
    protected int getDefaultPoints(double val) {
        return 0;
    }
}
