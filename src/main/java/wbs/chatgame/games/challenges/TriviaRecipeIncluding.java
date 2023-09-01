package wbs.chatgame.games.challenges;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import wbs.chatgame.LangUtil;
import wbs.chatgame.games.trivia.TriviaGame;
import wbs.chatgame.games.trivia.TriviaQuestion;
import wbs.utils.util.WbsCollectionUtil;

import java.util.*;

public class TriviaRecipeIncluding extends TriviaQuestionChallenge {
    // Map of materials to all recipes that it's included in
    private final Multimap<Material, Material> recipes = HashMultimap.create();
    private final List<Material> history = new LinkedList<>();

    public TriviaRecipeIncluding(TriviaGame parent) {
        super(parent);

        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();

        // Recipes that appear in a group in the recipe book
        Set<Material> groupedRecipes = new HashSet<>();
        // Recipes that are "storage" recipes, defined as 9 of the same item
        // being crafted together into a block. Removed to avoid easy answers like
        // "Name an item you can craft using Raw Iron" where the
        // only answer is "Raw Iron Block"
        Set<Material> storageRecipes = new HashSet<>();
        // Recipes that include the end result in the recipe
        Set<Material> duplicationRecipes = new HashSet<>();
        // Recipes that are only a single item, like flowers to dye
        Set<Material> singleItemRecipes = new HashSet<>();

        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();

            List<Material> ingredients = new LinkedList<>();
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                if (!shapedRecipe.getGroup().isEmpty()) {
                    groupedRecipes.add(recipe.getResult().getType());
                }

                Map<Character, ItemStack> ingredientMap = shapedRecipe.getIngredientMap();

                ingredientMap.values().stream()
                        .filter(Objects::nonNull)
                        .filter(ingredient -> !ingredient.hasItemMeta())
                        .map(ItemStack::getType)
                        .forEach(ingredients::add);
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                if (!shapelessRecipe.getGroup().isEmpty()) {
                    groupedRecipes.add(recipe.getResult().getType());
                }

                shapelessRecipe.getIngredientList().stream()
                        .filter(Objects::nonNull)
                        .filter(ingredient -> !ingredient.hasItemMeta())
                        .map(ItemStack::getType)
                        .forEach(ingredients::add);
            }

            Material resultType = recipe.getResult().getType();

            if (ingredients.contains(resultType)) {
                duplicationRecipes.add(resultType);
            }

            if (ingredients.size() == 1) {
                singleItemRecipes.add(resultType);
            }

            if (ingredients.size() == 9 && new HashSet<>(ingredients).size() == 1) {
                storageRecipes.add(resultType);
            }

            ingredients.stream()
                    .distinct()
                    .forEach(ingredient -> recipes.put(ingredient, resultType));
        }

        Set<Material> toRemove = new HashSet<>();

        //TODO: Make these options configurable somewhere - maybe convert challenge
        // sections from just a chance to have additional options?
        Set<Material> allFilterable = new HashSet<>();
        allFilterable.addAll(storageRecipes);
        allFilterable.addAll(groupedRecipes);
        allFilterable.addAll(duplicationRecipes);
        allFilterable.addAll(singleItemRecipes);

        for (Material ingredient : recipes.keySet()) {
            Collection<Material> results = this.recipes.get(ingredient);
            if (allFilterable.containsAll(results)) {
                toRemove.add(ingredient);
            }
        }

        toRemove.forEach(recipes::removeAll);
    }

    @Override
    protected TriviaQuestion nextQuestion() {
        Material chosenMaterial = WbsCollectionUtil.getAvoidRepeats(
                () -> WbsCollectionUtil.getRandom(recipes.keySet()),
                recipes.size(),
                history,
                1.125);

        String materialName = LangUtil.getMaterialName(chosenMaterial);

        String question = "Name an item you can craft using &h" + materialName + "&r.";
        Collection<Material> materials = this.recipes.get(chosenMaterial);
        String[] answers = materials.stream()
                .map(LangUtil::getMaterialName)
                .distinct()
                .toArray(String[]::new);

        int numberOfAnswers = answers.length;

        int points;
        if (numberOfAnswers == 1) {
            if (materials.contains(chosenMaterial)) {
                // If the only recipe an item is used in is its own, don't consider it a 3-point question.
                points = 1;
            } else {
                points = 3;
            }
        } else if (numberOfAnswers <= 4) {
            points = 2;
        } else {
            points = 1;
        }

        return new TriviaQuestion("ingredient:" + chosenMaterial.name(),
                question,
                points,
                true,
                false,
                false,
                answers
        );
    }

    @Override
    public boolean valid() {
        return !recipes.isEmpty();
    }
}
