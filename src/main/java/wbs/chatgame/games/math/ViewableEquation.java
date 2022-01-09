package wbs.chatgame.games.math;

import org.jetbrains.annotations.Nullable;

public record ViewableEquation(Equation equation, @Nullable String asString, boolean customEquation) {
}
