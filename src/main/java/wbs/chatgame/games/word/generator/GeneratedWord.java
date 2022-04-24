package wbs.chatgame.games.word.generator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.games.word.Word;

public class GeneratedWord extends Word {

    @Nullable
    private final String hint;

    public GeneratedWord(String word, @NotNull WordGenerator generator) {
        super(word, generator);
        this.hint = generator.getGenericHint();
    }

    public GeneratedWord(String word, @NotNull WordGenerator generator, @Nullable String hint) {
        super(word, generator);
        this.hint = hint;
    }

    @Nullable
    public String getHint() {
        return hint;
    }
}
