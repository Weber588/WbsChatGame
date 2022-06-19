package wbs.chatgame.games.word.generator;

import org.bukkit.configuration.ConfigurationSection;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;

public abstract class KeyedWordGenerator extends WordGenerator {

    @Override
    protected List<GeneratedWord> generateWords() {
        ConfigurationSection lang = GeneratorManager.getLangConfig();

        List<GeneratedWord> words = new LinkedList<>();

        if (lang != null) {
            ConfigurationSection langSection = lang.getConfigurationSection(getLangPrefix());

            if (langSection != null) {
                for (String key : langSection.getKeys(true)) {
                    if (langSection.isConfigurationSection(key)) continue;

                    String defaultName = key.replace('_', ' ');
                    int lastIndex = defaultName.lastIndexOf('.');
                    if (lastIndex != -1) {
                        defaultName = defaultName.substring(lastIndex);
                    }

                    defaultName = WbsStrings.capitalizeAll(defaultName);

                    String name = langSection.getString(key, defaultName);

                    words.add(new GeneratedWord(name, this, true));
                }
            }
        }

        if (!words.isEmpty()) {
            return words;
        } else {
            return getDefault();
        }
    }

    protected abstract String getLangPrefix();
    protected abstract List<GeneratedWord> getDefault();
}
