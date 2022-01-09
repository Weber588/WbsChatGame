package wbs.chatgame.games.word.generator;

import org.bukkit.entity.Monster;
import org.jetbrains.annotations.NotNull;

public class MonsterWordGenerator extends EntityWordGenerator<Monster> {
    @Override
    protected @NotNull Class<Monster> getEntityClass() {
        return Monster.class;
    }
}
