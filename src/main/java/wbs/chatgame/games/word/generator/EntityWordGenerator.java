package wbs.chatgame.games.word.generator;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class EntityWordGenerator<T extends Entity> extends WordGenerator {
    @Override
    public List<String> generateWords() {
        return Arrays.stream(EntityType.values())
                .filter(type -> type != EntityType.UNKNOWN)
                .filter(type -> {
                    Class<? extends Entity> entityClazz = type.getEntityClass();
                    if (entityClazz == null) return false;

                    return getEntityClass().isAssignableFrom(entityClazz);
                })
                .map(WbsEnums::toPrettyString)
                .collect(Collectors.toList());
    }

    @NotNull
    protected abstract Class<T> getEntityClass();
}
