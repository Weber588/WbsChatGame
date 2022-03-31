package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.GameController;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class NextCommand extends AbstractNextCommand {
    public NextCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "next");
        addAlias("custom");
    }

    @Override
    protected void afterNext(CommandSender sender, String label, String[] args, Game game, List<String> options) {
        sendMessage("The next round will be " + game.getGameName() + ".", sender);
    }
}
