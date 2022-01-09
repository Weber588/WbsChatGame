package wbs.chatgame.rewards;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.data.PlayerRecord;
import wbs.utils.util.pluginhooks.VaultWrapper;

import java.util.List;
import java.util.UUID;

public record Reward(RewardType type,
                     double chance,
                     int points,
                     double money,
                     String message,
                     List<String> commands,
                     String broadcast
                    ) {

    public enum RewardType {
        RANDOM, RECURRING, MILESTONE
    }

    private void giveMoney(UUID uuid) {
        if (!VaultWrapper.isEcoSetup()) {
            WbsChatGame.getInstance().logger.warning("Vault hook not found - Monetary rewards require Vault to be installed.");
            return;
        }

        VaultWrapper.giveMoney(Bukkit.getOfflinePlayer(uuid), money);
    }

    private void giveReward(PlayerRecord record) {
        if (money != 0) {
            giveMoney(record.getUUID());
        }

        Player player = record.getPlayer();
        WbsChatGame plugin = WbsChatGame.getInstance();

        if (message != null && player != null) {
            plugin.sendMessageNoPrefix(message.replaceAll("%player%", player.getName()), player);
        }

        if (broadcast != null) {
            plugin.broadcast(broadcast
                    .replaceAll("%player%", record.getName())
                    .replaceAll("%points%", record.getPoints() + "")
            );
        }
        if (commands != null && !commands.isEmpty()) {
            plugin.runSync(() -> {
                ConsoleCommandSender sender = Bukkit.getServer().getConsoleSender();
                Server server = Bukkit.getServer();

                for (String command : commands) {
                    server.dispatchCommand(sender, command.replaceAll("%player%", record.getName()));
                }
            });
        }
    }

    public void run(PlayerRecord record, int addPoints) {
        int oldPoints = record.getPoints();
        switch (type) {
            case RANDOM:
                if (Math.random() * 100 < chance) {
                    if (addPoints >= points) {
                        giveReward(record);
                    }
                }
                break;
            case RECURRING:
                if (oldPoints != 0) {
                    if (oldPoints / points < (oldPoints + addPoints) / points) {
                        giveReward(record);
                    }
                }
                break;
            case MILESTONE:
                if (oldPoints < points && (oldPoints + addPoints) >= points) {
                    giveReward(record);
                }
                break;
        }
    }
}
