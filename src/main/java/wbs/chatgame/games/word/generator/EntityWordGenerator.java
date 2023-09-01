package wbs.chatgame.games.word.generator;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.LangUtil;
import wbs.utils.util.WbsEnums;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
        String defaultString = WbsEnums.toPrettyString(type);

        NamespacedKey key = type.getKey();
        String nameKey = "entity." + key.getNamespace() + "." + key.getKey();

        String name = LangUtil.getString(nameKey, defaultString);

        return new GeneratedWord(name, 0, this, true);
    }
}
