package wbs.chatgame.games.word.generator;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsEnums;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class MaterialWordGenerator extends WordGenerator {

    @Override
    public final List<GeneratedWord> generateWords() {
        //noinspection deprecation
        return generateMaterials().stream()
                .filter(Predicate.not(Material::isLegacy))
                .map(material ->
                        new GeneratedWord(WbsEnums.toPrettyString(material), this, getHint(material)))
                .collect(Collectors.toList());
    }

    public abstract List<Material> generateMaterials();

    @Nullable
    private String getHint(Material material) {
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
