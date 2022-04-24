package wbs.chatgame.games.word.generator;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemWordGenerator extends MaterialWordGenerator {
    @Override
    public List<Material> generateMaterials() {
        return Arrays.stream(Material.values())
                .filter(material -> material.isItem() && !material.isBlock())
                .collect(Collectors.toList());
    }
}
