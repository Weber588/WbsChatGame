package wbs.chatgame.games.word.generator;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;

public abstract class RegistryWordGenerator<T extends Keyed> extends WordGenerator {
    @Override
    protected List<GeneratedWord> generateWords() {
        List<GeneratedWord> words = new LinkedList<>();

        RegistryAccess.registryAccess().getRegistry(getRegistryKey()).stream()
                .map(this::getWord)
                .map(word -> new GeneratedWord(word, 0, this, true))
                .forEach(words::add);

        return words;
    }

    @NotNull
    protected abstract RegistryKey<T> getRegistryKey();
    @NotNull
    protected String getWord(T value) {
        return WbsStrings.capitalizeAll(
                value.getKey()
                        .getKey()
                        .replace('_', ' ')
        );
    }
}
