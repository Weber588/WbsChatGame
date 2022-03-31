package wbs.chatgame.games.word.generator;

import org.bukkit.StructureType;
import wbs.utils.util.string.WbsStrings;

import java.util.List;
import java.util.stream.Collectors;

public class StructureWordGenerator extends SimpleWordGenerator {
    @Override
    public List<String> generateStrings() {
        return StructureType.getStructureTypes()
                .keySet()
                .stream()
                .map(type -> type.replace('_', ' '))
                .map(WbsStrings::capitalizeAll)
                .collect(Collectors.toList());
    }
}
