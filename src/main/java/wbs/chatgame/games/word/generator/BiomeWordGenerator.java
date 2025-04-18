package wbs.chatgame.games.word.generator;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.block.Biome;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsKeyed;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BiomeWordGenerator extends SimpleWordGenerator {
    @Override
    public List<String> generateStrings() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).stream()
                .map(WbsKeyed::toPrettyString)
                .collect(Collectors.toList());
    }
}
