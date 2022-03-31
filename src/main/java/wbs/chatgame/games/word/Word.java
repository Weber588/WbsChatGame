package wbs.chatgame.games.word;

import org.jetbrains.annotations.Nullable;
import wbs.chatgame.games.word.generator.WordGenerator;

import java.util.Objects;

public class Word {
    public final String word;
    private int points;
    @Nullable
    public final WordGenerator generator;

    public Word(String word, int points, @Nullable WordGenerator generator) {
        this.word = word;
        this.points = points;
        this.generator = generator;
    }

    public Word(String word, @Nullable WordGenerator generator) {
        this.word = word;
        this.generator = generator;
    }

    public Word setPoints(int points) {
        this.points = points;
        return this;
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
                Objects.equals(this.generator, that.generator) &&
                this.points == that.points;
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, points, generator);
    }

    @Override
    public String toString() {
        return word;
    }

}
