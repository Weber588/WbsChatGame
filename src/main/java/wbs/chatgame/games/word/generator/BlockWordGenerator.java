package wbs.chatgame.games.word.generator;

import org.bukkit.Material;
import wbs.utils.util.WbsEnums;

import java.util.*;
import java.util.stream.Collectors;

public class BlockWordGenerator extends MaterialWordGenerator {
    @Override
    public List<Material> generateMaterials() {
        return Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .collect(Collectors.toList());
    }
}
