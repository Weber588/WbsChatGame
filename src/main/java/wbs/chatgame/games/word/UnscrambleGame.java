package wbs.chatgame.games.word;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.chatgame.GameController;
import wbs.chatgame.WordUtil;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.challenges.Challenge;
import wbs.chatgame.games.challenges.ChallengeManager;
import wbs.chatgame.games.challenges.UnscrambleOnlinePlayer;
import wbs.chatgame.games.math.ConditionalPointsCalculator;
import wbs.chatgame.games.word.generator.GeneratedWord;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.plugin.WbsMessage;
import wbs.utils.util.string.WbsStrings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UnscrambleGame extends WordGame {
    public UnscrambleGame(String gameName, ConfigurationSection section, String directory) {
        super(gameName, section, directory);

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

        enabledHintTypes.addAll(copy.enabledHintTypes);
    }

    private boolean hintsEnabled = false;
    private boolean generatorHintsEnabled = true;
    private boolean enhancedGeneratorHints = true;
    private final int hintThreshold;
    private final int hintDelay;

    private final Set<HintType> enabledHintTypes = new HashSet<>();

    private String originalScramble = null;

    private int hintTaskId = -1;

    @Override
    public Game startGame(Word word) {
        originalScramble = WordUtil.scrambleString(word.word);
        broadcastScramble(originalScramble);

        if (hintsEnabled && getPoints() >= hintThreshold) {
            scheduleHint();
        }

        return this;
    }

    protected void broadcastScramble(String scrambledWord) {
        WbsMessage message = plugin.buildMessage("Unscramble \"")
                .appendRaw(scrambledWord)
                    .setFormatting("&h")
                .append("\" for "
                        + GameController.pointsDisplay(getPoints()) + "!")
                .build();

        broadcastQuestion(message);
    }

    @Override
    protected int calculateDefaultPoints(String word) {
        return WordUtil.scramblePoints(word);
    }

    private void scheduleHint() {
        hintTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                showHint();
            }
        }.runTaskLater(plugin, hintDelay).getTaskId();
    }

    private void showHint() {
        Set<HintType> possibleTypes = new HashSet<>();

        Word word = getCurrentWord();
        String current = word.word;
        if (current.contains(" ") && enabledHintTypes.contains(HintType.SCRAMBLE_INDIVIDUALLY)) {
            possibleTypes.add(HintType.SCRAMBLE_INDIVIDUALLY);
        } else {
            addHintIfEnabled(possibleTypes, HintType.SCRAMBLE_MULTIPLE);
            addHintIfEnabled(possibleTypes, HintType.REVEAL_ENDS);

            // If current doesn't contain any capital letters
            if (current.equals(current.toLowerCase())) {
                addHintIfEnabled(possibleTypes, HintType.CAPITALIZE_FIRST);
            }
        }

        if (word.generator != null && generatorHintsEnabled) {
            addHintIfEnabled(possibleTypes, HintType.GENERATOR_HINTS);
        }

        showHint(possibleTypes);
    }

    private void showHint(Set<HintType> possibleTypes) {
        Word word = getCurrentWord();
        String current = word.word;

        if (possibleTypes.isEmpty()) {
            plugin.logger.warning("No valid hint types! Skipping hints for " + gameName);
        } else {
            if (currentPoints > 1) {
                // int divide rounding up
                currentPoints = (currentPoints - 1) / 2 + 1;
            }
            HintType type = WbsCollectionUtil.getRandom(possibleTypes);
            switch (type) {
                case SCRAMBLE_INDIVIDUALLY -> {
                    String[] words = current.split(" ");
                    for (int i = 0; i < words.length; i++) {
                        words[i] = WordUtil.scrambleString(words[i]);
                    }
                    String hint = String.join(" ", words);
                    broadcastQuestion("Too hard? Here are the words scrambled individually: \"&h" + hint + "&r\" (" + GameController.pointsDisplay(getPoints()) + ")");
                }
                case CAPITALIZE_FIRST -> {
                    char firstLetter = Character.toUpperCase(current.charAt(0));

                    String rescrambled = WordUtil.scrambleString(WbsStrings.capitalize(getCurrentWord().word));
                    broadcastQuestion("Too hard? The first letter is " + firstLetter + "! \"&h" + rescrambled + "&r\" (" + GameController.pointsDisplay(getPoints()) + ")");
                }
                case SCRAMBLE_MULTIPLE -> {
                    int scrambles = 3;

                    String[] rescrambles = new String[scrambles];

                    for (int i = 0; i < scrambles; i++) {
                        rescrambles[i] = WordUtil.scrambleString(WbsStrings.capitalize(getCurrentWord().word));
                    }

                    String rescrambleString = String.join("&r\", \"&h", rescrambles);

                    broadcastQuestion("Too hard? Here it is scrambled a few ways differently: \"&h" + rescrambleString + "&r\" (" + GameController.pointsDisplay(getPoints()) + ")");
                }
                case REVEAL_ENDS -> {
                    int amountAtStart = 1;
                    if (!current.equals(current.toLowerCase())) {
                        amountAtStart++;
                    }

                    String start = current.substring(0, amountAtStart);
                    String end = current.substring(current.length() - 2);
                    String hintString = start + current.substring(amountAtStart, current.length() - 2).replaceAll(".?", "_") + end;

                    broadcastQuestion("Hint: \"&h" + hintString + "&r\" (" + GameController.pointsDisplay(getPoints()) + ")");
                }
                case GENERATOR_HINTS -> {
                    if (word instanceof GeneratedWord generatedWord) {
                        String hint = generatedWord.getHint();

                        if (hint == null) {
                            possibleTypes.remove(HintType.GENERATOR_HINTS);
                            showHint(possibleTypes);
                            return;
                        }

                        broadcastQuestion("Hint: " + hint + "! \"&h" + originalScramble + "&r\" (" + GameController.pointsDisplay(getPoints()) + ")");
                    } else {
                        plugin.logger.severe("Internal error. Generator hints was chosen as a hint type for a non-generated word.");
                        possibleTypes.remove(HintType.GENERATOR_HINTS);
                        showHint(possibleTypes);
                    }
                }
            }
        }
    }

    private void addHintIfEnabled(Set<HintType> set, HintType type) {
        if (enabledHintTypes.contains(type)) {
            set.add(type);
        }
    }

    @Override
    public void endNoWinner() {
        super.endNoWinner();
        cancelIfHintRunning();
    }

    @Override
    public void endWinner(Player player, String guess) {
        super.endWinner(player, guess);
        cancelIfHintRunning();
    }

    private void cancelIfHintRunning() {
        if (hintTaskId != -1) {
            Bukkit.getScheduler().cancelTask(hintTaskId);
            hintTaskId = -1;
        }
    }

    /**
     * Register this games challenges with the ChallengeManager, if it has any.
     */
    @Override
    public void registerChallenges() {
        super.registerChallenges();
        ChallengeManager.buildAndRegisterChallenge("randomplayer", this, UnscrambleOnlinePlayer.class);
    }

    private enum HintType {
        SCRAMBLE_INDIVIDUALLY, CAPITALIZE_FIRST, SCRAMBLE_MULTIPLE, REVEAL_ENDS, GENERATOR_HINTS
    }
}
