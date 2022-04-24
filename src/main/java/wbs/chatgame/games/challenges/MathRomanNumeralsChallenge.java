package wbs.chatgame.games.challenges;

import org.jetbrains.annotations.NotNull;
import wbs.chatgame.GameController;
import wbs.chatgame.games.math.MathGame;
import wbs.chatgame.games.math.ViewableEquation;

public class MathRomanNumeralsChallenge extends MathGame implements Challenge<MathGame> {
    public MathRomanNumeralsChallenge(MathGame parent) {
        super(parent);
    }

    @Override
    public void broadcastEquation(ViewableEquation currentEquation) {
        if (currentEquation.customEquation()) {
            super.broadcastEquation(currentEquation);
        } else {
            broadcastQuestion("Solve \"&h" + currentEquation.equation().toString(true) +
                    "&r\" (roman numerals) for " + GameController.pointsDisplay(currentPoints) + "!");
        }
    }

    private String id;

    @Override
    public void setId(@NotNull String id) {
        this.id = id;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public Class<MathGame> getGameClass() {
        return MathGame.class;
    }
}
