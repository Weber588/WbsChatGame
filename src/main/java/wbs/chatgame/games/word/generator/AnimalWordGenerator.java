package wbs.chatgame.games.word.generator;

import org.bukkit.entity.Animals;
import org.jetbrains.annotations.NotNull;

public class AnimalWordGenerator extends EntityWordGenerator<Animals> {
    @Override
    protected @NotNull Class<Animals> getEntityClass() {
        return Animals.class;
    }
}
