package wbs.chatgame.controller;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.data.ChatGameDB;
import wbs.chatgame.games.Game;
import wbs.chatgame.rewards.RewardManager;
import wbs.utils.util.string.WbsStringify;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.function.Consumer;

public class GameController {

    private static WbsChatGame plugin;

    public static void setPlugin(WbsChatGame plugin) {
        GameController.plugin = plugin;
    }

    // Configured info
    public static int roundDelay = 180 * 20;

    // Round info
    private static Game currentGame;
    private static int roundRunnableId = -1;
    private static int betweenRoundsRunnableId = -1;

    private static Player lastWinner;
    private static Game lastGame;

    private static GameQueue gameQueue = new GameQueue();

    /**
     * The start of the current phase, whether it's the start of a round,
     * or the start of the time between rounds (i.e. end of the last round)
     */
    private static LocalDateTime phaseStartTime;
    // If stopped by command, this is set, which prevents certain start events (like joining)
    private static boolean forceStopped;

    /**
     * Start the game if it's not already running.
     * @return True if the game started, false otherwise
     */
    public static boolean start() {
        if (isRunning()) {
            return false;
        }

        plugin.logger.info("Game has started.");
        forceStopped = false;
        startRound();
        return true;
    }

    /**
     * Stop the game if it's running.
     * @return True if the game stopped,
     */
    public static boolean stop() {
        if (!isRunning()) {
            return false;
        }

        plugin.logger.info("Game has stopped.");
        endRoundNoWinner(false);
        cancelTasks();
        return true;
    }

    public static boolean isRunning() {
        return roundRunnableId != -1 || betweenRoundsRunnableId != -1;
    }

    // ==================== //
    //   Round management   //
    // ==================== //

    private static void startRound() {
        phaseStartTime = LocalDateTime.now();
        cancelTasks();

        if (GameMessenger.getListeningPlayers().size() == 0) {
            return;
        }

        currentGame = gameQueue.startNext();

        roundRunnableId = new BukkitRunnable() {
            @Override
            public void run() {
                endRoundNoWinner(true);
            }
        }.runTaskLater(plugin, currentGame.getDuration()).getTaskId();
    }

    /**
     * Skip the current round if there is one, and then immediately start the next round.
     */
    public static void skip() {
        if (currentGame != null) {
            endRoundNoWinner(false);
        }
        cancelTasks();

        startRound();
    }

    /**
     * Cause the round to end without a winner, due to skipping,
     * game stopping, or running out of time.
     * @return Whether or not there was a round running.
     */
    public static synchronized boolean endRoundNoWinner(boolean startNext) {
        if (currentGame == null) {
            return false;
        }

        lastWinner = null;

        currentGame.endNoWinner();

        onRoundEnd(startNext);
        return true;
    }

    public static boolean endRoundWithWinner(Player winner, String guess) {
        if (currentGame == null) {
            return false;
        }

        final int points = currentGame.getPoints();
        final Game finalGame = currentGame;

        lastWinner = winner;
        currentGame.endWinner(winner, guess);

        Duration duration = Duration.between(phaseStartTime, LocalDateTime.now());
        long millis = duration.toMillis();
        double speed = millis / (double) 1000;

        onRoundEnd(true);

        ChatGameDB.getPlayerManager().getAsync(winner.getUniqueId(), record -> {
            RewardManager.giveRewards(record, points);
            record.addPoints(points, finalGame);
            record.registerTime(speed, finalGame);

            // TODO: Make it configurable, to save as batches or save after each win.
            ChatGameDB.getPlayerManager().saveAsync(Collections.singletonList(record));
        });
        return true;
    }

    private static void onRoundEnd(boolean startNext) {
        phaseStartTime = LocalDateTime.now();

        lastGame = currentGame;
        currentGame = null;
        cancelTasks();

        if (startNext) {
            betweenRoundsRunnableId = new BukkitRunnable() {
                @Override
                public void run() {
                    startRound();
                }
            }.runTaskLater(plugin, roundDelay).getTaskId();
        }
    }

    private static void cancelTasks() {
        if (betweenRoundsRunnableId != -1) {
            Bukkit.getScheduler().cancelTask(betweenRoundsRunnableId);
            betweenRoundsRunnableId = -1;
        }

        if (roundRunnableId != -1) {
            Bukkit.getScheduler().cancelTask(roundRunnableId);
            roundRunnableId = -1;
        }
    }
    /**
     * Make a guess for the given player.
     * Runs on the main thread, and invokes a given runnable if the round ended before the guess was processed.
     * This prevents double winning, or winning after the round ended after guessing on an async thread.
     * @param guesser The player guessing.
     * @param guess The guess they're making.
     * @param callback A consumer that accepts a boolean for if the player won.
     * @param alreadyWon A runnable, called if the round ended before the guess was processed.
     */
    public static void guess(Player guesser, String guess, Consumer<Boolean> callback, Runnable alreadyWon) {
        if (isRunning()) {
            plugin.runSync(() -> {
                if (!inRound()) {
                    alreadyWon.run();
                    return;
                }

                boolean won = currentGame.checkGuess(guess, guesser);

                if (won) {
                    GameController.endRoundWithWinner(guesser, guess);
                }

                callback.accept(won);
            });
        }
    }

    /**
     * Make a guess for the given player.
     * While the correctness of the answer is accurate for the current question,
     * this method makes no guarantee that a correct guess means the player actually won.
     * @param guesser The player guessing.
     * @param guess The guess they're making.
     * @param callback A consumer that accepts a boolean for if the player won.
     * @param alreadyWon A runnable, called if the round ended before the guess was processed.
     * @return Whether or not the answer was correct, but does <b>not</b> imply that the player
     * actually won if their guess was correct. To check if the player won, use the callback.
     */
    public static boolean guessAfterCheck(Player guesser, String guess, Consumer<Boolean> callback, Runnable alreadyWon) {
        if (currentGame.checkGuess(guess, guesser)) {
            GameController.guess(guesser, guess, callback, alreadyWon);
            return true;
        }
        return false;
    }

    // ==================== //
    //    Utility Methods   //
    // ==================== //

    public static LocalDateTime getPhaseStartTime() {
        return phaseStartTime;
    }

    public static String getLastRoundStartedString() {
        Duration duration = Duration.between(phaseStartTime, LocalDateTime.now());
        return WbsStringify.toString(duration, false);
    }

    public static String pointsDisplay(int points) {
        if (points == -1 || points == 1) {
            return points + " point";
        }
        return points + " points";
    }

    @Nullable
    public static Game getCurrentGame() {
        return currentGame;
    }

    public static boolean forceStopped() {
        return forceStopped;
    }

    public static void setForceStopped(boolean forceStopped) {
        GameController.forceStopped = forceStopped;
    }

    @Nullable
    public static String timeToNextRound() {
        if (phaseStartTime == null) return null;

        LocalDateTime nextStart = phaseStartTime.plusNanos((long) (roundDelay / 20.0d * 1000000000.0));
        Duration timeLeft = Duration.between(LocalDateTime.now(), nextStart);

        return WbsStringify.toString(timeLeft, true);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean inRound() {
        return currentGame != null;
    }

    public static Player getLastWinner() {
        return lastWinner;
    }

    @Nullable
    public static Game getLastGame() {
        return lastGame;
    }

    public static GameQueue getGameQueue() {
        return gameQueue;
    }
}
