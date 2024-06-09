package wbs.chatgame.games.word.reveal;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.games.word.Word;
import wbs.chatgame.games.word.WordGame;
import wbs.chatgame.games.word.WordGameQuestion;

public class RevealGame extends WordGame<RevealGame> {
    @SuppressWarnings("unused") // Accessed reflectively
    public RevealGame(String gameName, ConfigurationSection section, String directory) {
        super(gameName, section, directory);

        double defaultPerReveal = getDuration() / 5.0;
        int durationPerReveal = (int) (section.getDouble("duration-per-reveal", defaultPerReveal / 20.0) * 20);
        if (durationPerReveal <= 0) {
            settings.logError("Duration per reveal must be positive: " + durationPerReveal, directory + "/duration-per-reveal");
            durationPerReveal = (int) defaultPerReveal;
        }

        this.durationPerReveal = durationPerReveal;

        numberOfReveals = (getDuration() / (double) durationPerReveal); // +1 since the last reveal ends the round

        fractionToReveal = section.getDouble("percent-to-reveal", 100) / 100.0;
        finalFractionOfPoints = section.getDouble("final-percent-of-points", 0) / 100.0;
    }

    // How long should there be between each round?
    final int durationPerReveal;
    // Store as a double for precision with multiple divisions
    final double numberOfReveals;

    // How much of the word should be revealed?
    final double fractionToReveal;
    // On the last round, what fraction of the initial points should the question be worth?
    final double finalFractionOfPoints;

    @Override
    protected @NotNull WordGameQuestion generateQuestion(Word wordToGuess) {
        return new RevealQuestion(this, wordToGuess, getDuration());
    }

    @Override
    protected int calculateDefaultPoints(String word) {
        return Math.max(1, (word.length()/4));
    }
}
