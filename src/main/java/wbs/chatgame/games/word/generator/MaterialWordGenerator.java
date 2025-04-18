package wbs.chatgame.games.word.generator;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.LangUtil;
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
        return generateMaterials().stream()
                .filter(Predicate.not(Material::isLegacy))
                .map(this::toWord)
                .collect(Collectors.toList());
    }

    public abstract List<Material> generateMaterials();
    protected abstract String getLangPrefix();

    protected GeneratedWord toWord(Material material) {
        return new GeneratedWord(LangUtil.getMaterialName(material), 0, this, getHint(material), false);
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
