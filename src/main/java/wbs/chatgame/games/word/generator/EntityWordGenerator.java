package wbs.chatgame.games.word.generator;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class EntityWordGenerator<T extends Entity> extends WordGenerator {
    @Override
    public List<GeneratedWord> generateWords() {
        return Arrays.stream(EntityType.values())
                .filter(type -> type != EntityType.UNKNOWN)
                .filter(type -> {
                    Class<? extends Entity> entityClazz = type.getEntityClass();
                    if (entityClazz == null) return false;

                    return getEntityClass().isAssignableFrom(entityClazz);
                })
                .map(this::getWord)
                .collect(Collectors.toList());
    }

    @NotNull
    protected abstract Class<T> getEntityClass();

    private GeneratedWord getWord(EntityType type) {
        ConfigurationSection lang = GeneratorManager.getLangConfig();

        String defaultString = WbsEnums.toPrettyString(type);

        if (lang == null) {
            return new GeneratedWord(defaultString, this);
        }

        NamespacedKey key = type.getKey();
        String nameKey = "entity." + key.getNamespace() + "." + key.getKey();

        if (!lang.isString(nameKey)) {
            return new GeneratedWord(defaultString, this);
        }

        return new GeneratedWord(lang.getString(nameKey), this, true);
    }
}
