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

    private final static Map<PotionEffectType, String> potionNameOverrides = new HashMap<>();

    static {
        // TODO: Move this to a config?
        potionNameOverrides.put(PotionEffectType.SLOW, "SLOWNESS");
        potionNameOverrides.put(PotionEffectType.FAST_DIGGING, "HASTE");
        potionNameOverrides.put(PotionEffectType.SLOW_DIGGING, "MINING_FATIGUE");
        potionNameOverrides.put(PotionEffectType.INCREASE_DAMAGE, "STRENGTH");
        potionNameOverrides.put(PotionEffectType.HEAL, "INSTANT_HEALTH");
        potionNameOverrides.put(PotionEffectType.HARM, "INSTANT_DAMAGE");
        potionNameOverrides.put(PotionEffectType.CONFUSION, "NAUSEA");
    }

    @Override
    protected String getLangPrefix() {
        return "effect.minecraft";
    }

    @Override
    protected List<GeneratedWord> getDefault() {
        return Arrays.stream(PotionEffectType.values())
                .map(potion -> potionNameOverrides.getOrDefault(potion, potion.getName()))
                .map(name -> name.replace("_", " "))
                .map(WbsStrings::capitalizeAll)
                .map(word -> new GeneratedWord(word, 0, this))
                .collect(Collectors.toList());
    }
}
