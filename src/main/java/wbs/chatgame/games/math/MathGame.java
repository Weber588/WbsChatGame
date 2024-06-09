package wbs.chatgame.games.math;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameQuestion;
import wbs.chatgame.games.QuestionGenerator;
import wbs.chatgame.games.challenges.ChallengeManager;
import wbs.chatgame.games.challenges.math.MathRomanNumeralAnswer;
import wbs.chatgame.games.challenges.math.MathRomanNumeralBoth;
import wbs.chatgame.games.challenges.math.MathRomanNumeralQuestion;
import wbs.chatgame.games.math.functions.CGFunction;
import wbs.chatgame.games.math.functions.FunctionManager;
import wbs.chatgame.games.math.operators.Operator;
import wbs.chatgame.games.math.operators.OperatorManager;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsCollectionUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MathGame extends Game<MathGame> {

    public MathGame(String gameName, ConfigurationSection section, String directory) {
        super(gameName, section, directory);

        ChatGameSettings settings = WbsChatGame.getInstance().settings;

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

        operationSet = new OperationSet();

        ConfigurationSection operatorsSection = section.getConfigurationSection("operators");
        if (operatorsSection != null) {
            Map<String, Class<? extends Operator>> operatorMap = OperatorManager.getRegistrations();
            for (String operationKey : operatorMap.keySet()) {
                ConfigurationSection operationSection = operatorsSection.getConfigurationSection(operationKey);

                if (operationSection == null) {
                    continue;
                }

                Operator operator = OperatorManager.getOperator(operatorMap.get(operationKey));

                ConditionalPointsCalculator calculator =
                        new ConditionalPointsCalculator(operationSection,
                                directory + "/operators/" + operationKey);

                operationSet.addOperator(operator, calculator);

            }
        } else {
            operationSet.populateDefaultOperations();
        }

        ConfigurationSection functionsSection = section.getConfigurationSection("functions");
        if (functionsSection != null) {
            for (String functionKey : functionsSection.getKeys(false)) {
                Class<? extends CGFunction> function = FunctionManager.getFunctionClass(functionKey);

                if (function == null) {
                    settings.logError("Invalid function: \"" + functionKey + "\".", directory + "/functions/" + functionKey);
                    continue;
                }

                ConfigurationSection functionSection = functionsSection.getConfigurationSection(functionKey);
                assert functionSection != null;

                ConditionalPointsCalculator calculator =
                        new ConditionalPointsCalculator(functionSection, directory + "/functions/" + functionKey);

                operationSet.registerFunction(function, calculator);
            }
        } else {
            operationSet.populateDefaultFunctions();
        }
    }

    @Override
    protected @NotNull QuestionGenerator<MathGame> getDefaultGenerator() {
        return new MathQuestionGenerator(this);
    }

    public MathGame(MathGame copy) {
        super(copy);
        this.operationSet = copy.operationSet;
        generatorsWithChances.putAll(copy.generatorsWithChances);
    }

    private final Map<EquationGenerator, Double> generatorsWithChances = new HashMap<>();

    private final OperationSet operationSet;

    @Override
    public List<String> getOptionCompletions() {
        return generatorsWithChances.keySet().stream()
                .map(EquationGenerator::getGeneratorName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    @Override
    public void registerChallenges() {
        ChallengeManager.buildAndRegisterChallenge("roman_numerals_question", this, MathRomanNumeralQuestion.class);
        ChallengeManager.buildAndRegisterChallenge("roman_numerals_answer", this, MathRomanNumeralAnswer.class);
        ChallengeManager.buildAndRegisterChallenge("roman_numerals", this, MathRomanNumeralBoth.class);
    }
}
