package wbs.chatgame.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TempWordSearchCommand extends WbsSubcommand {
    public TempWordSearchCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "search");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

        if (args.length <= 1) {
            sendMessage("Usage: /cg search <material search string>", sender);
            return true;
        }

        List<Material> materials = new LinkedList<>();

        for (Material material : Material.values()) {
            if (material.toString().toLowerCase().contains(args[1].toLowerCase())) {
                materials.add(material);
            }
        }

        if (materials.isEmpty()) {
            sendMessage("None found!", sender);
        } else {
            sendMessage(materials.stream().map(Objects::toString).collect(Collectors.joining(", ")), sender);
        }

        return true;
    }
}
