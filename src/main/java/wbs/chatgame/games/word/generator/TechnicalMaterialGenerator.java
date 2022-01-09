package wbs.chatgame.games.word.generator;

import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Returns a hard coded list of technical materials, like MOVING_PISTON
 * or WALL_BANNER
 */
public class TechnicalMaterialGenerator extends MaterialWordGenerator {

    private final static List<Tag<Material>> TECHNICAL_TAGS = Arrays.asList(
            Tag.FLOWER_POTS, Tag.WALL_SIGNS, Tag.WALL_CORALS
    );

    @Override
    public List<Material> generateMaterials() {
        Set<Material> materials = new HashSet<>();
        for (Tag<Material> tag : TECHNICAL_TAGS) {
            materials.addAll(tag.getValues());
        }

        // Manually adding extras that don't have tags
        Arrays.stream(Material.values())
                .filter(material ->
                        material.toString().contains("INFESTED") ||
                                material.toString().contains("_PLANT") ||
                                material.toString().contains("_STEM") ||
                                material.toString().contains("CORAL_WALL_FAN") ||
                                material.toString().contains("MOVING_PISTON") ||
                                material.toString().contains("_WALL_"))
                .forEach(materials::add);

        materials.addAll(Arrays.asList(
                Material.CARROTS,
                Material.POTATOES,
                Material.BEETROOTS,
                Material.MOVING_PISTON,
                Material.PISTON_HEAD,
                Material.WRITABLE_BOOK,
                Material.FILLED_MAP
        ));

        return materials.stream().toList();
    }
}
