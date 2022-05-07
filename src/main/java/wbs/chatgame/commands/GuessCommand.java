package wbs.chatgame.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.GameController;
import wbs.chatgame.WbsChatGame;
import wbs.utils.util.commands.WbsCommand;
import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.WbsStrings;

import java.util.Collections;
import java.util.List;

public class GuessCommand extends WbsMessenger implements TabExecutor {

    private final WbsChatGame plugin;
    public GuessCommand(WbsChatGame plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        if (args.length == 0) {
            sendMessage("Usage: &h/" + label + " <guess>", sender);
            return true;
        }

        if (!GameController.inRound()) {
            if (GameController.isRunning()) {
                sendMessage("There is no question pending right now.", sender);
                sendMessage("There are &h" + GameController.timeToNextRound() + "&r until the next round starts.", sender);
            } else {
                sendMessage("The game is not running right now.", sender);
            }
            return true;
        }

        String guess = String.join(" ", args);

        GameController.guess(player, guess, success -> {
            if (!success) {
                sendMessage("&cIncorrect!", player);
            }
        }, () -> sendMessage("&wToo late! The round has ended.", sender));

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
