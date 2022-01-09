package wbs.chatgame.games.word.generator;

import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates materials that are considered states of another material, such as
 * variations on wood types (doors, boats, stairs). Does not include coloured blocks.
 */
public class VariedMaterialWordGenerator extends MaterialWordGenerator {

    private final static List<Tag<Material>> SHAPED = Arrays.asList(
            Tag.ITEMS_BOATS, Tag.BUTTONS, Tag.DOORS, Tag.FENCES, Tag.FENCE_GATES,
            Tag.PRESSURE_PLATES, Tag.SIGNS, Tag.WALL_SIGNS, Tag.STAIRS, Tag.TRAPDOORS,
            Tag.WALLS, Tag.SLABS
    );

    @Override
    public List<Material> generateMaterials() {
        Set<Material> materials = new HashSet<>();
        for (Tag<Material> tag : SHAPED) {
            materials.addAll(tag.getValues());
        }
        return materials.stream().toList();
    }
}
