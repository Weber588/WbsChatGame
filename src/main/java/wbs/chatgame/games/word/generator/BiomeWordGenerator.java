package wbs.chatgame.games.word.generator;

import org.bukkit.block.Biome;
import wbs.utils.util.WbsEnums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BiomeWordGenerator extends SimpleWordGenerator {
    @Override
    public List<String> generateStrings() {
        return Arrays.stream(Biome.values())
                .filter(biome -> biome != Biome.CUSTOM)
                .map(WbsEnums::toPrettyString)
                .collect(Collectors.toList());
    }
}
