package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.GameController;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class GameUnlockCommand extends WbsSubcommand {
    public GameUnlockCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "unlock");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (GameController.isNextLocked()) {
            GameController.unlockNext();
            sendMessage("Game unlocked!", sender);
        } else {
            sendMessage("Game is not locked! Use &h/" + label + " lock <game> [options]&r to lock it to a game type.", sender);
        }

        return true;
    }
}
