package wbs.chatgame.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import wbs.chatgame.GameController;
import wbs.chatgame.data.ChatGameDB;
import wbs.chatgame.data.StatsManager;

@SuppressWarnings("unused")
public class JoinListeners implements Listener {

    @EventHandler
    public void onJoinStart(PlayerJoinEvent event) {
        StatsManager.loadTotalPoints(event.getPlayer().getUniqueId());
        ChatGameDB.getPlayerManager().loadOnlinePlayer(event.getPlayer(), record -> {
            if (record.isListening()) {
                if (!GameController.isRunning() && !GameController.forceStopped()) {
                    GameController.start();
                }
            }
        });

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        StatsManager.unload(event.getPlayer().getUniqueId());
        ChatGameDB.getPlayerManager().unloadPlayer(event.getPlayer().getUniqueId());
    }
}
