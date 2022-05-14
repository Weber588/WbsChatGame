package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.games.Game;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;

public class SkipCommand extends AbstractNextCommand {
    public SkipCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "skip");
        isGameMandatory = false;
    }

    @Override
    protected void afterNext(CommandSender sender, String label, String[] args, @Nullable Game game, List<String> options) {
        sendMessage("Skipping...", sender);

        GameController.skip();
    }
}
