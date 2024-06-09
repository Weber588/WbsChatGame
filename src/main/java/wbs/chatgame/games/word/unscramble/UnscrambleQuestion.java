package wbs.chatgame.games.word.unscramble;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WordUtil;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.games.word.Word;
import wbs.chatgame.games.word.WordGameQuestion;
import wbs.chatgame.games.word.generator.GeneratedWord;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.plugin.WbsMessage;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.string.WbsStrings;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnscrambleQuestion extends WordGameQuestion<UnscrambleGame> {
    private String originalScramble = null;

    private int hintTaskId = -1;

    public UnscrambleQuestion(UnscrambleGame parent, Word word) {
        super(parent,
                word,
                parent.getDuration());
    }

    @Override
    public void start() {
        originalScramble = WordUtil.scrambleNicely(currentWord.word, preventSpacesOnEnds(), preventDoubleSpaces());
        currentDisplay = getDisplay(originalScramble);

        broadcastQuestion(currentDisplay);

        if (getHintsEnabled() && getPoints() >= getHintThreshold()) {
            scheduleHint();
        }
    }

    protected WbsMessage getDisplay(String scrambledWord) {
        return plugin.buildMessage("Unscramble \"")
                .appendRaw(scrambledWord)
                .setFormatting("&h")
                .append("\" for "
                        + GameController.pointsDisplay(getPoints()) + "!")
                .build();
    }


    private void showHint() {
        Set<UnscrambleGame.HintType> possibleTypes = new HashSet<>();

        String current = currentWord.word;
        if (current.contains(" ") && getEnabledHintTypes().contains(UnscrambleGame.HintType.SCRAMBLE_INDIVIDUALLY)) {
            possibleTypes.add(UnscrambleGame.HintType.SCRAMBLE_INDIVIDUALLY);
        } else {
            addHintIfEnabled(possibleTypes, UnscrambleGame.HintType.SCRAMBLE_MULTIPLE);
            addHintIfEnabled(possibleTypes, UnscrambleGame.HintType.REVEAL_ENDS);

            // If current doesn't contain any capital letters
            if (current.equals(current.toLowerCase())) {
                addHintIfEnabled(possibleTypes, UnscrambleGame.HintType.CAPITALIZE_FIRST);
            }
        }

        if (currentWord instanceof GeneratedWord && generatorHintsEnabled()) {
            addHintIfEnabled(possibleTypes, UnscrambleGame.HintType.GENERATOR_HINTS);
        }

        showHint(possibleTypes);
    }

    private void showHint(Set<UnscrambleGame.HintType> possibleTypes) {
        String current = currentWord.word;

        if (possibleTypes.isEmpty()) {
            plugin.logger.warning("No valid hint types! Skipping hints for " + parent.getGameName());
        } else {
            if (currentPoints > 1) {
                // int divide rounding up
            }
            UnscrambleGame.HintType type = WbsCollectionUtil.getRandom(possibleTypes);
            switch (type) {
                case SCRAMBLE_INDIVIDUALLY -> {
                    String[] words = current.split(" ");
                    for (int i = 0; i < words.length; i++) {
                        words[i] = WordUtil.scrambleString(words[i]);
                    }
                    String hint = String.join(" ", words);

                    WbsMessage message = plugin.buildMessage("Too hard? Here are the words scrambled individually: \"")
                            .appendRaw(hint)
                            .setFormatting("&h")
                            .append("\" (" + GameController.pointsDisplay(getPoints()) + ")")
                            .build();

                    broadcastQuestion(message);
                }
                case CAPITALIZE_FIRST -> {
                    char firstLetter = Character.toUpperCase(current.charAt(0));

                    WbsMessage message = plugin.buildMessage("Too hard? The first letter is " + firstLetter + "! \"")
                            .appendRaw(originalScramble)
                            .setFormatting("&h")
                            .append("\" (" + GameController.pointsDisplay(getPoints()) + ")")
                            .build();

                    broadcastQuestion(message);
                }
                case SCRAMBLE_MULTIPLE -> {
                    int scrambles = 3;

                    WbsMessageBuilder message = plugin.buildMessage("Too hard? Here it is scrambled a few ways differently: \"");

                    for (int i = 0; i < scrambles; i++) {
                        String rescramble = WordUtil.scrambleString(WbsStrings.capitalize(currentWord.word));

                        message.appendRaw(rescramble)
                                .setFormatting("&h");

                        if (i < scrambles - 1) {
                            message.append("\", \"");
                        }
                    }

                    message.append("\" (" + GameController.pointsDisplay(getPoints()) + ")");

                    broadcastQuestion(message.build());
                }
                case REVEAL_ENDS -> {
                    int amountAtStart = 1;
                    if (!current.equals(current.toLowerCase())) {
                        amountAtStart++;
                    }

                    String start = current.substring(0, amountAtStart);
                    String end = current.substring(current.length() - 2);
                    String hintString = start + current.substring(amountAtStart, current.length() - 2).replaceAll(".?", "_") + end;

                    WbsMessage message = plugin.buildMessage("Unscramble \"")
                            .appendRaw(originalScramble)
                            .setFormatting("&h")
                            .append("\" for "
                                    + GameController.pointsDisplay(getPoints()) + "! (Hint: \"")
                            .appendRaw(hintString)
                            .setFormatting("&h")
                            .append("\")")
                            .build();

                    broadcastQuestion(message);
                }
                case GENERATOR_HINTS -> {
                    if (currentWord instanceof GeneratedWord generatedWord) {
                        String hint = generatedWord.getHint();

                        if (hint == null) {
                            possibleTypes.remove(UnscrambleGame.HintType.GENERATOR_HINTS);
                            showHint(possibleTypes);
                            return;
                        }

                        WbsMessage message = plugin.buildMessage("Hint: " + hint + "! \"")
                                .appendRaw(originalScramble)
                                .setFormatting("&h")
                                .append("\" (" + GameController.pointsDisplay(getPoints()) + ")")
                                .build();

                        broadcastQuestion(message);
                    } else {
                        plugin.logger.severe("Internal error. Generator hints was chosen as a hint type for a non-generated word.");
                        possibleTypes.remove(UnscrambleGame.HintType.GENERATOR_HINTS);
                        showHint(possibleTypes);
                    }
                }
            }
        }
    }

    private void scheduleHint() {
        hintTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                showHint();
            }
        }.runTaskLater(plugin, getHintDelay()).getTaskId();
    }

    private int getHintDelay() {
        return parent.hintDelay();
    }

    private void addHintIfEnabled(Set<UnscrambleGame.HintType> set, UnscrambleGame.HintType type) {
        if (getEnabledHintTypes().contains(type)) {
            set.add(type);
        }
    }

    @Override
    protected void onRoundEnd(@Nullable Player winner, @Nullable String guess, @Nullable Double finalDuration) {
        super.onRoundEnd(winner, guess, finalDuration);
        cancelIfHintRunning();
    }

    private void cancelIfHintRunning() {
        if (hintTaskId != -1) {
            Bukkit.getScheduler().cancelTask(hintTaskId);
            hintTaskId = -1;
        }
    }

    private int getHintThreshold() {
        return parent.hintThreshold;
    }

    private boolean getHintsEnabled() {
        return parent.hintsEnabled;
    }

    protected boolean preventDoubleSpaces() {
        return parent.preventDoubleSpaces();
    }

    protected boolean preventSpacesOnEnds() {
        return parent.preventSpacesOnEnds();
    }

    private boolean generatorHintsEnabled() {
        return parent.generatorHintsEnabled();
    }

    private List<UnscrambleGame.HintType> getEnabledHintTypes() {
        return parent.getEnabledHintTypes();
    }
}
