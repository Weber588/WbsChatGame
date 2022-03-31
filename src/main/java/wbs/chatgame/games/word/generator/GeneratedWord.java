package wbs.chatgame.games.word.generator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.games.word.Word;

public class GeneratedWord extends Word {

    @NotNull
    private final String hint;

    public GeneratedWord(String word, @NotNull WordGenerator generator, @Nullable String hint) {
        super(word, generator);

        if (hint == null) {
            this.hint = generator.getGenericHint() ;
        } else {
            this.hint = hint;
        }
    }

    @NotNull
    public String getHint() {
        return hint;
    }
}
