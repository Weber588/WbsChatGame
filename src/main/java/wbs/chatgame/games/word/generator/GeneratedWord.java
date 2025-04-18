package wbs.chatgame.games.word.generator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.games.word.Word;

public class GeneratedWord extends Word {
    @Nullable
    private final String hint;
    private final WordGenerator generator;

    public GeneratedWord(@NotNull String word, int points, @NotNull WordGenerator generator) {
        super(word, points);
        this.generator = generator;
        this.hint = generator.getGenericHint();
    }

    public GeneratedWord(@NotNull String word, int points, WordGenerator generator, boolean isFormatted) {
        super(word, points, isFormatted);
        this.generator = generator;
        this.hint = generator.getGenericHint();
    }

    public GeneratedWord(@NotNull String word, int points, @NotNull WordGenerator generator, @Nullable String hint, boolean isFormatted) {
        super(word, points, isFormatted);
        this.generator = generator;
        this.hint = hint;
    }

    public GeneratedWord(@NotNull String word, int points, WordGenerator generator, @Nullable String hint) {
        super(word, points);
        this.generator = generator;
        this.hint = hint;
    }


    @Nullable
    public String getHint() {
        return hint;
    }

    @NotNull
    public WordGenerator getGenerator() {
        assert generator != null;
        return generator;
    }

    @Override
    public GeneratedWord setPoints(int points) {
        return new GeneratedWord(word, points, generator, hint, isFormatted);
    }
}
