package wbs.chatgame.commands;

import org.bukkit.command.PluginCommand;
import wbs.utils.util.commands.WbsCommand;
import wbs.utils.util.plugin.WbsPlugin;

public class ChatGameCommand extends WbsCommand {
    public ChatGameCommand(WbsPlugin plugin, PluginCommand command) {
        super(plugin, command);

        String perm = "wbscommand";

        addSubcommand(new StatsCommand(plugin), perm + ".stats");
        addSubcommand(new CurrentCommand(plugin), perm + ".current");
        addSubcommand(new IgnoreCommand(plugin), perm + ".ignore");
        addSubcommand(new TopCommand(plugin), perm + ".top");

        String adminPerm = perm + ".admin";

        addSubcommand(new StartCommand(plugin), adminPerm + ".start");
        addSubcommand(new StopCommand(plugin), adminPerm + ".start");
        addSubcommand(new ReloadCommand(plugin), adminPerm + ".reload");
        addSubcommand(new ErrorCommand(plugin), adminPerm + ".reload");
        addSubcommand(new SkipCommand(plugin), adminPerm + ".skip");
        addSubcommand(new CheatCommand(plugin), adminPerm + ".cheat");
        addSubcommand(new TempWordSearchCommand(plugin), adminPerm + ".temp");
        addSubcommand(new DelayCommand(plugin), adminPerm + ".delay");
        addSubcommand(new NextCommand(plugin), adminPerm + ".next");
        addSubcommand(new SaveCacheCommand(plugin), adminPerm + ".savecache");
    }
}
