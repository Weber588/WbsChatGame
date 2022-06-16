package wbs.chatgame.games.challenges;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.games.trivia.TriviaGame;
import wbs.chatgame.games.trivia.TriviaQuestion;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsEnums;

import java.util.*;

public class TriviaCraftingAmount extends TriviaQuestionChallenge {

    private final List<RecipeQuestion> recipes = new LinkedList<>();
    private final List<RecipeQuestion> history = new LinkedList<>();

    public TriviaCraftingAmount(TriviaGame parent) {
        super(parent);

        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();

            String group = "";
            if (recipe instanceof ShapedRecipe shaped) {
                group = shaped.getGroup();
            } else if (recipe instanceof ShapelessRecipe shapeless) {
                group = shapeless.getGroup();
            }

            // TODO: Make these toggleable?
            // Skip grouped recipes (fences, wood slabs, most dyed items)
            if (!group.isEmpty()) {
                continue;
            }

            // Exclude things like tools
            if (EnchantmentTarget.BREAKABLE.includes(recipe.getResult())) {
                continue;
            }

            Map<RecipeChoice, Integer> counts = getIngredientCounts(recipe);

            // If the recipe has at least 2 different ingredient types, include it
            if (counts.size() > 1) {
                for (RecipeChoice choice : counts.keySet()) {
                    int count = counts.get(choice);

                    if (count > 1) {
                        RecipeQuestion question = new RecipeQuestion(recipe, choice, count);

                        recipes.add(question);
                    }
                }
            }
        }
    }

    private Map<RecipeChoice, Integer> getIngredientCounts(Recipe recipe) {
        Map<RecipeChoice, Integer> counts = new HashMap<>();

        if (recipe instanceof ShapedRecipe shaped) {
            Map<Character, RecipeChoice> choiceMap = shaped.getChoiceMap();

            for (String row : shaped.getShape()) {
                char[] chars = row.toCharArray();

                for (char key : chars) {
                    RecipeChoice choice = choiceMap.get(key);

                    if (choice != null) {
                        countIfPrecise(choice, counts);
                    }
                }
            }
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            for (RecipeChoice choice : shapeless.getChoiceList()) {
                countIfPrecise(choice, counts);
            }
        }

        return counts;
    }

    private void countIfPrecise(RecipeChoice choice, Map<RecipeChoice, Integer> counts) {
        if (choice instanceof RecipeChoice.ExactChoice exact) {
            if (exact.getChoices().size() != 1) return;
        } else if (choice instanceof RecipeChoice.MaterialChoice material) {
            if (material.getChoices().size() != 1) return;
        } else {
            return;
        }

        int count = counts.getOrDefault(choice, 0);
        counts.put(choice, count + 1);
    }

    @Override
    protected TriviaQuestion nextQuestion() {
        RecipeQuestion recipeQuestion = WbsCollectionUtil.getAvoidRepeats(
                () -> WbsCollectionUtil.getRandom(recipes),
                recipes.size(),
                history,
                1.125);

        ItemStack result = recipeQuestion.recipe.getResult();
        String itemName = WbsEnums.toPrettyString(result.getType());

        RecipeChoice choice = recipeQuestion.choice;
        String choiceString = null;

        if (choice instanceof RecipeChoice.ExactChoice exact) {
            ItemStack ingredient = exact.getChoices().get(0);

            choiceString = WbsEnums.toPrettyString(ingredient.getType());
        } else if (choice instanceof RecipeChoice.MaterialChoice material) {
            Material type = material.getChoices().get(0);

            choiceString = WbsEnums.toPrettyString(type);
        }

        if (choiceString == null) {
            throw new IllegalArgumentException("Internal error. Invalid recipe choice " + choice + ". Please report this error.");
        }

        String question = "How many &h" + choiceString + "&r do you need to craft &h" + itemName + "&r?";

        return new TriviaQuestion("crafting:" + result.getType().name(),
                question,
                2,
                true,
                false,
                false,
                recipeQuestion.count + "");
    }

    @Override
    public boolean valid() {
        return !recipes.isEmpty();
    }

    private record RecipeQuestion(@NotNull Recipe recipe, @NotNull RecipeChoice choice, int count) {}
}
