package wbs.chatgame.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.utils.util.database.RecordProducer;
import wbs.utils.util.database.WbsRecord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerRecord implements RecordProducer {

    private Player player;
    private final UUID uuid;
    private String name;

    private boolean listening;

    private final Map<String, GameStats> stats = new HashMap<>();

    public PlayerRecord(WbsRecord record) {
        uuid = UUID.fromString(record.getValue(ChatGameDB.uuidField, String.class));

        tryGetPlayer();

        if (name == null) {
            name = record.getValue(ChatGameDB.nameField, String.class);
        }

        Boolean checkListening = record.getValue(ChatGameDB.listeningField, Boolean.class);
        listening = checkListening == null ? WbsChatGame.getInstance().settings.listenByDefault : checkListening;
    }

    public PlayerRecord(UUID uuid) {
        this.uuid = uuid;
        listening = WbsChatGame.getInstance().settings.listenByDefault;

        tryGetPlayer();
    }

    @SuppressWarnings("UnusedReturnValue")
    public Player tryGetPlayer() {
        player = Bukkit.getPlayer(uuid);
        if (player != null) {
            name = player.getName();
        }

        return player;
    }

    @Override
    public WbsRecord toRecord() {
        WbsRecord record = new WbsRecord(ChatGameDB.getDatabase());

        record.setField(ChatGameDB.uuidField, uuid.toString());
        record.setField(ChatGameDB.nameField, name);
        record.setField(ChatGameDB.listeningField, listening);

        return record;
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }
    public UUID getUUID() {
        return uuid;
    }

    @NotNull
    public GameStats getStats(Game game) {
        String id = GameManager.getRegistrationId(game);

        if (id == null) throw new IllegalArgumentException("Game not registered: " + game.getClass().getCanonicalName());

        GameStats toReturn = stats.get(id);
        if (toReturn == null) {
            toReturn = new GameStats(this, id);
            stats.put(id, toReturn);
        }

        return toReturn;
    }

    public void addPoints(int points, Game game) {
        getStats(game).addPoints(points);
        StatsManager.updateCaches(this, game);
        StatsManager.updateTotalCache(this);
    }

    public int getPoints() {
        return getPoints(TrackedPeriod.TOTAL);
    }

    public int getPoints(TrackedPeriod period) {
        int total = 0;
        for (GameStats stat : stats.values()) {
            total += stat.getPoints(period);
        }
        return total;
    }

    public int getPoints(Game game) {
        return getPoints(game, TrackedPeriod.TOTAL);
    }

    public int getPoints(Game game, TrackedPeriod period) {
        return getStats(game).getPoints(period);
    }

    public void addStat(GameStats stats) {
        this.stats.put(stats.getGameId(), stats);
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public Collection<WbsRecord> getStatsRecords() {
        return this.stats.values().stream().map(RecordProducer::toRecord).collect(Collectors.toList());
    }
}
