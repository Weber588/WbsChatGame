package wbs.chatgame.games.challenges;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import wbs.chatgame.GameController;
import wbs.chatgame.WordUtil;
import wbs.chatgame.games.Game;
import wbs.utils.util.WbsCollectionUtil;

import java.util.Collection;

public class UnscrambleOnlinePlayer extends Challenge {

    public UnscrambleOnlinePlayer(Game game) {
        super(game);
    }

    @Override
    public boolean startChallenge() {
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();

        if (online.isEmpty()) return false;

        Player randomPlayer = WbsCollectionUtil.getRandom(online);
        String scrambled = WordUtil.scrambleString(randomPlayer.getName());

        GameController.broadcast("Unscramble \"&h" + scrambled + "&r\" for "
                + GameController.pointsDisplay(2) + "! " +
                "This scramble is an online players name!");

        return true;
    }
}
