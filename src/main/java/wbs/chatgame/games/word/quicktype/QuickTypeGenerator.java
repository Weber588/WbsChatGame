package wbs.chatgame.games.word.quicktype;

import org.jetbrains.annotations.NotNull;
import wbs.chatgame.games.word.Word;
import wbs.chatgame.games.word.WordGameGenerator;
import wbs.chatgame.games.word.WordGameQuestion;

public class QuickTypeGenerator extends WordGameGenerator<QuickTypeGame> {
    public QuickTypeGenerator(QuickTypeGame parent) {
        super(parent);
    }

    @Override
    protected @NotNull WordGameQuestion generateQuestion(Word wordToGuess) {
        return null;
    }

    @Override
    protected int calculateDefaultPoints(String word) {
        return 0;
    }
}
