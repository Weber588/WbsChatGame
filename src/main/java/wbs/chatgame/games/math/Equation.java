package wbs.chatgame.games.math;

import org.jetbrains.annotations.NotNull;
import wbs.chatgame.WbsChatGame;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Equation implements Solveable {

    private final List<Operator> operators = new LinkedList<>();
    private final List<Solveable> values = new LinkedList<>();

    /**
     * Given an operator index, remove that operator and reduce the two corresponding
     * values either side of it to the operator's result, returning the value
     * from the individual calculation.
     * @param operatorIndex The index of the operator.
     * @param operators A list of operators to alter.
     * @param values A list of values to alter.
     * @return The value
     */
    private Solution solveAndReduce(int operatorIndex, List<Operator> operators, List<Solveable> values) {
        Solution sol1 = values.get(operatorIndex).solve();
        Solution sol2 = values.get(operatorIndex + 1).solve();

        double val1 = sol1.value();
        double val2 = sol2.value();
        Operator operator = operators.get(operatorIndex);

        Solution solution = operator.operate(val1, val2);

        operators.remove(operatorIndex);
        values.remove(operatorIndex);
        values.set(operatorIndex, new Value(solution.value()));

        return new Solution(solution.value(), solution.points() + sol1.points() + sol2.points());
    }

    public void addOperator(Operator operator) {
        operators.add(operator);
    }

    public void addValue(Solveable value) {
        values.add(value);
    }
    public void addValue(double value) {
        values.add(new Value(value));
    }

    @Override
    public Solution solve() {
        if (operators.size() != values.size() - 1) {
            throw new IllegalStateException("There must be n - 1 operators for n values");
        }

        final List<Operator> operators = new LinkedList<>(this.operators);
        final List<Solveable> values = new LinkedList<>(this.values);

        int points = 0;

        // Relies on order of declaration to determine which order they should be
        // processed in.
        for (Operator operator : Operator.values()) {
            while (operators.contains(operator)) {
                if (WbsChatGame.getInstance().settings.debugMode) {
                    Equation tempEquation = new Equation()
                            .addAllOperators(operators)
                            .addAllValues(values);

                    WbsChatGame.getInstance().logger.info(tempEquation.toString());
                }

                int index = operators.indexOf(operator);
                points += solveAndReduce(index, operators, values).points();
            }
        }

        return new Solution(values.get(0).solve().value(), Math.max(1, points));
    }

    @NotNull
    public Equation addAllOperators(Collection<Operator> operators) {
        this.operators.addAll(operators);
        return this;
    }

    @NotNull
    public Equation addAllValues(Collection<Solveable> values) {
        this.values.addAll(values);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder equationString = new StringBuilder(values.get(0) + "");
        for (int i = 0; i < operators.size(); i++) {
            equationString.append(" ")
                    .append(operators.get(i))
                    .append(" ");

            Solveable solveable = values.get(i + 1);
            if (solveable instanceof Equation) {
                equationString.append('(').append(solveable).append(')');
            } else {
                equationString.append(solveable);
            }
        }
        return equationString.toString();
    }
}
