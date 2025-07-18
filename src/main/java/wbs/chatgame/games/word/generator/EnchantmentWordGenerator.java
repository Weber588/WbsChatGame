package wbs.chatgame.games.word.generator;

import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.LangUtil;

import java.util.Map;

public class EnchantmentWordGenerator extends RegistryWordGenerator<Enchantment> {
    @Override
    protected @Nullable String getLangPrefix() {
        return "enchantment";
    }

    @Override
    protected @NotNull RegistryKey<Enchantment> getRegistryKey() {
        return RegistryKey.ENCHANTMENT;
    }

    @Override
    protected @Nullable Component getHint(Enchantment enchantment) {
        if (!enchantment.key().namespace().equals(NamespacedKey.MINECRAFT_NAMESPACE)) {
            return Component.text("This is a custom enchantment");
        }

        return Component.text("This word is a type of enchantment");
    }

    @Override
    protected @NotNull String getFallback(Enchantment enchantment) {
        Component name = enchantment.description();

        if (name instanceof TextComponent text) {
            return text.content();
        } else if (name instanceof TranslatableComponent translatable) {
            Map<String, String> lang = LangUtil.getLangConfig();

            String orDefault = lang.getOrDefault(translatable.key(), translatable.fallback());
            if (orDefault != null) {
                return orDefault;
            }
        }

        return super.getFallback(enchantment);
    }
}
