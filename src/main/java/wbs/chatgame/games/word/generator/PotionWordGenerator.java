package wbs.chatgame.games.word.generator;

import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeWrapper;
import wbs.utils.util.string.WbsStrings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PotionWordGenerator extends WordGenerator {

    private final static Map<PotionEffectType, String> potionNameOverrides = new HashMap<>();

    static {
        potionNameOverrides.put(PotionEffectType.SLOW, "SLOWNESS");
        potionNameOverrides.put(PotionEffectType.FAST_DIGGING, "HASTE");
        potionNameOverrides.put(PotionEffectType.SLOW_DIGGING, "MINING_FATIGUE");
        potionNameOverrides.put(PotionEffectType.INCREASE_DAMAGE, "STRENGTH");
        potionNameOverrides.put(PotionEffectType.HEAL, "INSTANT_HEALTH");
        potionNameOverrides.put(PotionEffectType.HARM, "INSTANT_DAMAGE");
        potionNameOverrides.put(PotionEffectType.CONFUSION, "NAUSEA");
    }

    @Override
    public List<String> generateWords() {
        return Arrays.stream(PotionEffectType.values())
                .map(potion -> potionNameOverrides.getOrDefault(potion, potion.getName()))
                .map(name -> name.replace("_", " "))
                .map(WbsStrings::capitalizeAll)
                .collect(Collectors.toList());
    }
}
