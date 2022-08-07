package wbs.chatgame.games.challenges;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.games.word.UnscrambleGame;
import wbs.chatgame.games.word.Word;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.plugin.WbsMessage;

import java.util.Collection;

public class UnscrambleOnlinePlayer extends UnscrambleGame implements Challenge<UnscrambleGame> {
    public UnscrambleOnlinePlayer(UnscrambleGame parent) {
        super(parent);
    }

    @Override
    protected Word getWord() {
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();

        if (online.isEmpty()) {
            plugin.logger.info("No online players found - skipping online player challenge!");
            return super.getWord();
        }

        Player randomPlayer = WbsCollectionUtil.getRandom(online);

        return new Word(randomPlayer.getName(), 2, true);
    }

    @Override
    public void broadcastQuestion(String currentQuestion) {
        currentQuestion += "&r This unscramble is an online player's name!";
        super.broadcastQuestion(currentQuestion);
    }

    @Override
    protected void broadcastScramble(String scrambledWord) {
        WbsMessage message = plugin.buildMessage("Unscramble \"")
                .appendRaw(scrambledWord)
                    .setFormatting("&h")
                .append("\" for "
                        + GameController.pointsDisplay(getPoints()) + "! This unscramble is an online player's name!")
                .build();

        broadcastQuestion(message);
    }

    @Override
    public Class<UnscrambleGame> getGameClass() {
        return UnscrambleGame.class;
    }

    private String challengeId;

    @Override
    public final void setId(@NotNull String id) {
        challengeId = id;
    }

    @Override
    public final @NotNull String getId() {
        return challengeId;
    }

    @Override
    public boolean valid() {
        return Bukkit.getOnlinePlayers().size() > 0;
    }
}
