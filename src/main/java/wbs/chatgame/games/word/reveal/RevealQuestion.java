package wbs.chatgame.games.word.reveal;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.games.word.Word;
import wbs.chatgame.games.word.WordGameQuestion;
import wbs.utils.util.WbsMath;
import wbs.utils.util.plugin.WbsMessage;

public class RevealQuestion extends WordGameQuestion<RevealGame> {

    private String currentText;
    private int revealTaskId = -1;

    public RevealQuestion(RevealGame parent, Word word, int duration) {
        super(parent, word, duration);
    }

    @Override
    public void start() {
        currentText = conceal(currentWord.word);
        startRevealTimer();
    }

    private void startRevealTimer() {
        String answer = currentWord.word;

        int numOfSpaces = answer.length() - answer.replaceAll(" ", "").length();
        int lenWithoutSpaces = answer.length() - numOfSpaces;

        int amountToReveal = (int) Math.ceil(lenWithoutSpaces * getFractionToReveal());

        int amountPerReveal = (int) Math.max(1, amountToReveal / getNumberOfReveals());

        // How many letters were remaining after the int divide to calculate amount
        int remaining = amountToReveal - (int) (amountPerReveal * getNumberOfReveals());

        int firstAmount = amountPerReveal;
        if (remaining > 0) {
            firstAmount++;
            remaining--;
        }

        final int initialPoints = parent.calculatePoints(currentWord.word);

        currentText = reveal(currentText, answer, firstAmount);

        WbsMessage message = plugin.buildMessage("Guess the word! \"")
                .appendRaw(currentText)
                .setFormatting("&h")
                .append("\" ("
                        + GameController.pointsDisplay(currentPoints) + ")")
                .build();
        broadcastQuestion(message);

        final int initialMissing = remaining;
        revealTaskId = new BukkitRunnable() {

            int revealedSoFar = amountPerReveal;
            int revealsSoFar = 1; // First message counts as a reveal, start at 1

            int missing = initialMissing;

            final int finalPoints = (int) Math.floor(initialPoints * getFinalFractionOfPoints());

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

                currentText = reveal(currentText, answer, amountThisRound);

                if (currentText.equals(currentWord.word) || revealsSoFar >= (int) getNumberOfReveals()) {
                    GameController.endRoundNoWinner(true);
                } else {
                    double fractionRevealed = revealedSoFar / (double) amountToReveal;

                    double pointsAsDouble = WbsMath.lerp(initialPoints, finalPoints, fractionRevealed);

                    currentPoints = Math.max(1, (int) Math.round(pointsAsDouble));

                    WbsMessage message = plugin.buildMessage(amountDisplay + "! \"")
                            .appendRaw(currentText)
                            .setFormatting("&h")
                            .append("\" ("
                                    + GameController.pointsDisplay(currentPoints) + ")")
                            .build();
                    broadcastQuestion(message);
                    revealsSoFar++;
                }
            }
        }.runTaskTimer(plugin, getDurationPerReveal(), getDurationPerReveal()).getTaskId();
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
    protected void onRoundEnd(@Nullable Player winner, @Nullable String guess, @Nullable Double finalDuration) {
        super.onRoundEnd(winner, guess, finalDuration);

        if (revealTaskId != -1) {
            Bukkit.getScheduler().cancelTask(revealTaskId);
            revealTaskId = -1;
        }
        currentDisplay = null;
    }

    protected int getDurationPerReveal() {
        return parent.durationPerReveal;
    }

    protected double getNumberOfReveals() {
        return parent.numberOfReveals;
    }

    protected double getFinalFractionOfPoints() {
        return parent.finalFractionOfPoints;
    }

    protected double getFractionToReveal() {
        return parent.fractionToReveal;
    }
}
