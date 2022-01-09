package wbs.chatgame.games;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.WordUtil;
import wbs.chatgame.games.math.MathGame;
import wbs.chatgame.games.trivia.TriviaGame;
import wbs.chatgame.games.word.QuickTypeGame;
import wbs.chatgame.games.word.RevealGame;
import wbs.chatgame.games.word.UnscrambleGame;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsCollectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GameManager {
    private static final Map<String, Class<? extends Game>> registeredGames = new HashMap<>();

    private static final Map<String, Game> games = new HashMap<>();
    private static final Map<Game, Double> chances = new HashMap<>();

    public static void registerNativeGames() {
        registerGame("unscramble", UnscrambleGame.class);
        registerGame("trivia", TriviaGame.class);
        registerGame("quicktype", QuickTypeGame.class);
        registerGame("reveal", RevealGame.class);
        registerGame("math", MathGame.class);
    }

    public static void registerGame(String id, Class<? extends Game> clazz) {
        registeredGames.put(WordUtil.stripSyntax(id), clazz);
    }

    @Nullable
    public static String getRegistrationId(Game game) {
        for (String id : registeredGames.keySet()) {
            if (registeredGames.get(id).equals(game.getClass())) {
                return id;
            }
        }

        return null;
    }

    @Nullable
    public static Game newGame(String gameName, YamlConfiguration specs, String directory) throws InvalidConfigurationException {
        ChatGameSettings settings = WbsChatGame.getInstance().settings;

        String typeString = specs.getKeys(false).toArray(new String[0])[0];
        ConfigurationSection section = specs.getConfigurationSection(typeString);

        Class<? extends Game> gameClass = registeredGames.get(WordUtil.stripSyntax(typeString));

        if (gameClass == null) {
            settings.logError("Invalid game type: " + typeString, directory);
            throw new InvalidConfigurationException();
        }

        Game game;
        try {
            Constructor<? extends Game> constructor = gameClass.getConstructor(String.class, ConfigurationSection.class, String.class);
            game = constructor.newInstance(gameName, section, directory);

        } catch (SecurityException | NoSuchMethodException | InstantiationException
                | IllegalAccessException | IllegalArgumentException e) {
            settings.logError("Invalid constructor for game type " + typeString, directory);
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e){
            Throwable cause = e.getCause();
            if (!(cause instanceof InvalidConfigurationException)) {
                settings.logError("An error occurred while constructing " + typeString, directory);
                e.printStackTrace();
            }
            return null;
        }

        addGame(gameName, game);
        return game;
    }

    private static void addGame(String name, Game game) {
        name = WordUtil.stripSyntax(name);
        if (games.containsKey(name))
            throw new IllegalArgumentException("Name \"" + name + "\" already registered to game of type " + game.getClass().getSimpleName());

        games.put(name, game);
    }

    public static void setChance(String id, double chance) {
        Game game = getGame(id);
        if (game == null) throw new IllegalArgumentException("Game not defined: " + id);
        chances.put(game, chance);
    }

    public static void setChance(Game game, double chance) {
        chances.put(game, chance);
    }

    @Nullable
    public static Game getGame(String id) {
        return games.get(WordUtil.stripSyntax(id));
    }

    @NotNull
    public static Game getRandomGame() {
        return WbsCollectionUtil.getRandomWeighted(chances);
    }

    public static void clear() {
        games.clear();
        chances.clear();
    }

    public static List<Game> getGames() {
        return new LinkedList<>(games.values());
    }

    public static boolean isChanceSet(Game game) {
        return chances.containsKey(game);
    }
}
