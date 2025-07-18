package wbs.chatgame.games.word.generator;

import io.papermc.paper.registry.RegistryKey;
import org.bukkit.generator.structure.StructureType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StructureWordGenerator extends RegistryWordGenerator<StructureType> {
    @Override
    protected @Nullable String getLangPrefix() {
        return "structure";
    }

    @Override
    protected @NotNull RegistryKey<StructureType> getRegistryKey() {
        return RegistryKey.STRUCTURE_TYPE;
    }
}
