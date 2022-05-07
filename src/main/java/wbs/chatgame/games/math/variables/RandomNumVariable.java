package wbs.chatgame.games.math.variables;

import org.jetbrains.annotations.NotNull;
import wbs.utils.util.providers.generator.num.RandomGenerator;

public class RandomNumVariable extends Variable {

    private final RandomGenerator randomGenerator;
    public RandomNumVariable(String name, RandomGenerator randomGenerator) {
        super(name);
        this.randomGenerator = randomGenerator;
    }

    @Override
    public @NotNull String getValue() {
        randomGenerator.refresh();
        return (int) randomGenerator.getValue() + "";
    }
}
