package wbs.chatgame.games.word.unscramble;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.WordUtil;
import wbs.chatgame.games.challenges.ChallengeManager;
import wbs.chatgame.games.challenges.unscramble.UnscrambleOnlinePlayer;
import wbs.chatgame.games.word.Word;
import wbs.chatgame.games.word.WordGame;
import wbs.utils.util.WbsEnums;

import java.util.*;

public class UnscrambleGame extends WordGame<UnscrambleGame> {
    @SuppressWarnings("unused") // Accessed reflectively
    public UnscrambleGame(String gameName, ConfigurationSection section, String directory) {
        super(gameName, section, directory);

        ConfigurationSection scrambleSettings = section.getConfigurationSection("scramble-settings");
        if (scrambleSettings != null) {
            preventDoubleSpaces = scrambleSettings.getBoolean("prevent-double-spaces", preventDoubleSpaces);
            preventSpacesOnEnds = scrambleSettings.getBoolean("prevent-spaces-on-ends", preventSpacesOnEnds);
        }

        ConfigurationSection hintSection = section.getConfigurationSection("hints");
        if (hintSection == null) {
            hintsEnabled = false;
            hintThreshold = 0;
            hintDelay = 0;
        } else {
            hintsEnabled = hintSection.getBoolean("enabled", hintsEnabled);
            generatorHintsEnabled = hintSection.getBoolean("generator-hints", generatorHintsEnabled);
            enhancedGeneratorHints = hintSection.getBoolean("enhanced-material-hints", enhancedGeneratorHints);
            hintThreshold = hintSection.getInt("point-threshold", 0);
            hintDelay = (int) (hintSection.getDouble("delay", (getDuration() / 20.0) * 2.0 / 3.0) * 20);

            ConfigurationSection typeSection = hintSection.getConfigurationSection("hint-types");
            if (typeSection == null) {
                enabledHintTypes.addAll(Arrays.asList(HintType.values()));
            } else {
                for (String key : typeSection.getKeys(false)) {
                    String typeDirectory = directory + "/hints/hint-types";
                    HintType type = WbsEnums.getEnumFromString(HintType.class, key);
                    if (type == null) {
                        settings.logError("Invalid hint type: " + key, typeDirectory);
                        continue;
                    }

                    if (!typeSection.isBoolean(key)) {
                        settings.logError("Invalid boolean: " + typeSection.getString(key), typeDirectory + "/" + key);
                        continue;
                    }

                    if (typeSection.getBoolean(key)) {
                        enabledHintTypes.add(type);
                    }
                }
            }
        }
    }

    public UnscrambleGame(UnscrambleGame copy) {
        super(copy);

        hintsEnabled = copy.hintsEnabled;
        hintDelay = copy.hintDelay;
        hintThreshold = copy.hintThreshold;
        generatorHintsEnabled = copy.generatorHintsEnabled;
        enhancedGeneratorHints = copy.enhancedGeneratorHints;

        preventDoubleSpaces = copy.preventDoubleSpaces;
        preventSpacesOnEnds = copy.preventSpacesOnEnds;

        enabledHintTypes.addAll(copy.enabledHintTypes);
    }

    boolean hintsEnabled = false;
    private boolean generatorHintsEnabled = true;
    private boolean enhancedGeneratorHints = true;
    final int hintThreshold;
    private final int hintDelay;

    private boolean preventDoubleSpaces = true;
    private boolean preventSpacesOnEnds = true;

    private final Set<HintType> enabledHintTypes = new HashSet<>();

    @Override
    public @NotNull UnscrambleQuestion generateQuestion(Word word) {

        return new UnscrambleQuestion(this, word);
    }

    @Override
    protected int calculateDefaultPoints(String word) {
        return WordUtil.scramblePoints(word);
    }

    /**
     * Register this games challenges with the ChallengeManager, if it has any.
     */
    @Override
    public void registerChallenges() {
        super.registerChallenges();
        ChallengeManager.buildAndRegisterChallenge("randomplayer", this, UnscrambleOnlinePlayer.class);
    }

    boolean preventDoubleSpaces() {
        return preventDoubleSpaces;
    }

    boolean preventSpacesOnEnds() {
        return preventSpacesOnEnds;
    }
    
    int hintThreshold() {
        return hintThreshold;
    }
    boolean hintsEnabled() {
        return hintsEnabled;
    }

    List<HintType> getEnabledHintTypes() {
        return new LinkedList<>(enabledHintTypes);
    }

    boolean generatorHintsEnabled() {
        return generatorHintsEnabled;
    }

    int hintDelay() {
        return hintDelay;
    }

    enum HintType {
        SCRAMBLE_INDIVIDUALLY, CAPITALIZE_FIRST, SCRAMBLE_MULTIPLE, REVEAL_ENDS, GENERATOR_HINTS
    }
}
