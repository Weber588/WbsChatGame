package wbs.chatgame.controller;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;
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
    public Game startNext() {
        try {
            Game gameRun = next().start();
            startAttempts = 0;
            return gameRun;
        } catch (IllegalArgumentException e) {
            startAttempts++;
            if (forceNext != null) {
                WbsPlugin plugin = WbsChatGame.getInstance();
                if (forceNext.sender() != null) {
                    plugin.sendMessage("&w" + e.getMessage(), forceNext.sender());
                    plugin.sendMessage("Starting round without options.", forceNext.sender());
                } else {
                    plugin.logger.warning("Game failed to start: " + forceNext.game());
                }
                forceNext = new RunnableInstance(forceNext.sender(), forceNext.game(), null);
            }

            if (startAttempts < MAX_START_ATTEMPTS) {
                return startNext();
            } else {
                throw new IllegalArgumentException("Failed to start game.");
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
            queue.add(new RunnableInstance(null, GameManager.getRandomGame(), null));
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

    public static record RunnableInstance(@Nullable CommandSender sender, @NotNull Game game, @Nullable List<String> options) {
        public Game start() throws IllegalArgumentException {
            if (options != null) {
                return game.startWithOptions(options);
            }
            return game.startGame();
        }
    }

    public Queue<RunnableInstance> getQueue() {
        return new LinkedList<>(queue);
    }
}
