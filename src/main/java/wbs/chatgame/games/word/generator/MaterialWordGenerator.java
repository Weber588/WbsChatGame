package wbs.chatgame.games.word.generator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Fire;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.meta.Repairable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsEnums;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MaterialWordGenerator extends WordGenerator {

    private static final List<FurnaceRecipe> furnaceRecipes = new LinkedList<>();

    static {
        Bukkit.recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof FurnaceRecipe) furnaceRecipes.add((FurnaceRecipe) recipe);
        });
    }

    @Override
    public final List<GeneratedWord> generateWords() {
        return generateMaterials().stream()
                .map(material ->
                        new GeneratedWord(WbsEnums.toPrettyString(material), this, getHint(material)))
                .collect(Collectors.toList());
    }

    public abstract List<Material> generateMaterials();

    private enum MaterialProperty {
        FUEL,
        FOOD,
        ARMOUR,
        POWERABLE,
        GROWABLE,
        INTERACTABLE,
        SMELTABLE,
        GRAVITY,
        TOOL,
        BEACON_BASE,
        FLOWER,
        ;


        public boolean hasProperty(Material material) {
            switch (this) {
                case FUEL -> {
                    return material.isFuel();
                }
                case FOOD -> {
                    return material.isEdible();
                }
                case ARMOUR -> {
                    if (!material.isItem()) return false;
                    return material.getEquipmentSlot() != EquipmentSlot.HAND;
                }
                case POWERABLE -> {
                    if (!material.isBlock()) return false;
                    return Powerable.class.isAssignableFrom(material.createBlockData().getClass());
                }
                case GROWABLE -> {
                    if (!material.isBlock()) return false;
                    Class<? extends BlockData> dataClass = material.createBlockData().getClass();
                    // Fire extends ageable despite not being a crop because it ages before extinguishing; ignore it
                    return Ageable.class.isAssignableFrom(dataClass) && !Fire.class.isAssignableFrom(dataClass);
                }
                case INTERACTABLE -> {
                    if (!material.isBlock()) return false;
                    if (material.isInteractable()) {
                        if (!Tag.STAIRS.isTagged(material)) {
                            //noinspection RedundantIfStatement
                            if (material != Material.PISTON_HEAD) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
                case SMELTABLE -> {
                    if (!material.isItem()) return false;
                    for (FurnaceRecipe recipe : furnaceRecipes) {
                        if (recipe.getInput().getType() == material) {
                            return true;
                        }
                    }
                    return false;
                }
                case GRAVITY -> {
                    if (!material.isBlock()) return false;
                    return material.hasGravity();
                }
                case TOOL -> {
                    if (!material.isItem()) return false;
                    return EnchantmentTarget.TOOL.includes(material);
                }
                case BEACON_BASE -> {
                    return Tag.BEACON_BASE_BLOCKS.isTagged(material);
                }
                case FLOWER -> {
                    return Tag.FLOWERS.isTagged(material);
                }
            }

            return false;
        }

        public String getHint() {
            switch (this) {
                case FUEL -> {
                    return "This item/block can be used as fuel in a furnace";
                }
                case FOOD -> {
                    return "This item is a type of food or drink";
                }
                case ARMOUR -> {
                    return "This item can be worn as armour";
                }
                case POWERABLE -> {
                    return "This block interacts with redstone";
                }
                case GROWABLE -> {
                    return "This block can grow";
                }
                case INTERACTABLE -> {
                    return "This block does something when you right click on it";
                }
                case SMELTABLE -> {
                    return "This item can be smelted";
                }
                case GRAVITY -> {
                    return "This block is affected by gravity";
                }
                case TOOL -> {
                    return "This item is a type of tool";
                }
                case BEACON_BASE -> {
                    return "This block can be used as the base of a beacon";
                }
                case FLOWER -> {
                    return "This is a type of flower";
                }
            }

            return null;
        }
    }

    private String getHint(Material material) {
        List<MaterialProperty> properties = new ArrayList<>();
        for (MaterialProperty property : MaterialProperty.values()) {
            if (property.hasProperty(material)) {
                properties.add(property);
            }
        }

        if (properties.isEmpty()) {
            if (material.isBlock()) {
                return "This is a type of block";
            } else if (material.isItem()) {
                return "This is a type of item";
            }
            return null;
        }

        return WbsCollectionUtil.getRandom(properties).getHint();
    }
}
