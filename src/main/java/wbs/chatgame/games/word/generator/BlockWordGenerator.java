package wbs.chatgame.games.word.generator;

import org.bukkit.Material;
import wbs.utils.util.WbsEnums;

import java.util.*;
import java.util.stream.Collectors;

public class BlockWordGenerator extends WordGenerator {
    @Override
    public List<String> generateWords() {
        return Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .map(WbsEnums::toPrettyString)
                .collect(Collectors.toList());
    }
}
