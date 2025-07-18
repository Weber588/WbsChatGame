package wbs.chatgame.games.word.generator;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.LangUtil;
import wbs.utils.util.string.WbsStrings;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class RegistryWordGenerator<T extends Keyed> extends WordGenerator {
    protected abstract @Nullable String getLangPrefix();

    @Override
    protected List<GeneratedWord> generateWords() {
        List<GeneratedWord> words = new LinkedList<>();

        Map<String, String> lang = LangUtil.getLangConfig();
        String langPrefix = getLangPrefix();

        for (T entry : getEntries()) {
            String word;

            String fallback = getFallback(entry);

            if (entry instanceof Translatable translatable) {
                String translationKey = translatable.translationKey();

               word = lang.getOrDefault(translationKey, fallback);
            } else if (langPrefix != null) {
                word = lang.getOrDefault(langPrefix + "." + entry.key().namespace() + "." + entry.key().value(), fallback);
            } else {
                word = fallback;
            }

            words.add(new GeneratedWord(word, 0, this, getHint(entry), true));
        }

        return words;
    }

    protected @NotNull Collection<T> getEntries() {
        return RegistryAccess.registryAccess().getRegistry(getRegistryKey()).stream().toList();
    }

    @NotNull
    protected abstract RegistryKey<T> getRegistryKey();
    protected @Nullable Component getHint(T t) {
        return null;
    }

    @NotNull
    protected String getFallback(T value) {
        String keyString = value.key().value();

        return WbsStrings.capitalizeAll(
                keyString.substring(keyString.lastIndexOf("/") + 1)
                        .replaceAll("[_\\-]", " ")
        );
    }
}
