package wbs.chatgame.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import wbs.chatgame.GameController;
import wbs.chatgame.data.StatsManager;

public class JoinListeners implements Listener {

    @EventHandler
    public void onJoinStart(PlayerJoinEvent event) {
        if (!GameController.isRunning() && !GameController.forceStopped()) {
            GameController.start();
        }

        StatsManager.loadTotalPoints(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        StatsManager.unload(event.getPlayer().getUniqueId());
    }
}
