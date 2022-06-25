package wbs.chatgame.games.word.generator;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsEnums;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class MaterialWordGenerator extends WordGenerator {

    @Override
    public final List<GeneratedWord> generateWords() {
        //noinspection deprecation
        return generateMaterials().stream()
                .filter(Predicate.not(Material::isLegacy))
                .map(this::toWord)
                .collect(Collectors.toList());
    }

    public abstract List<Material> generateMaterials();
    protected abstract String getLangPrefix();

    protected GeneratedWord toWord(Material material) {
        Map<String, String> lang = GeneratorManager.getLangConfig();

        String defaultString = WbsEnums.toPrettyString(material);

        if (lang == null) {
            return new GeneratedWord(defaultString, this, getHint(material));
        }

        NamespacedKey key = material.getKey();
        String nameKey = getLangPrefix() + "." + key.getNamespace() + "." + key.getKey();

        String name = lang.get(nameKey);

        if (name == null) {
            return new GeneratedWord(defaultString, this, getHint(material));
        }

        return new GeneratedWord(name, this, getHint(material), true);
    }

    @Nullable
    protected String getHint(Material material) {
        List<MaterialProperty> properties = new ArrayList<>();
        for (MaterialProperty property : MaterialProperty.values()) {
            if (property.hasProperty(material)) {
                properties.add(property);
            }
        }

        if (properties.isEmpty()) {
            return null;
        }

        return WbsCollectionUtil.getRandom(properties).getHint();
    }
}
