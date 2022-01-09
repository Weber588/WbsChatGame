package wbs.chatgame.games.math;

import org.jetbrains.annotations.NotNull;

public abstract class Variable {

    protected final String name;
    public Variable(String name) {
        this.name = name;
    }

    @NotNull
    public abstract String getPlaceholder();

}
