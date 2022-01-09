package wbs.chatgame.games.word.generator;

import org.bukkit.block.Biome;
import wbs.utils.util.WbsEnums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BiomeWordGenerator extends WordGenerator {
    @Override
    public List<String> generateWords() {
        return Arrays.stream(Biome.values())
                .filter(biome -> biome != Biome.CUSTOM)
                .map(WbsEnums::toPrettyString)
                .collect(Collectors.toList());
    }
}
