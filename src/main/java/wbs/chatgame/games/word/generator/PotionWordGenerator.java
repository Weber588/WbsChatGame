package wbs.chatgame.games.word.generator;

import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.LangUtil;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.string.WbsStrings;

import java.util.*;
import java.util.stream.Collectors;

public class PotionWordGenerator extends RegistryWordGenerator<PotionEffectType> {

    @Override
    protected String getLangPrefix() {
        return "effect";
    }

    @Override
    protected @NotNull RegistryKey<PotionEffectType> getRegistryKey() {
        return RegistryKey.MOB_EFFECT;
    }

    @Override
    protected @Nullable Component getHint(PotionEffectType potionEffectType) {
        return Component.text("This word is a type of potion effect!");
    }
}
