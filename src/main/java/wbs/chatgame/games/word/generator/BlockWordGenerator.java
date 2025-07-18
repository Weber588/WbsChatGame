package wbs.chatgame.games.word.generator;

import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class BlockWordGenerator extends MaterialWordGenerator<BlockType> {
    @Override
    protected String getLangPrefix() {
        return "block";
    }

    @Override
    protected @NotNull RegistryKey<BlockType> getRegistryKey() {
        return RegistryKey.BLOCK;
    }

    @Override
    protected Material toMaterial(BlockType blockType) {
        return blockType.asMaterial();
    }
}
