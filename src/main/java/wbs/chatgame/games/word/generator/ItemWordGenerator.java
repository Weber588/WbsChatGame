package wbs.chatgame.games.word.generator;

import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class ItemWordGenerator extends MaterialWordGenerator<ItemType> {
    @Override
    protected String getLangPrefix() {
        return "item";
    }

    @Override
    protected @NotNull Collection<ItemType> getEntries() {
        return super.getEntries().stream()
                .filter(itemType -> !toMaterial(itemType).isBlock())
                .collect(Collectors.toSet());
    }

    @Override
    protected @NotNull RegistryKey<ItemType> getRegistryKey() {
        return RegistryKey.ITEM;
    }

    @Override
    protected Material toMaterial(ItemType itemType) {
        return itemType.asMaterial();
    }
}
