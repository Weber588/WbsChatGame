package wbs.chatgame.games.math;

import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.math.functions.CGFunction;
import wbs.chatgame.games.math.functions.FunctionManager;
import wbs.chatgame.games.math.functions.NegateFunction;
import wbs.chatgame.games.math.operators.Operator;
import wbs.chatgame.games.math.operators.SubtractionOperator;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.util.LinkedList;
import java.util.List;

public final class EquationUtils {
    private EquationUtils() {}

    private static final boolean debug = false;
    private static void debug(String message) {
        if (debug) {
            WbsChatGame.getInstance().logger.info(message);
        }
    }

    public static int getClosingBracket(String equationString, int start) {
        int depth = 1;

        int closing = -1;
        int index = start + 1;
        char current = equationString.charAt(index);

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

        return closing;
    }

    public static Equation parseEquation(String equationString, OperationSet operationSet) {
        // Get rid of white space for ease of detecting edges between numbers and operators
        equationString = equationString.replaceAll("\s", "");

        debug("Entering parseEquation(" + equationString + ")");

        List<Solvable> values = new LinkedList<>();
        List<Operator> operators = new LinkedList<>();

        // Track if the last added thing was a value or an operator, to prevent stuff like +* etc
        boolean lastAddedValue = false;

        // The function found in the previous pass, to be applied to the next found solvable.
        String previousFunctionName = null;

        // Is the next value negative?
        boolean negateNextValue = false;

        int index = 0;
        while (index < equationString.length()) {
            char current = equationString.charAt(index);
            debug("next char: " + current);

            // Find closing and recurse, skipping to closing bracket.
            if (current == '(') { // For my thinking: 5 + (3 + (2 + 5)) + (2 + 3)
                debug("Entering brackets: " + current);
                if (lastAddedValue) {
                    // Implicit multiplication
                    operators.add(operationSet.getOperator("*"));
                }

                int opening = index;
                int closing = getClosingBracket(equationString, index);

                index = closing + 1;

                // closing is always set unless an unbalanced error was thrown
                Equation subEquation = parseEquation(equationString.substring(opening + 1, closing), operationSet);

                Solvable nextValue;

                if (previousFunctionName != null) {
                    CGFunction function = operationSet.getFunction(previousFunctionName, subEquation);
                    if (function == null) {
                        nextValue = subEquation;
                    } else {
                        nextValue = function;
                        previousFunctionName = null;
                    }
                } else {
                    nextValue = subEquation;
                }

                if (negateNextValue) {
                    nextValue = new NegateFunction(nextValue);
                    negateNextValue = false;
                }

                values.add(nextValue);

                lastAddedValue = true;
                debug("Leaving brackets: " + current);
                continue; // Fine to continue - index has already been incremented when leaving the loop to find the closing parenthesis
            }

            if (current == ')') {
                throw new InvalidConfigurationException("Invalid (sub)equation: \"" + equationString + "\" at index " + index + " (\"" + current + "\"). Unbalanced parentheses.");
            }

            if (Character.isDigit(current)) {
                debug("Found digit: " + current);
                if (lastAddedValue) {
                    throw new InvalidConfigurationException("Invalid (sub)equation: \"" + equationString + "\" at index " + index + " (\"" + current + "\"). Values must be separated by an operator.");
                }

                StringBuilder valueString = new StringBuilder();
                valueString.append(current);

                int subindex = index + 1;

                boolean foundEndOfNumber = false;
                boolean foundDecimalPoint = false;
                while (subindex < equationString.length() && !foundEndOfNumber) {
                    current = equationString.charAt(subindex);

                    if (Character.isDigit(current)) {
                        valueString.append(current);
                    } else if (current == '.' && !foundDecimalPoint) {
                        valueString.append(current);
                        foundDecimalPoint = true;
                    } else {
                        foundEndOfNumber = true;
                    }

                    subindex++;
                }

                index += valueString.length() - 1;

                double value;
                try {
                    value = Double.parseDouble(valueString.toString());
                } catch (NumberFormatException e) {
                    throw new InvalidConfigurationException("Invalid (sub)equation: \"" + equationString + "\". Failed to parse \""
                            + valueString + "\" at index " + index + " (\"" + current + "\"). Please report this error.");
                }

                Solvable nextValue;

                if (previousFunctionName != null) {
                    CGFunction function = operationSet.getFunction(previousFunctionName, new Value(value));
                    if (function == null) {
                        nextValue = new Value(value);
                    } else {
                        nextValue = function;
                        previousFunctionName = null;
                    }
                } else {
                    nextValue = new Value(value);
                }

                if (negateNextValue) {
                    nextValue = new NegateFunction(nextValue);
                    negateNextValue = false;
                }

                values.add(nextValue);

                lastAddedValue = true;
            } else { // Not a digit
                debug("Found non-digit: " + current);
                StringBuilder operatorString = new StringBuilder();
                operatorString.append(current);

                int subindex = index + 1;

                if (subindex < equationString.length() && !Character.isDigit(equationString.charAt(subindex))) {
                    boolean foundEndOfOperator = false;
                    while (subindex < equationString.length() && !foundEndOfOperator) {
                        current = equationString.charAt(subindex);

                        if (!Character.isDigit(current)) {
                            if (current == '(') {
                                foundEndOfOperator = true;
                            } else {
                                operatorString.append(current);
                            }
                        } else {
                            foundEndOfOperator = true;
                        }

                        subindex++;
                    }

                    index += operatorString.length() - 1;
                }

                Operator operator = operationSet.getOperator(operatorString.toString());
                if (operator == null) {
                    for (int i = 0; i < operatorString.length(); i++) {
                        operator = operationSet.getOperator(operatorString.substring(0, i));
                        if (operator != null) {
                            index -= operatorString.length() - i;
                            break;
                        }
                    }
                }

                if (operator == null) {
                    if (current == '(' && FunctionManager.isRegistered(operatorString.toString())) {
                        previousFunctionName = operatorString.toString();
                    } else {
                        throw new InvalidConfigurationException("Invalid (sub)equation: \"" + equationString + "\" at index " + index + " (\"" + current + "\"). Invalid operator/function: \"" + operatorString + "\".");
                    }
                }

                if (operator != null) {
                    if (!lastAddedValue) { // If last added operator
                        if (operator.getClass().equals(SubtractionOperator.class)) {
                            negateNextValue = true;
                            operator = null;
                        } else {
                            throw new InvalidConfigurationException("Invalid (sub)equation: \"" + equationString + "\" at index " + index + " (\"" + current + "\"). Operators must be split by values.");
                        }
                    }
                }

                // Functions aren't operators, so don't change the lastAddedValue flag
                if (previousFunctionName == null && operator != null) {
                    lastAddedValue = false;
                    operators.add(operator);
                }
            }

            index++;
        }

        Equation toReturn = new Equation(operationSet);
        for (Operator operator : operators) {
            toReturn.addOperator(operator);
        }
        for (Solvable value : values) {
            toReturn.addValue(value);
        }

        debug("Generated equation: " + toReturn);

        return toReturn;
    }
}
