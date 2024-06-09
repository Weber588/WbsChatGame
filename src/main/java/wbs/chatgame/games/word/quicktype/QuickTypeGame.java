package wbs.chatgame.games.word.quicktype;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.WordUtil;
import wbs.chatgame.games.QuestionGenerator;
import wbs.chatgame.games.challenges.ChallengeManager;
import wbs.chatgame.games.challenges.quicktype.QuickTypeBackwards;
import wbs.chatgame.games.challenges.quicktype.QuickTypeHover;
import wbs.chatgame.games.word.Word;
import wbs.chatgame.games.word.WordGame;
import wbs.chatgame.games.word.WordGameQuestion;

public class QuickTypeGame extends WordGame<QuickTypeGame> {
    @SuppressWarnings("unused") // Accessed reflectively
    public QuickTypeGame(String gameName, ConfigurationSection section, String directory) {
        super(gameName, section, directory);

        scramble = section.getBoolean("scramble", true);
        matchCase = section.getBoolean("match-case", true);
    }

    @Override
    protected QuickTypeGame getThis() {
        return this;
    }

    @Override
    protected @NotNull QuestionGenerator<QuickTypeGame> getDefaultGenerator() {
        return null;
    }

    public QuickTypeGame(QuickTypeGame copy) {
        super(copy);

        scramble = copy.scramble;
        matchCase = copy.matchCase;
    }

    final boolean scramble;
    final boolean matchCase;

    @Override
    protected @NotNull WordGameQuestion generateQuestion(Word wordToGuess) {
        return new QuickTypeQuestion(this, wordToGuess, getDuration());
    }


    @Override
    protected Word generateWord() {
        return conditionalScramble(super.generateWord());
    }

    @Override
    protected Word getCustomWord() {
        return conditionalScramble(super.getCustomWord());
    }

    private Word conditionalScramble(Word word) {
        if (scramble) {
            String scrambled = WordUtil.scrambleString(word.word);
            return new Word(scrambled, word.getPoints());
        } else {
            return word;
        }
    }

    @Override
    protected int calculateDefaultPoints(String word) {
        return Math.max(1,
                (int) Math.round(
                        Math.log(word.length() / 3.0) / Math.log(2) // log_2(length/3)
                )
        );
    }

    @Override
    public void registerChallenges() {
        super.registerChallenges();
        ChallengeManager.buildAndRegisterChallenge("backwards", this, QuickTypeBackwards.class);
        ChallengeManager.buildAndRegisterChallenge("hover", this, QuickTypeHover.class);
    }
}
