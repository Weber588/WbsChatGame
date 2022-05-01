package wbs.chatgame;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import wbs.chatgame.commands.ChatGameCommand;
import wbs.chatgame.commands.GuessCommand;
import wbs.chatgame.data.ChatGameDB;
import wbs.chatgame.data.ResetManager;
import wbs.chatgame.data.StatsManager;
import wbs.chatgame.games.GameManager;
import wbs.chatgame.games.challenges.ChallengeManager;
import wbs.chatgame.listeners.ChatGuessListener;
import wbs.chatgame.listeners.JoinListeners;
import wbs.utils.util.plugin.WbsPlugin;

public class WbsChatGame extends WbsPlugin {

    private static WbsChatGame instance;
    public static WbsChatGame getInstance() {
        return instance;
    }

    public ChatGameSettings settings;

    @Override
    public void onEnable() {
        instance = this;
        settings = new ChatGameSettings(this);

        GameController.setPlugin(this);
        GameManager.registerNativeGames();
        ChatGameDB.setupDatabase();
        ResetManager.scheduleResets();

        settings.reload();

        registerListener(new ChatGuessListener(this));
        registerListener(new JoinListeners());
        new ChatGameCommand(this, getCommand("chatgame"));

        GuessCommand guessCommand = new GuessCommand(this);
        PluginCommand pluginCommand = getCommand("guess");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(guessCommand);
            pluginCommand.setTabCompleter(guessCommand);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            StatsManager.loadTotalPoints(player.getUniqueId());
            ChatGameDB.getPlayerManager().loadOnlinePlayer(player);
        }

        StatsManager.recalculateAll();

        PlaceholderManager.registerPlaceholders();
    }

    @Override
    public void onDisable() {
        GameController.stop();
    }
}
