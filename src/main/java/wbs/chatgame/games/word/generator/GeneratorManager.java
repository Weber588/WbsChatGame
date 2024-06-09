package wbs.chatgame.games.word.generator;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.WordUtil;

import java.io.File;
import java.util.*;

public final class GeneratorManager {
    private GeneratorManager() {}

    private static final Map<String, WordGenerator> generators = new HashMap<>();
    private static final Map<WordGenerator, String> generatorIds = new HashMap<>();

    static {
        registerGenerator("animal", new AnimalWordGenerator());
        registerGenerator("biome", new BiomeWordGenerator());
        registerGenerator("block", new BlockWordGenerator());
        registerGenerator("enchantment", new EnchantmentWordGenerator());
        registerGenerator("item", new ItemWordGenerator());
        registerGenerator("monster", new MonsterWordGenerator());
        registerGenerator("potion", new PotionWordGenerator());
        registerGenerator("structure", new StructureWordGenerator());
        registerGenerator("advancement-name", new AdvancementNameGenerator());
    }

    public static void registerGenerator(String id, WordGenerator generator) {
        id = WordUtil.stripSyntax(id);
        if (generators.containsKey(id)) {
            throw new IllegalArgumentException("Id already registered: " + id);
        }
        if (generatorIds.containsKey(generator)) {
            throw new IllegalArgumentException("Generator already registered: " + generator);
        }

        generators.put(id, generator);
        generatorIds.put(generator, id);
    }

    @Nullable
    public static WordGenerator getGenerator(String id) {
        return generators.get(WordUtil.stripSyntax(id));
    }

    @Nullable
    public static String getRegisteredId(WordGenerator generator) {
        return generatorIds.get(generator);
    }

    @NotNull
    public static List<String> getIds() {
        return new LinkedList<>(generators.keySet());
    }

    public static void configureRegistered(ConfigurationSection section, String directory) {
        for (String key : section.getKeys(false)) {
            WordGenerator generator = generators.get(key);

            if (generator == null) {
                WbsChatGame.getInstance().settings.logError("Invalid generator: " + key, directory + "/" + key);
                continue;
            }

            if (!section.isConfigurationSection(key)) {
                WbsChatGame.getInstance().settings.logError("Generator settings must be a section: " + key, directory + "/" + key);
                continue;
            }

            ConfigurationSection genSection = section.getConfigurationSection(key);
            assert genSection != null;

            generator.configure(genSection);
        }
    }
}
