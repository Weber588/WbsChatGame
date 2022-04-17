package wbs.chatgame.games.math.variables;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsCollectionUtil;

import java.util.LinkedList;
import java.util.List;

public class StringVariable extends Variable {

    private final List<String> strings = new LinkedList<>();
    public StringVariable(ConfigurationSection section, String key) {
        super(key);
        List<String> stringList = section.getStringList(key);

        if (!stringList.isEmpty()) {
            strings.addAll(stringList);
        } else {
            String string = section.getString(key);
            strings.add(string);
        }

        if (strings.isEmpty()) {
            throw new InvalidConfigurationException("Unexpected config error. Please report this issue.");
        }
    }

    @Override
    public @NotNull String getValue() {
        return WbsCollectionUtil.getRandom(strings);
    }
}
