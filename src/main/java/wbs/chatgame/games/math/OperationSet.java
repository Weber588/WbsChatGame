package wbs.chatgame.games.math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WordUtil;
import wbs.chatgame.games.math.functions.CGFunction;
import wbs.chatgame.games.math.functions.FunctionManager;
import wbs.chatgame.games.math.operators.AdditionOperator;
import wbs.chatgame.games.math.operators.Operator;
import wbs.chatgame.games.math.operators.OperatorManager;

import java.util.*;

/**
 * Represents a set of operations with point functions that are valid in an equation.
 */
public class OperationSet {
    private static final OperationSet defaultSet =
            new OperationSet().populateDefaultOperations()
                    .populateDefaultFunctions();

    public static OperationSet getDefaultSet() {
        return defaultSet;
    }

    public OperationSet populateDefaultOperations() {
        for (Class<? extends Operator> operatorClass : OperatorManager.getOperators()) {
            addOperator(OperatorManager.getOperator(operatorClass), null);
        }

        return this;
    }

    public OperationSet populateDefaultFunctions() {
        for (Class<? extends CGFunction> function : FunctionManager.getFunctions()) {
            registerFunction(function, null);
        }

        return this;
    }

    private final List<Operator> operators = new LinkedList<>();

    private Operator defaultOperator = new AdditionOperator();

    public Operator getDefaultOperator() {
        return defaultOperator;
    }

    public void setDefaultOperator(Operator defaultOperator) {
        this.defaultOperator = defaultOperator;
    }

    public List<Operator> getOperators() {
        return Collections.unmodifiableList(operators);
    }

    @Nullable
    public Operator getOperator(String asString) {
        for (Operator operator : operators) {
            if (operator.asString().equalsIgnoreCase(asString)) {
                return operator;
            }
        }

        return null;
    }

    public void addOperator(@NotNull Operator operator, @Nullable ConditionalPointsCalculator pointsCalculator) {
        operators.add(operator);
        operator.setPointsCalculator(pointsCalculator);
    }





    private final Map<String, Class<? extends CGFunction>> functions = new HashMap<>();
    private final Map<String, ConditionalPointsCalculator> functionPointCalculators = new HashMap<>();

    public void registerFunction(Class<? extends CGFunction> function, ConditionalPointsCalculator pointsCalculator) {
        String name = FunctionManager.getRegistrationName(function);

        if (name == null) {
            throw new IllegalArgumentException("Invalid function for operation set: " + function.getCanonicalName());
        }

        functions.put(name, function);
        functionPointCalculators.put(name, pointsCalculator);
    }

    public boolean isRegistered(String name) {
        return functions.containsKey(WordUtil.stripSyntax(name));
    }

    @Nullable
    public CGFunction getFunction(String name, Solvable solvable) {
        ConditionalPointsCalculator pointsCalculator = functionPointCalculators.get(WordUtil.stripSyntax(name));

        return FunctionManager.getFunction(name, solvable, pointsCalculator);
    }
}
