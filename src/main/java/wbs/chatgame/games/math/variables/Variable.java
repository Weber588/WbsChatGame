package wbs.chatgame.games.math.variables;

import org.jetbrains.annotations.NotNull;

public abstract class Variable {

    protected final String name;
    public Variable(String name) {
        this.name = name;
    }

    @NotNull
    public abstract String getValue();

    public String name() {
        return name;
    }
}
