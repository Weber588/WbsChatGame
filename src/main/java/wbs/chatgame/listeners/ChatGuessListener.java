package wbs.chatgame.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.GameController;
import wbs.chatgame.data.ChatGameDB;
import wbs.chatgame.games.Game;
import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;

@SuppressWarnings("unused")
public class ChatGuessListener extends WbsMessenger implements Listener {

    public ChatGuessListener(@NotNull WbsPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!ChatGameDB.getPlayerManager().getOnlinePlayer(player).isListening()) return;

        String message = event.getMessage();

        Game currentGame = GameController.getCurrentGame();

        if (currentGame == null) return;

        boolean correctAnswer = GameController.guessAfterCheck(player, message,
                success -> {},
                () -> sendMessage("&wToo late! The round has ended.", player));

        if (correctAnswer) {
            event.setCancelled(true);
        }
    }
}
