package wbs.chatgame.games.word.generator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Fire;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public enum MaterialProperty {
    FUEL {
        @Override
        public String getHint() {
            return "This item/block can be used as fuel in a furnace";
        }

        @Override
        public boolean hasProperty(Material material) {
            return material.isFuel();
        }
    },
    FOOD {
        @Override
        public boolean hasProperty(Material material) {
            return material.isEdible();
        }

        @Override
        public String getHint() {
            return "This item is a type of food or drink";
        }
    },
    WEARABLE {
        @Override
        public boolean hasProperty(Material material) {
            return EnchantmentTarget.WEARABLE.includes(material);
        }

        @Override
        public String getHint() {
            return "This item can be worn like armour";
        }
    },
    POWERABLE {
        @Override
        public boolean hasProperty(Material material) {
            if (!material.isBlock()) return false;
            return Powerable.class.isAssignableFrom(material.createBlockData().getClass());
        }

        @Override
        public String getHint() {
            return "This block interacts with redstone";
        }
    },
    GROWABLE {
        @Override
        public boolean hasProperty(Material material) {
            if (!material.isBlock()) return false;
            Class<? extends BlockData> dataClass = material.createBlockData().getClass();
            // Fire extends ageable despite not being a crop because it ages before extinguishing; ignore it
            return Ageable.class.isAssignableFrom(dataClass) && !Fire.class.isAssignableFrom(dataClass);
        }

        @Override
        public String getHint() {
            return "This block can grow";
        }
    },
    INTERACTABLE {
        @Override
        public boolean hasProperty(Material material) {
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

        @Override
        public String getHint() {
            return "This block can be right clicked";
        }
    },
    SMELTABLE {
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
                if (recipe instanceof FurnaceRecipe) {
                    furnaceRecipes.add((FurnaceRecipe) recipe);
                }
            }
        }

        @Override
        public boolean hasProperty(Material material) {
            for (FurnaceRecipe recipe : furnaceRecipes) {
                if (recipe.getInput().getType() == material) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getHint() {
            return "This item can be smelted";
        }
    },
    GRAVITY {
        @Override
        public boolean hasProperty(Material material) {
            return material.hasGravity();
        }

        @Override
        public String getHint() {
            return "This block is affected by gravity";
        }
    },
    DURABILITY {
        @Override
        public boolean hasProperty(Material material) {
            return EnchantmentTarget.BREAKABLE.includes(material);
        }

        @Override
        public String getHint() {
            return "This item has durability";
        }
    },
    BEACON_BASE {
        @Override
        public boolean hasProperty(Material material) {
            return Tag.BEACON_BASE_BLOCKS.isTagged(material);
        }

        @Override
        public String getHint() {
            return "This block can be used as the base of a beacon";
        }
    },
    FLOWER {
        @Override
        public boolean hasProperty(Material material) {
            return Tag.FLOWERS.isTagged(material);
        }

        @Override
        public String getHint() {
            return "This is a type of flower";
        }
    },
    STACK_SIZE_16 {
        @Override
        public boolean hasProperty(Material material) {
            return material.getMaxStackSize() == 16;
        }

        @Override
        public String getHint() {
            return "This item has a maximum stack size of 16";
        }
    },
    UNSTACKABLE {
        @Override
        public boolean hasProperty(Material material) {
            return material.getMaxStackSize() == 1;
        }

        @Override
        public String getHint() {
            return "This item is unstackable";
        }
    },
    WATERLOGGABLE {
        @Override
        public boolean hasProperty(Material material) {
            if (!material.isBlock()) return false;
            return Waterlogged.class.isAssignableFrom(material.createBlockData().getClass());
        }

        @Override
        public String getHint() {
            return "This block can be waterlogged";
        }
    },
    TNT_IMMUNE {
        @Override
        public boolean hasProperty(Material material) {
            if (!material.isBlock()) return false;

            return material.getBlastResistance() >= 100;
        }

        @Override
        public String getHint() {
            return "This block cannot be blown up by tnt";
        }
    },
    ;

    public abstract boolean hasProperty(Material material);
    public abstract String getHint();
}
