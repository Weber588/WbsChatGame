package wbs.chatgame.games.math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.games.GameQuestion;
import wbs.chatgame.games.QuestionGenerator;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsCollectionUtil;

import java.util.LinkedList;
import java.util.List;

public class MathQuestionGenerator extends QuestionGenerator<MathGame> {

    public MathQuestionGenerator(MathGame parent) {
        super(parent);
    }

    @Override
    public MathQuestion generateQuestion() {
        return generateWithOptions(new LinkedList<>());
    }

    @Override
    public @Nullable MathQuestion generateWithOptions(@NotNull List<String> options) {
        ViewableEquation equation = null;
        if (options.isEmpty()) {
            equation = generateEquation();
        } else {
            for (EquationGenerator generator : generatorsWithChances.keySet()) {
                if (generator.getGeneratorName().equalsIgnoreCase(options.get(0))) {
                    equation = generator.getEquation(operationSet);
                    break;
                }
            }

            if (equation == null) {
                EquationGenerator generator = new EquationGenerator(options);
                try {
                    equation = generator.getEquation(operationSet);
                } catch (InvalidConfigurationException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
        }

        if (equation == null) {
            throw new IllegalArgumentException("Equation failed to populate.");
        }

        return new MathQuestion(this, equation);
    }

    @NotNull
    private ViewableEquation generateEquation() throws IllegalArgumentException {
        ViewableEquation equation;
        try {
            equation = WbsCollectionUtil.getRandomWeighted(generatorsWithChances).getEquation(operationSet);
        } catch (InvalidConfigurationException e) {
            settings.logError(e.getMessage(), "Game config: " + getGameName());
            throw new IllegalArgumentException("Equation failed to generate: " + e.getMessage());
        }
        return equation;
    }

}
