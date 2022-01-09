package wbs.chatgame.games.word.generator;

import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ColourBlockWordGenerator extends MaterialWordGenerator {

    /**
     * Unfortunately no internal way to do this since the flattening.
     */
    private final static List<Tag<Material>> COLOUR_BLOCKS = Arrays.asList(
            Tag.BANNERS, Tag.ITEMS_BANNERS, Tag.SHULKER_BOXES, Tag.WOOL, Tag.CARPETS
    );

    @Override
    public List<Material> generateMaterials() {
        Set<Material> materials = new HashSet<>();
        for (Tag<Material> tag : COLOUR_BLOCKS) {
            materials.addAll(tag.getValues());
        }

        // Manually adding extras that don't have tags
        Arrays.stream(Material.values())
                .filter(material ->
                        material.toString().contains("CONCRETE") ||
                        material.toString().contains("TERRACOTTA") ||
                        material.toString().contains("DYE") ||
                        (material.toString().contains("GLASS") && material != Material.getMaterial("SPYGLASS")))
                .forEach(materials::add);

        return materials.stream().toList();
    }
}
