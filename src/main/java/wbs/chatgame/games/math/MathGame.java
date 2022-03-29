package wbs.chatgame.games.math;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.GameController;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.Game;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsMath;

import java.util.*;
import java.util.stream.Collectors;

public class MathGame extends Game {

    public MathGame(String gameName, ConfigurationSection section, String directory) {
        super(gameName, section, directory);

        ChatGameSettings settings = WbsChatGame.getInstance().settings;

        // TODO: Load generatorsWithChances

        ConfigurationSection generatorSection = section.getConfigurationSection("generators");
        if (generatorSection != null) {
            String genDir = directory + "/generators";
            for (String key : generatorSection.getKeys(false)) {
                ConfigurationSection genSection = generatorSection.getConfigurationSection(key);
                if (genSection == null) {
                    settings.logError("Generator must be a section: " + key, genDir + "/" + key);
                    continue;
                }

                EquationGenerator generator;
                try {
                    generator = new EquationGenerator(genSection, genDir + "/" + key);
                } catch (InvalidConfigurationException e) {
                    continue;
                }

                double chance = genSection.getDouble("chance", 20);

                generatorsWithChances.put(generator, chance);
            }
        }

        if (generatorsWithChances.isEmpty()) {
            settings.logError("No equation generators defined for " + getGameName() + "; game has been disabled.", directory);
            throw new InvalidConfigurationException();
        } else {
            plugin.logger.info("Loaded " + generatorsWithChances.size() + " equation generators for " + getGameName());
        }
    }

    private ViewableEquation currentEquation;
    private Solution currentSolution;
    private final Map<EquationGenerator, Double> generatorsWithChances = new HashMap<>();

    @Override
    public boolean checkGuess(String guess) {
        double numberGuess;
        try {
            numberGuess = Double.parseDouble(guess);
        } catch (NumberFormatException e) {
            return false;
        }

        return WbsMath.roundTo(numberGuess, 2) == WbsMath.roundTo(currentSolution.value(), 2);
    }

    @Override
    protected void start() {
        startWithOptions(new LinkedList<>());
    }

    @Override
    public @NotNull Game startWithOptions(@NotNull List<String> options) {
        if (options.isEmpty()) {
            currentEquation = generateQuestion();
        } else {
            for (EquationGenerator generator : generatorsWithChances.keySet()) {
                if (generator.getGeneratorName().equalsIgnoreCase(options.get(0))) {
                    currentEquation = generator.getEquation();
                }
            }

            if (currentEquation == null) {
                EquationGenerator generator = new EquationGenerator(options);
                try {
                    currentEquation = generator.getEquation();
                } catch (InvalidConfigurationException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
        }

        if (currentEquation == null) {
            GameController.skip();
            return this;
        }
        try {
            currentSolution = currentEquation.equation().solve();
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        currentPoints = currentSolution.points();

        String equationString = currentEquation.asString();
        if (currentEquation.customEquation()) {
            broadcastQuestion(equationString + " &h(" + GameController.pointsDisplay(currentPoints) + ")");
        } else {
            broadcastQuestion("Solve \"&h" + equationString + "&r\" for " + GameController.pointsDisplay(currentPoints) + "!");
        }

        return this;
    }

    @Nullable
    private ViewableEquation generateQuestion() {
        ViewableEquation equation;
        try {
            equation = WbsCollectionUtil.getRandomWeighted(generatorsWithChances).getEquation();
        } catch (InvalidConfigurationException e) {
            settings.logError(e.getMessage(), "Game config: " + getGameName());
            return null;
        }
        return equation;
    }

    @Override
    public void endNoWinner() {
        if (currentEquation != null) {
            GameController.broadcast("Nobody answered in time. The answer was: &h"
                    + formatAnswer());
        }
        currentEquation = null;
    }

    @Override
    public void endWinner(Player player, String guess) {
        GameController.broadcast(player.getName() + " won in " + GameController.getLastRoundStartedString() + "! The answer was: &h" + formatAnswer());
        currentEquation = null;
    }

    private String formatAnswer() {
        double rounded = WbsMath.roundTo(currentSolution.value(), 2);
        if (currentSolution.value() == rounded) {
            return (int) rounded + "";
        }
        return rounded + "";
    }

    @Override
    public List<String> getAnswers() {
        return Collections.singletonList(formatAnswer());
    }

    @Override
    public List<String> getOptionCompletions() {
        return generatorsWithChances.keySet().stream()
                .map(EquationGenerator::getGeneratorName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }
}
