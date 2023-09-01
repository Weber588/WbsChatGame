package wbs.chatgame;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for translating terms existing in the Minecraft lang file, if configured.
 */
public class LangUtil {

    private static Map<String, String> langConfig = new HashMap<>();

    public static void registerLangMap(Map<String, String> langConfig) {
        LangUtil.langConfig = langConfig;
    }

    @NotNull
    public static String getString(String key) {
        return langConfig.getOrDefault(key, key);
    }

    @NotNull
    public static String getString(String key, String defaultValue) {
        return langConfig.getOrDefault(key, defaultValue);
    }

    @NotNull
    public static String getMaterialName(Material material) {
        String defaultString = WbsEnums.toPrettyString(material);

        NamespacedKey key = material.getKey();

        String namePrefix;
        if (material.isItem()) {
            namePrefix = "item";
        } else {
            namePrefix = "block";
        }

        String nameKey = namePrefix + "." + key.getNamespace() + "." + key.getKey();

        return langConfig.getOrDefault(nameKey, defaultString);
    }

    @NotNull
    public static String getEntityName(EntityType type) {
        String defaultString = WbsEnums.toPrettyString(type);

        NamespacedKey key = type.getKey();

        String namePrefix = "entity";

        String nameKey = namePrefix + "." + key.getNamespace() + "." + key.getKey();

        return langConfig.getOrDefault(nameKey, defaultString);
    }

    public static Map<String, String> getLangConfig() {
        return new HashMap<>(langConfig);
    }
}
