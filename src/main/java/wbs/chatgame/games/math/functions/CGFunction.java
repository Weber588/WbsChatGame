package wbs.chatgame.games.math.functions;

import org.jetbrains.annotations.Nullable;
import wbs.chatgame.games.math.*;

import java.util.HashMap;
import java.util.Map;

public abstract class CGFunction implements Solvable, GameOperation {
    private final Solvable operand;
    @Nullable
    private ConditionalPointsCalculator pointsFunction;

    public CGFunction(Solvable operand) {
        this.operand = operand;
    }

    public void setPointsCalculator(ConditionalPointsCalculator pointsFunction) {
        this.pointsFunction = pointsFunction;
    }

    @Override
    public final Solution solve(boolean withPoints) {
        Solution solved = operand.solve(withPoints);
        double value = operateOn(solved.value());
        return new Solution(value, solved.points() + getPoints(solved.value()));
    }

    public final int getPoints(double val) {
        if (pointsFunction != null) {
            Map<String, Double> placeholders = new HashMap<>();
            placeholders.put("val", val);
            return pointsFunction.getPoints(placeholders, OperationSet.getDefaultSet());
        } else {
            return getDefaultPoints(val);
        }
    }

    public abstract double operateOn(double value);

    protected abstract int getDefaultPoints(double val);

    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public String toString(boolean romanNumerals) {
        return FunctionManager.getRegistrationName(getClass()) + "(" + operand.toString(romanNumerals) + ")";
    }

    @Override
    public String toRomanNumeralString() {
        return toString(true);
    }
}
