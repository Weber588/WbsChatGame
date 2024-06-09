package wbs.chatgame.games.word;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.controller.GameMessenger;
import wbs.chatgame.games.GameQuestion;
import wbs.chatgame.games.QuestionGenerator;
import wbs.chatgame.games.word.generator.GeneratedWord;
import wbs.chatgame.games.word.generator.WordGenerator;
import wbs.utils.util.string.WbsStrings;

import java.util.Collections;
import java.util.List;

public abstract class WordGameQuestion extends GameQuestion {

    protected Word currentWord;
    protected String unformattedWord;

    public WordGameQuestion(QuestionGenerator<?> generator, Word word, int duration) {
        super(generator, duration);

        this.currentWord = word;
        unformattedWord = word.word;

        if (this.currentWord instanceof GeneratedWord generatedWord) {
            WordGenerator generator = generatedWord.getGenerator();

            String displayFormatted = formatWord((GeneratedWord) word);
            currentWord = new GeneratedWord(displayFormatted,
                    word.getPoints(),
                    generator,
                    generatedWord.getHint());
        }

        currentPoints = word.getPoints();
    }

    @Override
    public List<String> getExampleAnswers() {
        return Collections.singletonList(unformattedWord);
    }

    @Override
    protected void onRoundEnd(@Nullable Player winner, @Nullable String guess, @Nullable Double finalDuration) {
        if (winner == null) {
            GameMessenger.broadcast("Nobody got the word in time! The word was: &h" + unformattedWord);
        } else {
            GameMessenger.broadcast(winner.getName() + " won in " + GameController.getLastRoundStartedString() + "! The answer was: &h" + unformattedWord);
        }
    }

    @Override
    public boolean checkGuess(String guess, Player guesser) {
        return guess.equalsIgnoreCase(currentWord.word);
    }

}
