package wbs.chatgame.games.math.operators;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.games.math.GameOperation;
import wbs.chatgame.games.math.ConditionalPointsCalculator;
import wbs.chatgame.games.math.OperationSet;
import wbs.chatgame.games.math.Solution;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines mathematical operators that accept two values and return a {@link Solution},
 * which defines how many points that solution is worth.
 * <br/>
 * The order of declaration defines the order of operations, where operations defined
 * earlier are processed first.
 */
public abstract class Operator implements GameOperation {
    private final String asString;
    private final String name;
    @Nullable
    private ConditionalPointsCalculator pointsFunction;

    public Operator(String asString, String name) {
        this.asString = asString;
        this.name = name;
    }

    public abstract double operate(double val1, double val2);

    @NotNull
    public final Solution solve(double val1, double val2) {
        return new Solution(operate(val1, val2), getPoints(val1, val2));
    }

    public void setPointsCalculator(ConditionalPointsCalculator pointsFunction) {
        this.pointsFunction = pointsFunction;
    }

    public String asString() {
        return asString;
    }

    public String getName() {
        return name;
    }

    public final int getPoints(double val1, double val2) {
        if (pointsFunction != null) {
            Map<String, Double> placeholders = new HashMap<>();
            placeholders.put("val1", val1);
            placeholders.put("val2", val2);
            return pointsFunction.getPoints(placeholders, OperationSet.getDefaultSet());
        } else {
            return getDefaultPoints(val1, val2);
        }
    }

    public abstract int getDefaultPoints(double val1, double val2);

    @Override
    public String toString() {
        return asString;
    }
}
