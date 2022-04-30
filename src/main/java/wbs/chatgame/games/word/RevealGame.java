package wbs.chatgame.games.word;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.chatgame.GameController;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.challenges.Challenge;
import wbs.utils.util.plugin.WbsMessage;

public class RevealGame extends WordGame {
    public RevealGame(String gameName, ConfigurationSection section, String directory) {
        super(gameName, section, directory);

        durationPerReveal = (int) (section.getDouble("duration-per-reveal", getDuration() * 20 / 5.0) * 20);
        numberOfReveals = (getDuration() / (double) durationPerReveal) + 1; // +1 since the last reveal ends the round
    }

    protected RevealGame(RevealGame copy) {
        super(copy);
        durationPerReveal = copy.durationPerReveal;
        numberOfReveals = copy.numberOfReveals;
    }

    private final int durationPerReveal;
    // Store as a double for precision with multiple divisions
    private final double numberOfReveals;

    private int revealTaskId = -1;
    private String currentDisplay;

    @Override
    protected Game startGame(Word wordToGuess) {
        currentDisplay = conceal(wordToGuess.word);
        startRevealTimer();
        return this;
    }

    private void startRevealTimer() {
        String answer = getCurrentWord().word;

        int numOfSpaces = answer.length() - answer.replaceAll(" ", "").length();
        int lenWithoutSpaces = answer.length() - numOfSpaces;

        int amount = (int) Math.max(1, lenWithoutSpaces / numberOfReveals);

        // How many letters were remaining after the int divide to calculate amount
        int missing = lenWithoutSpaces - (int) (amount * numberOfReveals);

        int firstAmount = amount;
        if (missing > 0) {
            firstAmount++;
            missing--;
        }

        final int initialPoints = calculatePoints(getCurrentWord().word);
        currentPoints = initialPoints;

        currentDisplay = reveal(currentDisplay, answer, firstAmount);

        WbsMessage message = plugin.buildMessage("Guess the word! \"")
                .appendRaw(currentDisplay)
                    .setFormatting("&h")
                .append("\" ("
                        + GameController.pointsDisplay(currentPoints) + ")")
                .build();
        broadcastQuestion(message);

        final int initialMissing = missing;
        revealTaskId = new BukkitRunnable() {

            int revealedSoFar = amount;
            int revealsSoFar = 1; // First message counts as a reveal, start at 1

            int missing = initialMissing;

            @Override
            public void run() {
                int amountThisRound = amount;

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
                    double fractionRevealed = revealedSoFar / (double) lenWithoutSpaces;
                    currentPoints = Math.max(1, (int) Math.floor((initialPoints) * (1 - fractionRevealed)) + 1);

                    WbsMessage message = plugin.buildMessage(amountDisplay + "! \"")
                            .appendRaw(currentDisplay)
                                .setFormatting("&h")
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
