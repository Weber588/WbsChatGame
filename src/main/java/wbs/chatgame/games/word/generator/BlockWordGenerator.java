package wbs.chatgame.games.word.generator;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import wbs.utils.util.WbsEnums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BlockWordGenerator extends MaterialWordGenerator {
    @Override
    public List<Material> generateMaterials() {
        return Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .collect(Collectors.toList());
    }

    @Override
    protected String getLangPrefix() {
        return "block";
    }
}
