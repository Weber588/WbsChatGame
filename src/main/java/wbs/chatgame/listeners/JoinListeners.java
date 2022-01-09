package wbs.chatgame.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import wbs.chatgame.GameController;
import wbs.chatgame.WbsChatGame;

public class JoinListeners implements Listener {

    @EventHandler
    public void onJoinStart(PlayerJoinEvent event) {
        if (!GameController.isRunning() && !GameController.forceStopped()) {
            GameController.start();
        }
    }
}
