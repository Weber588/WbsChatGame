package wbs.chatgame.games.word.generator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.games.word.Word;

public class GeneratedWord extends Word {

    @Nullable
    private final String hint;
    private boolean isFormatted = false;

    public GeneratedWord(String word, @NotNull WordGenerator generator) {
        super(word, generator);
        this.hint = generator.getGenericHint();
    }

    public GeneratedWord(String word, @NotNull WordGenerator generator, boolean isFormatted) {
        this(word, generator);
        this.isFormatted = isFormatted;
    }

    public GeneratedWord(String word, @NotNull WordGenerator generator, @Nullable String hint) {
        super(word, generator);
        this.hint = hint;
    }

    public GeneratedWord(String word, @NotNull WordGenerator generator, @Nullable String hint, boolean isFormatted) {
        this(word, generator, hint);
        this.isFormatted = isFormatted;
    }

    @Nullable
    public String getHint() {
        return hint;
    }

    public boolean isFormatted() {
        return isFormatted;
    }

    @NotNull
    public WordGenerator getGenerator() {
        assert generator != null;
        return generator;
    }
}
