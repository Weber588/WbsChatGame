package wbs.chatgame;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.data.ChatGameDB;
import wbs.chatgame.data.LegacyDataAdapter;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.chatgame.games.word.generator.GeneratorManager;
import wbs.chatgame.rewards.RewardManager;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.plugin.WbsSettings;

import java.io.*;
import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

public class ChatGameSettings extends WbsSettings {
    protected ChatGameSettings(WbsChatGame plugin) {
        super(plugin);
    }

    @Override
    public void reload() {
        errors.clear();
        YamlConfiguration config = loadDefaultConfig("config.yml");
        YamlConfiguration generatorConfig = this.loadConfigSafely(this.genConfig("generators.yml"));
        YamlConfiguration rewardsConfig = this.loadConfigSafely(this.genConfig("rewards.yml"));

        GameManager.clear();
        loadGames();
        RewardManager.reloadRewards(rewardsConfig);

        String settingsDir = "config.yml/settings";
        ConfigurationSection settingsSection = config.getConfigurationSection("settings");
        boolean ratesSet = false;
        if (settingsSection != null) {
            requireGuessCommand = settingsSection.getBoolean("require-guess-command", requireGuessCommand);
            listenByDefault = settingsSection.getBoolean("listen-by-default", listenByDefault);
            debugMode = settingsSection.getBoolean("debug-mode", debugMode);
            String langFileName = settingsSection.getString("minecraft-lang", "");

            if (!langFileName.isBlank()) {
                loadLanguageFile(langFileName);
            }

            ChatGameDB.statsTable.setDebugMode(debugMode);
            ChatGameDB.playerTable.setDebugMode(debugMode);
            ChatGameDB.datesTable.setDebugMode(debugMode);

            loadResetSettings(settingsSection, settingsDir);

            GameController.roundDelay = (int) (settingsSection.getDouble("seconds-between-rounds", 180) * 20);

            ConfigurationSection ratesSection = settingsSection.getConfigurationSection("rates");
            if (ratesSection != null) {
                ratesSet = true;
                loadRates(ratesSection);
            }

            boolean loadLegacyData = settingsSection.getBoolean("load-legacy-data", false);
            if (loadLegacyData) {
                LegacyDataAdapter.loadLegacyData();
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

    private void loadLanguageFile(String langFileName) {
        File langFile = new File(plugin.getDataFolder(), langFileName);

        if (!langFile.exists()) {
            plugin.logger.info("Language file not found: \"" + langFileName +
                    "\". Consider creating it for more reliable word generators!");
            return;
        }

        try {
            JsonReader reader = new JsonReader(new FileReader(langFile));
            Type type = new TypeToken<Map<String, String>>(){}.getType();

            Map<String, String> langConfig = new Gson().fromJson(reader, type);

            LangUtil.registerLangMap(langConfig);
        } catch (IOException e) {
            plugin.logger.info("Failed to load language file!");
            e.printStackTrace();
        }
    }

    private void loadResetSettings(ConfigurationSection section, String directory) {
        String resetDayString = section.getString("reset-day", resetDay.name());
        resetDay = WbsEnums.getEnumFromString(DayOfWeek.class, resetDayString);
        if (resetDay == null) {
            logError("Invalid reset day: " + resetDayString, directory + "/reset-day");
            resetDay = DayOfWeek.SUNDAY;
        }

        String resetTimeString = section.getString("reset-time",
                (resetTime.getHour() + 1) + ":" + (resetTime.getMinute() + 1));

        String[] args = resetTimeString.split(":");

        int hour;
        int minutes = 0;
        boolean pmTime = false;

        try {
            hour = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            logError("Invalid hour: " + args[0], directory + "/reset-time");
            return;
        }

        if (hour < 1 || hour > 24) {
            logError("Invalid hour: " + args[0], directory + "/reset-time");
            return;
        }

        if (args.length > 1) {
            String minutesString = args[1];
            if (minutesString.length() > 2) {
                String substring = minutesString.substring(0, minutesString.length() - 2);
                if (minutesString.toLowerCase().endsWith("pm")) {
                    pmTime = true;
                    minutesString = substring;
                } else  if (minutesString.toLowerCase().endsWith("am")) {
                    minutesString = substring;
                }
            }

            try {
                minutes = Integer.parseInt(minutesString);
            } catch (NumberFormatException e) {
                logError("Invalid minutes: " + minutesString, directory + "/reset-time");
                return;
            }

            if (minutes < 0 || minutes > 59) {
                logError("Invalid minutes: " + minutesString, directory + "/reset-time");
                return;
            }
        }

        if (pmTime) {
            hour += 12;
        }

        hour %= 24;

        resetTime = LocalTime.of(hour, minutes);
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

    public DayOfWeek resetDay = DayOfWeek.SUNDAY;
    public LocalTime resetTime = LocalTime.of(12, 0);

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
                if (game != null) {
                    GameManager.addGame(gameName, game);
                    gamesLoaded++;
                }
            } catch (InvalidConfigurationException ignored) {}
        }

        if (!errors.isEmpty()) {
            logger.warning("The games were loaded with " + errors.size() + " error(s). Do /cg errors to view them.");
        }

        if (gamesLoaded >= 0) {
            logger.info(gamesLoaded + " games loaded.");
        }
    }
}
