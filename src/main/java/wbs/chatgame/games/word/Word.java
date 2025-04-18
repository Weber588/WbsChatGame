package wbs.chatgame.games.word;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.games.word.generator.GeneratedWord;

import java.util.Objects;

public class Word {
    @NotNull
    public final String word;
    private final int points;
    protected final boolean isFormatted;

    public Word(@NotNull String word, int points) {
        this(word, points, false);
    }

    public Word(@NotNull String word, int points, boolean isFormatted) {
        Validate.notNull(word, "Word must not be null.");

        this.word = word;
        this.points = points;
        this.isFormatted = isFormatted;
    }

    public int getPoints() {
        return points;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Word) obj;
        return Objects.equals(this.word, that.word) &&
                this.points == that.points;
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, points);
    }

    @Override
    public String toString() {
        return word;
    }

    public boolean isFormatted() {
        return isFormatted;
    }

    public Word setPoints(int points) {
        return new Word(word, points, isFormatted);
    }
}
