package wbs.chatgame.games.word.generator;

import io.papermc.paper.registry.RegistryKey;
import org.bukkit.generator.structure.StructureType;
import org.jetbrains.annotations.NotNull;

public class StructureWordGenerator extends RegistryWordGenerator<StructureType> {
    @Override
    protected @NotNull RegistryKey<StructureType> getRegistryKey() {
        return RegistryKey.STRUCTURE_TYPE;
    }
}
