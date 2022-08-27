package wbs.chatgame.games.math.operators;

import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.WordUtil;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class OperatorManager {
    private static final Map<String, Class<? extends Operator>> operators = new LinkedHashMap<>();
    private static final String defaultOperator;

    public static Operator getDefaultOperator() {
        return getOperator(defaultOperator);
    }

    public static void registerOperation(String asString, String name, Class<? extends Operator> operator) {
        operators.put(WordUtil.stripSyntax(asString), operator);
        operators.put(WordUtil.stripSyntax(name), operator);
    }

    static {
        registerOperation("^", "exponentiation", ExponentiationOperator.class);
        registerOperation("*", "multiplication", MultiplicationOperator.class);
        registerOperation("/", "division", DivisionOperator.class);
        registerOperation("%", "modulo", ModuloOperator.class);
        registerOperation("-", "subtraction", SubtractionOperator.class);
        registerOperation("+", "addition", AdditionOperator.class);

        defaultOperator = "+";
    }

    public static Collection<Class<? extends Operator>> getOperators() {
        return Collections.unmodifiableCollection(operators.values());
    }

    @Nullable
    public static Operator getOperator(String asString) {
        Class<? extends Operator> operatorClass = operators.get(WordUtil.stripSyntax(asString));

        if (operatorClass == null) {
            return null;
        }

        return getOperator(asString, operatorClass);
    }

    private static Operator getOperator(String name, Class<? extends Operator> operatorClass) {
        Operator operator;
        try {
            Constructor<? extends Operator> constructor = operatorClass.getConstructor();
            operator = constructor.newInstance();
        } catch (SecurityException | NoSuchMethodException | InstantiationException
                | IllegalAccessException | IllegalArgumentException e) {
            WbsChatGame.getInstance().settings.logError("Invalid constructor for registered operator " + name, "Unknown");
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e){
            Throwable cause = e.getCause();
            if (!(cause instanceof InvalidConfigurationException)) {
                WbsChatGame.getInstance().settings.logError("An error occurred while constructing an operator of type " + name, "Unknown");
                e.printStackTrace();
            }
            return null;
        }

        return operator;
    }

    @Nullable
    public static String getRegistrationName(Class<? extends Operator> operator) {
        for (String name : operators.keySet()) {
            if (operators.get(name).equals(operator)) {
                return name;
            }
        }

        return null;
    }

    public static Operator getOperator(Class<? extends Operator> operatorClass) {
        return getOperator(getRegistrationName(operatorClass), operatorClass);
    }

    public static Map<String, Class<? extends Operator>> getRegistrations() {
        return Collections.unmodifiableMap(operators);
    }
}
