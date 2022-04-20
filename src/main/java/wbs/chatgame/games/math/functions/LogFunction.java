package wbs.chatgame.games.math.functions;

import wbs.chatgame.games.math.Solvable;

public class LogFunction extends CGFunction {
    public LogFunction(Solvable operand) {
        super(operand);
    }

    @Override
    public double operateOn(double operand) {
        if (operand > 0) {
            return Math.log10(operand);
        } else {
            return 0;
        }
    }

    @Override
    protected int getDefaultPoints(double val) {
        return 0;
    }
}
