package wbs.chatgame.games.math;

import org.jetbrains.annotations.NotNull;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.math.operators.Operator;
import wbs.chatgame.games.math.operators.OperatorManager;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Equation implements Solvable {

    private final List<Operator> operators = new LinkedList<>();
    private final List<Solvable> values = new LinkedList<>();

    private final OperationSet operationSet;

    public Equation(OperationSet operationSet) {
        this.operationSet = operationSet;
    }

    /**
     * Given an operator index, remove that operator and reduce the two corresponding
     * values either side of it to the operator's result, returning the value
     * from the individual calculation.
     * @param operatorIndex The index of the operator.
     * @param operators A list of operators to alter.
     * @param values A list of values to alter.
     * @return The value
     */
    private Solution solveAndReduce(int operatorIndex, List<Operator> operators, List<Solvable> values, boolean withPoints) {
        Solution sol1 = values.get(operatorIndex).solve(withPoints);
        Solution sol2 = values.get(operatorIndex + 1).solve(withPoints);

        double val1 = sol1.value();
        double val2 = sol2.value();
        Operator operator = operators.get(operatorIndex);

        Solution solution;
        if (withPoints) {
            solution = operator.solve(val1, val2);
        } else {
            double value = operator.operate(val1, val2);
            solution = new Solution(value, 0);
        }

        operators.remove(operatorIndex);
        values.remove(operatorIndex);
        values.set(operatorIndex, new Value(solution.value()));

        return new Solution(solution.value(), solution.points() + sol1.points() + sol2.points());
    }

    public Equation addOperator(Operator operator) {
        operators.add(operator);
        return this;
    }

    public Equation addValue(Solvable value) {
        values.add(value);
        return this;
    }
    public Equation addValue(double value) {
        values.add(new Value(value));
        return this;
    }

    @Override
    public Solution solve(boolean withPoints) {
        if (operators.size() != values.size() - 1) {
            throw new IllegalStateException("There must be n - 1 operators for n values");
        }

        final List<Operator> operators = new LinkedList<>(this.operators);
        final List<Solvable> values = new LinkedList<>(this.values);

        int points = 0;

        // Relies on order of definition to determine which order they should be
        // processed in.
        for (Operator operator : operationSet.getOperators()) {
            while (operators.contains(operator)) {
                if (WbsChatGame.getInstance().settings.debugMode) {
                    Equation tempEquation = new Equation(operationSet)
                            .addAllOperators(operators)
                            .addAllValues(values);

                    WbsChatGame.getInstance().logger.info(tempEquation.toString());
                }

                int index = operators.indexOf(operator);
                points += solveAndReduce(index, operators, values, withPoints).points();
            }
        }

        return new Solution(values.get(0).solve(withPoints).value(), Math.max(1, points));
    }

    @NotNull
    public Equation addAllOperators(Collection<Operator> operators) {
        this.operators.addAll(operators);
        return this;
    }

    @NotNull
    public Equation addAllValues(Collection<Solvable> values) {
        this.values.addAll(values);
        return this;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public String toString(boolean romanNumerals) {
        StringBuilder equationString = new StringBuilder(values.get(0).toString(romanNumerals) + "");
        for (int i = 0; i < operators.size(); i++) {
            equationString.append(" ")
                    .append(operators.get(i))
                    .append(" ");

            Solvable solvable = values.get(i + 1);
            if (solvable instanceof Equation) {
                equationString.append('(').append(solvable.toString(romanNumerals)).append(')');
            } else {
                equationString.append(solvable.toString(romanNumerals));
            }
        }
        return equationString.toString();
    }


    @Override
    public String toRomanNumeralString() {
        return toString(true);
    }
}
