package wbs.chatgame;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.chatgame.games.word.generator.GeneratorManager;
import wbs.chatgame.rewards.RewardManager;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.plugin.WbsSettings;

import java.io.File;
import java.util.*;

public class ChatGameSettings extends WbsSettings {
    protected ChatGameSettings(WbsChatGame plugin) {
        super(plugin);
    }

    private YamlConfiguration config = null;
    private YamlConfiguration generatorConfig = null;
    private YamlConfiguration rewardsConfig = null;

    @Override
    public void reload() {
        errors.clear();
        config = loadDefaultConfig("config.yml");
        generatorConfig = this.loadConfigSafely(this.genConfig("generators.yml"));
        rewardsConfig = this.loadConfigSafely(this.genConfig("rewards.yml"));

        GameManager.clear();
        loadGames();
        RewardManager.reloadRewards(rewardsConfig);

        ConfigurationSection settingsSection = config.getConfigurationSection("settings");
        boolean ratesSet = false;
        if (settingsSection != null) {
            requireGuessCommand = settingsSection.getBoolean("require-guess-command", requireGuessCommand);
            listenByDefault = settingsSection.getBoolean("listen-by-default", listenByDefault);
            debugMode = settingsSection.getBoolean("debug-mode", debugMode);
            GameController.roundDelay = (int) (settingsSection.getDouble("seconds-between-rounds", 180) * 20);

            ConfigurationSection ratesSection = settingsSection.getConfigurationSection("rates");
            if (ratesSection != null) {
                ratesSet = true;
                loadRates(ratesSection);
            }
        }

        double defaultChance = 20;
        if (!ratesSet) {
            for (Game game : GameManager.getGames()) {
                GameManager.setChance(game, defaultChance);
            }
        } else {
            for (Game game : GameManager.getGames()) {
                if (!GameManager.isChanceSet(game)) {
                    GameManager.setChance(game, defaultChance);
                    logger.warning("No chance set for " + game.getGameName() + ". Defaulting to " + defaultChance + ".");
                }
            }
        }

        GeneratorManager.configureRegistered(generatorConfig, "generators.yml");
    }

    private void loadRates(ConfigurationSection ratesSection) {
        for (String gameName : ratesSection.getKeys(false)) {
            String directory = "config.yml/settings/rates/" + gameName;
            double chance = ratesSection.getDouble("chance", 20);

            if (chance <= 0) continue;

            Game game = GameManager.getGame(gameName);
            if (game == null) {
                logError("Game not found: " + gameName, directory);
                continue;
            }
            GameManager.setChance(game, chance);
        }
    }

    public boolean requireGuessCommand = false;
    public boolean listenByDefault = true;
    public boolean debugMode = false;

    private final List<File> gameFiles = new ArrayList<>();

    private void loadGames() {
        final File gamesDir =  new File(plugin.getDataFolder() + File.separator + "games");

        genConfig("games" + File.separator + "trivia.yml");
        genConfig("games" + File.separator + "unscramble.yml");
        genConfig("games" + File.separator + "quicktype.yml");
        genConfig("games" + File.separator + "reveal.yml");
        genConfig("games" + File.separator + "math.yml");

        gameFiles.clear();
        for (File file : Objects.requireNonNull(gamesDir.listFiles())) {
            if (file.getName().endsWith(".yml")) {
                gameFiles.add(file);
            }
        }

        int gamesLoaded = 0;

        for (File gameFile : gameFiles) {
            YamlConfiguration specs = loadConfigSafely(gameFile);
            String gameName = gameFile.getName().substring(0, gameFile.getName().lastIndexOf('.'));

            try {
                Game game = GameManager.createGame(gameName, specs, gameFile.getName());
                GameManager.addGame(gameName, game);
                gamesLoaded++;
            } catch (InvalidConfigurationException ignored) {}
        }

        if (errors.size() != 0) {
            logger.warning("The games were loaded with " + errors.size() + " error(s). Do /cg errors to view them.");
        }

        if (gamesLoaded >= 0) {
            logger.info(gamesLoaded + " games loaded.");
        }
    }
}
