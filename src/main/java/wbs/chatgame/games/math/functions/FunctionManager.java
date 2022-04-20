package wbs.chatgame.games.math.functions;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.WordUtil;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.math.ConditionalPointsCalculator;
import wbs.chatgame.games.math.Solvable;
import wbs.chatgame.games.math.operators.Operator;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class FunctionManager {
    private FunctionManager() {}

    private static final Map<String, Class<? extends CGFunction>> functions = new HashMap<>();

    public static void registerFunction(String name, Class<? extends CGFunction> function) {
        functions.put(WordUtil.stripSyntax(name), function);

        new CGFunction(null) {
            @Override
            public double operateOn(double value) {
                return 0;
            }

            @Override
            protected int getDefaultPoints(double val) {
                return 0;
            }
        };
    }

    static {
        registerFunction("log", LogFunction.class);
        registerFunction("abs", AbsFunction.class);
        registerFunction("round", RoundFunction.class);
        registerFunction("round", RoundFunction.class);
    }

    public static boolean isRegistered(String name) {
        return functions.containsKey(WordUtil.stripSyntax(name));
    }

    @Nullable
    public static CGFunction getFunction(String name, Solvable solvable, @Nullable ConditionalPointsCalculator pointsCalculator) {
        Class<? extends CGFunction> functionClazz = functions.get(WordUtil.stripSyntax(name));

        if (functionClazz == null) {
            return null;
        }

        CGFunction function;
        try {
            Constructor<? extends CGFunction> constructor = functionClazz.getConstructor(Solvable.class);
            function = constructor.newInstance(solvable);
        } catch (SecurityException | NoSuchMethodException | InstantiationException
                | IllegalAccessException | IllegalArgumentException e) {
            WbsChatGame.getInstance().settings.logError("Invalid constructor for registered function " + name, "Unknown");
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e){
            Throwable cause = e.getCause();
            if (!(cause instanceof InvalidConfigurationException)) {
                WbsChatGame.getInstance().settings.logError("An error occurred while constructing a function of type " + name, "Unknown");
                e.printStackTrace();
            }
            return null;
        }

        function.setPointsCalculator(pointsCalculator);

        return function;
    }

    @Nullable
    public static String getRegistrationName(Class<? extends CGFunction> function) {
        for (String name : functions.keySet()) {
            if (functions.get(name).equals(function)) {
                return name;
            }
        }

        return null;
    }

    public static Collection<Class<? extends CGFunction>> getFunctions() {
        return Collections.unmodifiableCollection(functions.values());
    }

    public static Class<? extends CGFunction> getFunctionClass(String name) {
        return functions.get(WordUtil.stripSyntax(name));
    }
}
