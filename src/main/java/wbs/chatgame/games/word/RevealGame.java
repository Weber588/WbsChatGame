package wbs.chatgame.games.word;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.games.Game;
import wbs.utils.util.WbsMath;
import wbs.utils.util.plugin.WbsMessage;

public class RevealGame extends WordGame {
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

    protected RevealGame(RevealGame copy) {
        super(copy);
        durationPerReveal = copy.durationPerReveal;
        numberOfReveals = copy.numberOfReveals;
        fractionToReveal = copy.fractionToReveal;
        finalFractionOfPoints = copy.finalFractionOfPoints;
    }

    // How long should there be between each round?
    private final int durationPerReveal;
    // Store as a double for precision with multiple divisions
    private final double numberOfReveals;

    // How much of the word should be revealed?
    private final double fractionToReveal;
    // On the last round, what fraction of the initial points should the question be worth?
    private final double finalFractionOfPoints;

    private int revealTaskId = -1;
    private String currentDisplay;

    @Override
    @NotNull
    protected Game startGame(Word wordToGuess) {
        currentDisplay = conceal(wordToGuess.word);
        startRevealTimer();
        return this;
    }

    private void startRevealTimer() {
        String answer = getCurrentWord().word;

        int numOfSpaces = answer.length() - answer.replaceAll(" ", "").length();
        int lenWithoutSpaces = answer.length() - numOfSpaces;

        int amountToReveal = (int) Math.ceil(lenWithoutSpaces * fractionToReveal);

        int amountPerReveal = (int) Math.max(1, amountToReveal / numberOfReveals);

        // How many letters were remaining after the int divide to calculate amount
        int remaining = amountToReveal - (int) (amountPerReveal * numberOfReveals);

        int firstAmount = amountPerReveal;
        if (remaining > 0) {
            firstAmount++;
            remaining--;
        }

        final int initialPoints = calculatePoints(getCurrentWord().word);
        currentPoints = initialPoints;

        currentDisplay = reveal(currentDisplay, answer, firstAmount);

        WbsMessage message = plugin.buildMessage("Guess the word! \"")
                .append(Component.text(currentDisplay).color(plugin.getTextHighlightColour()))
                .append("\" ("
                        + GameController.pointsDisplay(currentPoints) + ")")
                .build();
        broadcastQuestion(message);

        final int initialMissing = remaining;
        revealTaskId = new BukkitRunnable() {

            int revealedSoFar = amountPerReveal;
            int revealsSoFar = 1; // First message counts as a reveal, start at 1

            int missing = initialMissing;

            final int finalPoints = (int) Math.floor(initialPoints * finalFractionOfPoints);

            @Override
            public void run() {
                int amountThisRound = amountPerReveal;

                // Only need to do 1 max, since missing is effectively
                // amount % numberOfReveals, and so will at most be
                // numberOfReveals - 1
                if (missing > 0) {
                    amountThisRound++;
                    missing--;
                }

                String amountDisplay;
                if (amountThisRound == 1) {
                    amountDisplay = amountThisRound + " more letter";
                } else {
                    amountDisplay = amountThisRound + " more letters";
                }
                revealedSoFar += amountThisRound;

                currentDisplay = reveal(currentDisplay, answer, amountThisRound);

                if (currentDisplay.equals(getCurrentWord().word) || revealsSoFar >= (int) numberOfReveals) {
                    GameController.endRoundNoWinner(true);
                } else {
                    double fractionRevealed = revealedSoFar / (double) amountToReveal;

                    double pointsAsDouble = WbsMath.lerp(initialPoints, finalPoints, fractionRevealed);

                    currentPoints = Math.max(1, (int) Math.round(pointsAsDouble));

                    WbsMessage message = plugin.buildMessage(amountDisplay + "! \"")
                            .append(Component.text(currentDisplay).color(plugin.getTextHighlightColour()))
                            .append("\" ("
                                    + GameController.pointsDisplay(currentPoints) + ")")
                            .build();
                    broadcastQuestion(message);
                    revealsSoFar++;
                }
            }
        }.runTaskTimer(plugin, durationPerReveal, durationPerReveal).getTaskId();
    }

    private String reveal(String current, String answer, int reveal) {
        char[] letters = current.toCharArray();
        char[] answerLetters = answer.toCharArray();

        for (int i = 0; i < reveal; i++) { // Replace #reveal underscores if found
            boolean found = false; // Whether or not the current string (char array) contains an underscore
            int index = 0;
            while (!found && index < letters.length) {
                if (letters[index] == '_') {
                    found = true;
                }
                index++;
            }
            if (found) {
                do {
                    index = (int) Math.ceil(Math.random() * letters.length)-1;
                } while (letters[index] != '_'); // Find an underscore

                letters[index] = answerLetters[index];
            }
        }

        return new String(letters);
    }

    private String conceal(String answer) {
        return answer.replaceAll("[^ ]", "_");
    }

    @Override
    public void endNoWinner() {
        super.endNoWinner();
        onEnd();
    }

    @Override
    public void endWinner(Player player, String guess) {
        super.endWinner(player, guess);
        onEnd();
    }

    private void onEnd() {
        if (revealTaskId != -1) {
            Bukkit.getScheduler().cancelTask(revealTaskId);
            revealTaskId = -1;
        }
        currentDisplay = null;
    }

    @Override
    protected int calculateDefaultPoints(String word) {
        return Math.max(1, (word.length()/4));
    }
}
