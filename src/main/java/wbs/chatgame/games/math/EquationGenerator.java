package wbs.chatgame.games.math;

import org.bukkit.configuration.ConfigurationSection;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.WbsChatGame;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.utils.util.providers.generator.num.RandomGenerator;

import java.util.*;
import java.util.regex.Pattern;

public class EquationGenerator {

    public EquationGenerator(ConfigurationSection section, String directory) throws InvalidConfigurationException {
        ChatGameSettings settings = WbsChatGame.getInstance().settings;

        generatorName = section.getName();

        equationString = section.getString("equation");

        if (equationString == null) {
            settings.logError("Equation is a required field.", directory + "/equation");
            throw new InvalidConfigurationException("Equation is a required field.");
        }

        template = section.getString("template", equationString);
        customEquation = !template.equalsIgnoreCase(equationString);

        randomOperators.addAll(section.getStringList("random-operators"));

        ConfigurationSection variablesSection = section.getConfigurationSection("variables");
        if (variablesSection != null) {
            for (String key : variablesSection.getKeys(false)) {
                ConfigurationSection varSection = variablesSection.getConfigurationSection(key);

                Variable var;
                if (varSection == null) {
                    if (variablesSection.isDouble(key)) {
                        var = new StaticNumVariable(key, variablesSection.getDouble(key));
                    } else {
                        var = new StringVariable(variablesSection, key);
                    }
                } else {
                    var = new RandomNumVariable(
                            key,
                            new RandomGenerator(varSection, WbsChatGame.getInstance().settings, directory)
                    );
                }

                variables.add(var);
            }
        }
    }

    public EquationGenerator(List<String> options) {
        generatorName = "Custom";

        equationString = String.join(" ", options);
        template = equationString;
        customEquation = false;
    }

    private final String generatorName;
    private final String equationString;
    private final String template;
    private final boolean customEquation;
    private final Set<String> randomOperators = new HashSet<>();
    private final LinkedList<Variable> variables = new LinkedList<>();

    private record EquationTemplatePair(String filledEquation, String filledTemplate) {}

    private EquationTemplatePair fillPlaceholders() {
        Map<String, String> placeholders = new HashMap<>();
        for (Variable var : variables) {
            placeholders.put(var.name, var.getPlaceholder());
        }

        String filledEquation = equationString;
        String filledTemplate = template;

        for (Variable var : variables) {
            String placeholder = placeholders.get(var.name);

            filledTemplate = filledTemplate.replaceAll(
                    Pattern.quote('%' + var.name + '%'),
                    placeholder);

            filledEquation = filledEquation.replaceAll(
                    Pattern.quote('%' + var.name + '%'),
                    placeholder);
        }

        Operator defaultOperator = Operator.ADDITION;
        if (randomOperators.isEmpty()) {
            randomOperators.add(defaultOperator.operator + "");
        }
        while (filledEquation.contains("?")) {
            String chosenOperator = WbsCollectionUtil.getRandom(randomOperators);
            filledEquation = filledEquation.replaceFirst("\\?", chosenOperator);
            filledTemplate = filledTemplate.replaceFirst("\\?", chosenOperator);
        }

        return new EquationTemplatePair(filledEquation, filledTemplate);
    }

    private Equation getEquation(String equationString) throws InvalidConfigurationException {
        // Get rid of white space for ease of detecting edges between numbers and operators
        equationString = equationString.replaceAll("\s", "");

        List<Solveable> values = new LinkedList<>();
        List<Operator> operators = new LinkedList<>();

        // Track if the last added thing was a value or an operator, to prevent stuff like +* etc
        boolean lastAddedValue = false;

        // Iterate over characters in equation string
        int index = 0;
        while (index < equationString.length()) {
            char current = equationString.charAt(index);

            // Find closing and recurse, skipping to closing bracket.
            if (current == '(') { // For my thinking: 5 + (3 + (2 + 5)) + (2 + 3)

                if (lastAddedValue) {
                    // TODO: Allow implicit multiplication?
                    throw new InvalidConfigurationException("Invalid (sub)equation: \"" + equationString + "\" at index " + index + " (" + current + "). Values must be separated by an operator.");
                }

                index++; // Skip opening bracket
                int depth = 1;

                int opening = index;
                int closing = -1;
                while (closing == -1) {
                    if (index >= equationString.length()) {
                        throw new InvalidConfigurationException("Invalid (sub)equation: \"" + equationString + "\" at index " + index + " (" + current + "). Unbalanced parentheses.");
                    }

                    current = equationString.charAt(index);

                    if (current == '(') {
                        depth++;
                    } else if (current == ')') {
                        depth--;

                        if (depth == 0) {
                            closing = index;
                        }
                    }

                    // Skipping the sub-equation anyway, so fine to use the main index here.
                    index++;
                }

                // closing is always set unless an unbalanced error was thrown
                Equation subEquation = getEquation(equationString.substring(opening, closing));

                values.add(subEquation);
                lastAddedValue = true;
                continue; // Fine to continue - index has already been incremented when leaving the loop to find the closing parenthesis
            }

            if (current == ')') {
                throw new InvalidConfigurationException("Invalid (sub)equation: \"" + equationString + "\" at index " + index + " (" + current + "). Unbalanced parentheses.");
            }

            Operator operator = Operator.fromChar(current);

            if (operator != null) {
                if (!lastAddedValue) { // If last added operator
                    // TODO: Add check for SUBTRACT, to account for negative numbers
                    throw new InvalidConfigurationException("Invalid (sub)equation: \"" + equationString + "\" at index " + index + " (" + current + "). Operators must be split by values.");
                }
                operators.add(operator);
                lastAddedValue = false;
            } else { // Operator was null - therefore this is either a number or an invalid character.
                if (lastAddedValue) {
                    throw new InvalidConfigurationException("Invalid (sub)equation: \"" + equationString + "\" at index " + index + " (" + current + "). Values must be separated by an operator.");
                }

                // Not allowing decimals; just check for numbers
                if (!Character.isDigit(current)) {
                    throw new InvalidConfigurationException("Invalid (sub)equation: \"" + equationString + "\". Number expected; found \"" + current + "\".");
                }

                StringBuilder valueString = new StringBuilder();

                int subindex = index + 1;

                if (subindex < equationString.length() && Character.isDigit(equationString.charAt(subindex))) {
                    boolean foundEndOfNumber = false;
                    while (subindex < equationString.length() && !foundEndOfNumber) {
                        current = equationString.charAt(subindex);

                        if (Character.isDigit(current)) {
                            valueString.append(current);
                        } else {
                            foundEndOfNumber = true;
                        }

                        subindex++;
                    }

                    index += valueString.length();
                } else {
                    valueString.append(current);
                }

                int value;
                try {
                    // Fine to just use index as substring omits the endIndex
                    value = Integer.parseInt(valueString.toString());
                } catch (NumberFormatException e) {
                    throw new InvalidConfigurationException("Invalid (sub)equation: \"" + equationString + "\". Failed to parse \""
                            + valueString + "\" at index " + index + " (" + current + "). Please report this error.");
                }

                values.add(new Value(value));

                lastAddedValue = true;
            }

            index++;
        }

        Equation toReturn = new Equation();
        for (Operator operator : operators) {
            toReturn.addOperator(operator);
        }
        for (Solveable value : values) {
            toReturn.addValue(value);
        }

        return toReturn;
    }

    public ViewableEquation getEquation() throws InvalidConfigurationException {
        EquationTemplatePair pair = fillPlaceholders();

        return new ViewableEquation(getEquation(pair.filledEquation), pair.filledTemplate, customEquation);
    }

    public String getGeneratorName() {
        return generatorName;
    }
}
