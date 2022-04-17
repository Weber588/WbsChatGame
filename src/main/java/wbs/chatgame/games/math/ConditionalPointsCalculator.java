package wbs.chatgame.games.math;

import org.bukkit.configuration.ConfigurationSection;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.math.variables.StaticNumVariable;
import wbs.chatgame.games.math.variables.Variable;

import java.util.*;
import java.util.regex.Pattern;

public class ConditionalPointsCalculator {
    private final EquationGenerator equationGenerator;
    private final Set<PointCondition> conditions = new HashSet<>();

    public ConditionalPointsCalculator(EquationGenerator equationGenerator) {
        this.equationGenerator = equationGenerator;
    }

    public ConditionalPointsCalculator(ConfigurationSection section, String directory) {
        String pointEquation = section.getString("points", "0");

        equationGenerator = new EquationGenerator(pointEquation);

        List<String> conditionStrings = section.getStringList("points-conditions");

        for (String conditionString : conditionStrings) {
            PointCondition pointCondition = null;
            for (EquationComparator comparator : EquationComparator.values()) {
                String[] strings = conditionString.split(Pattern.quote(comparator.asString));

                if (strings.length == 2) {
                    pointCondition = new PointCondition(strings[0], comparator, strings[1]);
                    break;
                }
            }

            if (pointCondition == null) {
                WbsChatGame.getInstance().settings.logError(
                        "No comparator found in point condition \"" + conditionString + "\"",
                        directory + "/points-conditions");
                continue;
            }

            addCondition(pointCondition);
        }
    }

    public void addCondition(PointCondition condition) {
        conditions.add(condition);
    }

    public int getPoints(Map<String, Double> placeholders, OperationSet set) {
        for (PointCondition condition : conditions) {
            if (!condition.evaluate(placeholders, set)) {
                return 0;
            }
        }

        for (String placeholder : placeholders.keySet()) {
            Variable variable = new StaticNumVariable(placeholder, placeholders.get(placeholder));

            equationGenerator.setVariable(variable);
        }

        ViewableEquation equation = equationGenerator.getEquation(set);

        return (int) equation.equation().solve(false).value();
    }

    private enum EquationComparator {
        // Order is important; longer strings should be declared earlier, to allow
        // string splitting without catching substrings first.
        NOT_EQUALS("!="),
        GREATER_OR_EQUAL(">="),
        LESS_OR_EQUAL("<="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        EQUALS("="),
        ;

        private final String asString;

        EquationComparator(String asString) {
            this.asString = asString;
        }

        public boolean compare(double val1, double val2) {
            WbsChatGame.getInstance().logger.info("Comparing: " + val1 + " to " + val2);
            return switch (this) {
                case NOT_EQUALS -> val1 != val2;
                case GREATER_OR_EQUAL -> val1 >= val2;
                case LESS_OR_EQUAL -> val1 <= val2;
                case GREATER_THAN -> val1 > val2;
                case LESS_THAN -> val1 < val2;
                case EQUALS -> val1 == val2;
            };
        }

        @Override
        public String toString() {
            return asString;
        }
    }

    public record PointCondition(String side1,
                                 EquationComparator comparator,
                                 String side2) {

        public boolean evaluate(Map<String, Double> placeholders, OperationSet set) {
            EquationGenerator generator1 = new EquationGenerator(side1);
            EquationGenerator generator2 = new EquationGenerator(side2);

            for (String placeholder : placeholders.keySet()) {
                Variable variable = new StaticNumVariable(placeholder, placeholders.get(placeholder));

                generator1.setVariable(variable);
                generator2.setVariable(variable);
            }

            double val1 = generator1.getEquation(set).equation().solve(false).value();
            double val2 = generator2.getEquation(set).equation().solve(false).value();

            return comparator.compare(val1, val2);
        }
    }
}
