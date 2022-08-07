package wbs.chatgame.games.word.generator;

import org.bukkit.enchantments.Enchantment;
import wbs.utils.util.string.WbsStrings;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantmentWordGenerator extends KeyedWordGenerator {
    @Override
    protected String getLangPrefix() {
        return "enchantment.minecraft";
    }

    @Override
    protected List<GeneratedWord> getDefault() {
        return Arrays.stream(Enchantment.values())
                .map(ench ->
                        WbsStrings.capitalizeAll(
                                ench.getKey()
                                        .getKey()
                                        .replace('_', ' ')
                        )
                ).map(word -> new GeneratedWord(word, 0, this))
                .collect(Collectors.toList());
    }
}
