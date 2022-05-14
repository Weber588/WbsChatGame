package wbs.chatgame.controller;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.data.ChatGameDB;
import wbs.chatgame.data.PlayerRecord;
import wbs.utils.util.plugin.WbsMessage;
import wbs.utils.util.plugin.WbsMessageBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GameMessenger {

    public static void broadcast(String message) {
        WbsMessageBuilder builder = WbsChatGame.getInstance().buildMessage(message);
        broadcast(builder.build());
    }

    public static void broadcast(WbsMessage message) {
        List<Player> listeningPlayers = getListeningPlayers().stream()
                .map(PlayerRecord::getPlayer)
                .collect(Collectors.toList());

        message.send(listeningPlayers);
    }

    public static List<PlayerRecord> getListeningPlayers() {
        List<PlayerRecord> listeningPlayers = new LinkedList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerRecord record = ChatGameDB.getPlayerManager().getOnlinePlayer(player);
            if (record.isListening()) {
                listeningPlayers.add(record);
            }
        }

        return listeningPlayers;
    }
}
