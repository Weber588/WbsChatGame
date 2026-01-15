package wbs.chatgame.games.word.generator;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Fire;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.LangUtil;
import wbs.utils.util.WbsEnums;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public interface MaterialProperty {
    Set<MaterialProperty> PROPERTIES = new HashSet<>();

    MaterialProperty FUEL = new SimpleProperty(Material::isFuel, "This item/block can be used as fuel in a furnace");
    MaterialProperty FOOD = new SimpleProperty(Material::isEdible, "This item is a type of food or drink");
    MaterialProperty WEARABLE = new SimpleProperty(EnchantmentTarget.WEARABLE::includes, "This item can be worn like armour");
    MaterialProperty POWERABLE = new SimpleProperty(
            material -> material.isBlock() && Powerable.class.isAssignableFrom(material.createBlockData().getClass()), 
            "This block interacts with redstone"
    );
    MaterialProperty GROWABLE = new SimpleProperty(
            material -> {
                if (!material.isBlock()) {
                    return false;
                }
                
                Class<? extends @NotNull BlockData> dataClass = material.createBlockData().getClass();
                return Ageable.class.isAssignableFrom(dataClass) && !Fire.class.isAssignableFrom(dataClass);
            }, 
            "This block can grow"
    );
    MaterialProperty GRAVITY = new SimpleProperty(Material::hasGravity, "This block is affected by gravity");
    MaterialProperty DURABILITY = new SimpleProperty(
            material -> material.getMaxDurability() > 0, 
            "This item has durability"
    );
    MaterialProperty BEACON_BASE = new SimpleProperty(
            Tag.BEACON_BASE_BLOCKS::isTagged, 
            "This block can be used as the base of a beacon"
    );
    MaterialProperty FLOWER = new SimpleProperty(
            Tag.FLOWERS::isTagged, 
            "This is a type of flower"
    );
    MaterialProperty STACK_SIZE_16 = new SimpleProperty(
            material -> material.getMaxStackSize() == 16, 
            "This item has a maximum stack size of 16"
    );
    MaterialProperty UNSTACKABLE = new SimpleProperty(
            material -> material.getMaxStackSize() == 1, 
            "This item is unstackable"
    );
    MaterialProperty WATERLOGGABLE = new SimpleProperty(
            material -> material.isBlock() && Waterlogged.class.isAssignableFrom(material.createBlockData().getClass()), 
            "This block can be waterlogged"
    );
    MaterialProperty TNT_IMMUNE = new SimpleProperty(
            material -> material.isBlock() && material.getBlastResistance() >= 100,
            "This block can be waterlogged"
    );
    MaterialProperty COMPOSTABLE = new SimpleProperty(
            Material::isCompostable,
            "This item can be used in a composter"
    );
    MaterialProperty IMMOVABLE = new SimpleProperty(
            material -> material.isBlock() && material.createBlockData().getPistonMoveReaction() == PistonMoveReaction.BLOCK,
            "This block cannot be moved by pistons"
    );
    MaterialProperty PISTON_BREAKS = new SimpleProperty(
            material -> material.isBlock() && material.createBlockData().getPistonMoveReaction() == PistonMoveReaction.BREAK,
            "This block will break when pushed by a piston"
    );
    MaterialProperty EMITS_LIGHT = new SimpleProperty(
            material -> material.isBlock() && material.createBlockData().getLightEmission() > 0,
            "This block emits light"
    );
    MaterialProperty HAS_ATTRIBUTES = new SimpleProperty(
            material -> material.isItem() && !material.getDefaultAttributeModifiers().isEmpty(),
            "This item has attributes"
    );
    MaterialProperty ENCHANTABLE = new SimpleProperty(
            material -> material.isItem() && material.hasDefaultData(DataComponentTypes.ENCHANTABLE),
            "This item can be enchanted"
    );
    MaterialProperty REPAIRABLE = new SimpleProperty(
            material -> material.isItem() && material.hasDefaultData(DataComponentTypes.REPAIRABLE),
            "This item can be repaired"
    );
    MaterialProperty LIGHTABLE = new SimpleProperty(
            material -> material.isBlock() && Lightable.class.isAssignableFrom(material.createBlockData().getClass()),
            "This block is lightable"
    );
    MaterialProperty ROTATABLE = new SimpleProperty(
            material -> {
                if (!material.isBlock()) {
                    return false;
                }
                Class<? extends @NotNull BlockData> dataClass = material.createBlockData().getClass();
                return material.isBlock() && (
                        Orientable.class.isAssignableFrom(dataClass) ||
                        Rotatable.class.isAssignableFrom(dataClass) ||
                        Directional.class.isAssignableFrom(dataClass)
                );
            },
            "This block can be rotated"
    );

    MaterialProperty SMELTABLE = new MaterialProperty() {
        private static final List<FurnaceRecipe> furnaceRecipes = new LinkedList<>();

        static {
            Iterator<Recipe> iterator = Bukkit.recipeIterator();
            while (iterator.hasNext()) {
                Recipe recipe;
                try {
                    recipe = iterator.next();
                } catch (IllegalArgumentException ex) {
                    continue;
                }
                // Only add vanilla recipes -- others can be buggy with plugins that require data to smelt
                if (recipe instanceof FurnaceRecipe furnaceRecipe && furnaceRecipe.key().namespace().equals(NamespacedKey.MINECRAFT_NAMESPACE)) {
                    furnaceRecipes.add(furnaceRecipe);
                }
            }
        }

        @Override
        public @Nullable Component getHint(Material material) {
            for (FurnaceRecipe recipe : furnaceRecipes) {
                if (recipe != null && recipe.getInput().getType() == material) {
                    return Component.text("This item can be smelted");
                }
            }
            return null;
        }
    }.register();

    MaterialProperty CREATIVE_CATEGORY = ((MaterialProperty) material -> {
        CreativeCategory category = material.getCreativeCategory();
        if (category != null && category != CreativeCategory.BUILDING_BLOCKS) {
            return Component.text("This block/item appears in the \""
                    + LangUtil.getLangConfig().getOrDefault(category.translationKey(), WbsEnums.toPrettyString(category))
                    + "\" category in creative mode");
        }

        return null;
    }).register();

    MaterialProperty RARITY = ((MaterialProperty) material -> {
        if (!material.isItem()) {
            return null;
        }

        ItemRarity defaultRarity = material.getDefaultData(DataComponentTypes.RARITY);
        if (defaultRarity != null && defaultRarity != ItemRarity.COMMON) {
            return Component.text("This block/item's rarity is ")
                    .append(Component.text(WbsEnums.toPrettyString(defaultRarity)).color(defaultRarity.color()));
        }

        return null;
    }).register();

    MaterialProperty MAP_COLOUR = ((MaterialProperty) material -> {
        if (!material.isBlock()) {
            return null;
        }

        BlockData blockData = material.createBlockData();

        Color mapColor = blockData.getMapColor();

        if (mapColor.getAlpha() == 0) {
            return Component.text("This block is invisible on maps");
        } else {
            return Component.text("This block appears as the colour ")
                    .append(Component.text("█")
                            .color(TextColor.color(mapColor.asRGB()))
                            .decorate(TextDecoration.BOLD)
                            .hoverEvent(HoverEvent.showText(Component.text("#" + Integer.toHexString(mapColor.asRGB())))
                    )).append(Component.text(" on a map"));
        }
    }).register();

    @Nullable Component getHint(Material material);
    default MaterialProperty register() {
        PROPERTIES.add(this);
        return this;
    }
    
    class SimpleProperty implements MaterialProperty {
        private final Predicate<Material> predicate;
        private final Component hint;

        public SimpleProperty(Predicate<Material> predicate, Component hint) {
            this.predicate = predicate;
            this.hint = hint;

            register();
        }
        public SimpleProperty(Predicate<Material> predicate, String hintString) {
            this(predicate, Component.text(hintString));
        }

        @Override
        public @Nullable Component getHint(Material material) {
            return predicate.test(material) ? hint : null;
        }
    }
}
