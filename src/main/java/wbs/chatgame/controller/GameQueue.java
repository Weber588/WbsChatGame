package wbs.chatgame.controller;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.chatgame.games.GameQuestion;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class GameQueue {
    private final static int QUEUE_SIZE = 5;
    private final static int MAX_START_ATTEMPTS = 5;

    @Nullable
    private RunnableInstance forceNext;

    private final Queue<RunnableInstance> queue = new LinkedList<>();

    private boolean locked = false;

    private int startAttempts = 0;

    @NotNull
    public GameQuestion getNext() {
        RunnableInstance toBuild = next();
        try {
            GameQuestion questionGenerated = toBuild.generateQuestion();
            startAttempts = 0;
            return questionGenerated;
        } catch (IllegalArgumentException e) {
            startAttempts++;
            WbsPlugin plugin = WbsChatGame.getInstance();
            if (toBuild.sender() != null) {
                plugin.sendMessage("&w" + e.getMessage(), toBuild.sender());
                plugin.sendMessage("Starting round without options.", toBuild.sender());
            } else {
                plugin.logger.warning("Game failed to start: " + toBuild.game());
            }
            if (toBuild.options() != null) {
                forceNext = new RunnableInstance(toBuild.sender(), toBuild.game(), null);
            }

            if (startAttempts < MAX_START_ATTEMPTS) {
                return getNext();
            } else {
                startAttempts = 0;
                throw new IllegalStateException("Failed to start game.");
            }
        }
    }

    private RunnableInstance next() {
        RunnableInstance toStart = null;

        if (locked) {
            toStart = forceNext;
        } else if (forceNext != null) {
            toStart = forceNext;
            forceNext = null;
        }

        while (queue.size() < QUEUE_SIZE) {
            Game game = GameManager.getRandomGame();
            queue.add(new RunnableInstance(null, game, null));
        }

        if (toStart == null) {
            toStart = queue.poll();
        }

        return toStart;
    }

    public void setNext(@NotNull CommandSender sender, @NotNull Game nextGame, @Nullable List<String> options) {
        forceNext = new RunnableInstance(sender, nextGame, options);
    }

    public boolean isLocked() {
        return locked;
    }

    public void lock(@NotNull CommandSender sender, @NotNull Game nextGame) {
        lock(sender, nextGame, null);
    }

    public void lock(@NotNull CommandSender sender, @NotNull Game nextGame, @Nullable List<String> options) {
        locked = true;
        setNext(sender, nextGame, options);
    }

    public void lockNext() {
        locked = true;
    }

    public void unlock() {
        if (locked) { // Only clear next game/options if it was actually locked, in case next was set without locking
            locked = false;
            forceNext = null;
        }
    }

    public Game getLockedGame() {
        if (forceNext != null) {
            return forceNext.game();
        } else {
            return null;
        }
    }

    public record RunnableInstance(@Nullable CommandSender sender, @NotNull Game game,
                                                      @Nullable List<String> options) {
        public GameQuestion generateQuestion() throws IllegalArgumentException {
            if (options != null) {
                return game.generateWithOptionsOrChallenge(options);
            }
            return game.generateQuestion();
        }
    }

    public Queue<RunnableInstance> getQueue() {
        return new LinkedList<>(queue);
    }
}
