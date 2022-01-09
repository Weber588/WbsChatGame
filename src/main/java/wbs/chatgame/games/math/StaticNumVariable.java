package wbs.chatgame.games.math;

import org.jetbrains.annotations.NotNull;

public class StaticNumVariable extends Variable {

    private final double value;

    public StaticNumVariable(String name, double value) {
        super(name);
        this.value = value;
    }

    @Override
    public @NotNull String getPlaceholder() {
        return value + "";
    }
}
