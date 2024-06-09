package wbs.chatgame.games;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class QuestionGenerator<T extends Game> {
    protected final T parent;
    private final String id;

    public QuestionGenerator(T parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @Nullable
    public abstract GameQuestion generateQuestion();

    public Game getParent() {
        return parent;
    }

    /**
     * Start this game with a given set of options.
     * @param options The options to use.
     * @return The game itself, or null if the game failed to start.
     * @throws IllegalArgumentException If the options were invalid
     */
    public GameQuestion generateWithOptions(List<String> options) {
        return generateQuestion();
    }

    /**
     * Returns if the challenge is valid at the time of being called. If not, the game will be re-rolled.
     * <br/>
     * This allows challenges with preconditions to auto-skip, such as if they require a certain number of
     * players to be online, or require certain variables to be populated in the game controller.
     * @return True if the challenge may be run.
     */
    public boolean valid() {
        return true;
    }
}
