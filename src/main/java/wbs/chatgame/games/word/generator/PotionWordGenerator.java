package wbs.chatgame.games.word.generator;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.string.WbsStrings;

import java.util.*;
import java.util.stream.Collectors;

public class PotionWordGenerator extends KeyedWordGenerator {
    @Override
    protected String getLangPrefix() {
        return "effect.minecraft";
    }

    @Override
    protected List<GeneratedWord> getDefault() {
        return Arrays.stream(PotionEffectType.values())
                .map(potion -> potion.translationKey())
                .map(name -> name.replace("_", " "))
                .map(WbsStrings::capitalizeAll)
                .map(word -> new GeneratedWord(word, 0, this))
                .collect(Collectors.toList());
    }
}
