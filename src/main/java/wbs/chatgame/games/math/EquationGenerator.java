package wbs.chatgame.games.math;

import org.bukkit.configuration.ConfigurationSection;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.math.operators.Operator;
import wbs.chatgame.games.math.operators.OperatorManager;
import wbs.chatgame.games.math.variables.StaticNumVariable;
import wbs.chatgame.games.math.variables.StringVariable;
import wbs.chatgame.games.math.variables.Variable;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.providers.generator.num.RandomGenerator;

import java.util.*;
import java.util.regex.Pattern;

public class EquationGenerator {

    public EquationGenerator(ConfigurationSection section, String directory) throws InvalidConfigurationException {
        ChatGameSettings settings = WbsChatGame.getInstance().settings;

        generatorName = section.getName();

        equationString = section.getString("equation");

        if (equationString == null) {
            settings.logError("Equation is a required field.", directory + "/equation");
            throw new InvalidConfigurationException("Equation is a required field.");
        }

        template = section.getString("template", equationString);
        custom = !template.equalsIgnoreCase(equationString);

        randomOperators.addAll(section.getStringList("random-operators"));

        ConfigurationSection variablesSection = section.getConfigurationSection("variables");
        if (variablesSection != null) {
            for (String key : variablesSection.getKeys(false)) {
                ConfigurationSection varSection = variablesSection.getConfigurationSection(key);

                Variable var;
                if (varSection == null) {
                    if (variablesSection.isDouble(key)) {
                        var = new StaticNumVariable(key, variablesSection.getDouble(key));
                    } else {
                        var = new StringVariable(variablesSection, key);
                    }
                } else {
                    var = new RandomNumVariable(
                            key,
                            new RandomGenerator(varSection, WbsChatGame.getInstance().settings, directory)
                    );
                }

                setVariable(var);
            }
        }
    }

    public EquationGenerator(List<String> options) {
        this(String.join(" ", options));
    }

    public EquationGenerator(String equationString) {
        generatorName = "Custom";

        this.equationString = equationString;
        template = equationString;
        custom = false;
    }

    private final String generatorName;
    private final String equationString;
    private final String template;
    private final boolean custom;
    private final Set<String> randomOperators = new HashSet<>();
    private final HashMap<String, Variable> variables = new HashMap<>();

    public void setVariable(Variable variable) {
        variables.put(variable.name(), variable);
    }

    private record EquationTemplatePair(String filledEquation, String filledTemplate) {}

    private EquationTemplatePair buildTemplatePair() {
        Map<String, String> placeholders = new HashMap<>();
        for (Variable var : variables.values()) {
            placeholders.put(var.name(), var.getValue());
        }

        String filledEquation = equationString;
        String filledTemplate = template;

        int escape = 0;
        do {
            for (Variable var : variables.values()) {
                String placeholder = placeholders.get(var.name());

                filledTemplate = filledTemplate.replaceAll(
                        Pattern.quote('%' + var.name() + '%'),
                        placeholder);

                filledEquation = filledEquation.replaceAll(
                        Pattern.quote('%' + var.name() + '%'),
                        placeholder);
            }
            escape++;
        } while (containsAnyPlaceholder(filledEquation, placeholders) && escape < 10);

        Operator defaultOperator = OperatorManager.getDefaultOperator();
        if (randomOperators.isEmpty()) {
            randomOperators.add(defaultOperator.asString());
        }
        while (filledEquation.contains("?")) {
            String chosenOperator = WbsCollectionUtil.getRandom(randomOperators);
            filledEquation = filledEquation.replaceFirst("\\?", chosenOperator);
            filledTemplate = filledTemplate.replaceFirst("\\?", chosenOperator);
        }

        return new EquationTemplatePair(filledEquation, filledTemplate);
    }

    private boolean containsAnyPlaceholder(String toCheck, Map<String, String> placeholders) {
        for (String name : placeholders.keySet()) {
            if (toCheck.contains("%" + name + "%")) {
                return true;
            }
        }
        return false;
    }

    public ViewableEquation getEquation(OperationSet operationSet) throws InvalidConfigurationException {
        EquationTemplatePair pair = buildTemplatePair();

        return new ViewableEquation(EquationUtils.parseEquation(pair.filledEquation, operationSet), pair.filledTemplate, custom);
    }

    public String getGeneratorName() {
        return generatorName;
    }
}
