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
    }

    protected RevealGame(RevealGame copy) {
        super(copy);
        durationPerReveal = copy.durationPerReveal;
    }

    private final int durationPerReveal;

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
        int maxAmount = Math.max(1, (int) Math.round(answer.length()/4.0));

        final int initialPoints = calculatePoints(getCurrentWord().word);
        currentPoints = initialPoints;

        currentDisplay = reveal(currentDisplay, answer, maxAmount);

        WbsMessage message = plugin.buildMessage("Guess the word! \"")
                .appendRaw(currentDisplay)
                    .setFormatting("&h")
                .append("\" ("
                        + GameController.pointsDisplay(currentPoints) + ")")
                .build();
        broadcastQuestion(message);

        revealTaskId = new BukkitRunnable() {

            int revealedSoFar = maxAmount;

            @Override
            public void run() {
                int amount = (int) Math.floor(Math.random() * maxAmount) + 1;
                String amountDisplay;
                if (amount == 1) {
                    amountDisplay = amount + " more letter";
                } else {
                    amountDisplay = amount + " more letters";
                }
                revealedSoFar += amount;

                currentDisplay = reveal(currentDisplay, answer, amount);

                if (currentDisplay.equals(getCurrentWord().word)) {
                    GameController.endRoundNoWinner(true);
                } else {
                    double fractionRevealed = revealedSoFar / (double) answer.length();
                    currentPoints = Math.max(1, (int) ((initialPoints - 1) * (1 - fractionRevealed)) + 1);

                    WbsMessage message = plugin.buildMessage(amountDisplay + "! \"")
                            .appendRaw(currentDisplay)
                                .setFormatting("&h")
                            .append("\" ("
                                    + GameController.pointsDisplay(currentPoints) + ")")
                            .build();
                    broadcastQuestion(message);
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
