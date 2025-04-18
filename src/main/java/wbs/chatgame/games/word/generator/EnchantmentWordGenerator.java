package wbs.chatgame.games.word.generator;

import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.LangUtil;

import java.util.Map;

public class EnchantmentWordGenerator extends RegistryWordGenerator<Enchantment> {
    @Override
    protected @NotNull RegistryKey<Enchantment> getRegistryKey() {
        return RegistryKey.ENCHANTMENT;
    }

    @Override
    protected @NotNull String getWord(Enchantment enchantment) {
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

        return super.getWord(enchantment);
    }
}
