package wbs.chatgame.games.word.generator;

import org.bukkit.Material;
import wbs.utils.util.WbsEnums;

import java.util.List;
import java.util.stream.Collectors;

public abstract class MaterialWordGenerator extends WordGenerator {
    @Override
    public final List<String> generateWords() {
        return generateMaterials().stream()
                .map(WbsEnums::toPrettyString)
                .collect(Collectors.toList());
    }

    public abstract List<Material> generateMaterials();
}
