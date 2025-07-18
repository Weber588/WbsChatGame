package wbs.chatgame.games.word.generator;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.registry.RegistryAccess;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.ItemRarity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.LangUtil;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.string.WbsStrings;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class MaterialWordGenerator<T extends Keyed> extends RegistryWordGenerator<T> {
    protected abstract Material toMaterial(T t);

    @Nullable
    @Override
    protected Component getHint(T t) {
        Material material = toMaterial(t);

        List<Component> propertyHints = new ArrayList<>();
        for (MaterialProperty property : MaterialProperty.PROPERTIES) {
            Component hint = property.getHint(material);
            if (hint != null) {
                propertyHints.add(hint);
            }
        }

        if (propertyHints.isEmpty()) {
            return null;
        }

        return WbsCollectionUtil.getRandom(propertyHints);
    }
}
