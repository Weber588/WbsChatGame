package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.WbsStringify;

import java.time.Duration;

public class DelayCommand extends WbsSubcommand {
    public DelayCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "delay");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

        if (args.length <= 1) {
            sendMessage("Set the delay between rounds. Usage: &h/" + label + " " + args[0] + " <delay in seconds>", sender);
            return true;
        }

        double delayInSeconds;
        try {
            delayInSeconds = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sendMessage("Invalid number: " + args[1] + ".", sender);
            return true;
        }

        GameController.roundDelay = (int) (delayInSeconds * 20);
        Duration betweenRounds = Duration.ofSeconds(GameController.roundDelay / 20);
        sendMessage("Set round delay to " + WbsStringify.toString(betweenRounds, true) + ".", sender);

        return true;
    }
}
