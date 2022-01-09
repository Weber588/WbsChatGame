package wbs.chatgame.games.word.generator;

import org.bukkit.enchantments.Enchantment;
import wbs.utils.util.string.WbsStrings;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantmentWordGenerator extends WordGenerator {
    @Override
    public List<String> generateWords() {
        return Arrays.stream(Enchantment.values())
                .map(ench ->
                        WbsStrings.capitalizeAll(
                                ench.getKey()
                                        .getKey()
                                        .replace('_', ' ')
                        )
                )
                .collect(Collectors.toList());
    }
}
