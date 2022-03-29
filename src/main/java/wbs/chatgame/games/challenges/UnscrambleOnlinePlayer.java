package wbs.chatgame.games.challenges;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import wbs.chatgame.GameController;
import wbs.chatgame.games.word.UnscrambleGame;
import wbs.chatgame.games.word.Word;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsMath;

import java.util.Collection;

public class UnscrambleOnlinePlayer extends UnscrambleGame implements Challenge<UnscrambleGame> {
    public UnscrambleOnlinePlayer(UnscrambleGame parent) {
        super(parent.getGameName(), 0, parent.getDuration());
    }

    @Override
    protected Word getWord() {
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();

        if (online.isEmpty()) {
            plugin.logger.info("No online players found - skipping online player challenge!");
            return super.getWord();
        }

        Player randomPlayer = WbsCollectionUtil.getRandom(online);

        return new Word(randomPlayer.getName(), 2, null);
    }

    @Override
    public void broadcastQuestion(String currentQuestion) {
        currentQuestion += "&r This unscramble is an online player's name!";
        super.broadcastQuestion(currentQuestion);
    }

    @Override
    public Class<UnscrambleGame> getGameClass() {
        return UnscrambleGame.class;
    }
}
