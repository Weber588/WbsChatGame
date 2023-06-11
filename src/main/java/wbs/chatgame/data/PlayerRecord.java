package wbs.chatgame.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.Game;
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
        if (player == null) {
            tryGetPlayer();
        }
        return player;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUUID() {
        return uuid;
    }

    @NotNull
    public GameStats getStats(@NotNull Game game) {
        String name = game.getGameName();

        GameStats toReturn = stats.get(name);
        if (toReturn == null) {
            toReturn = new GameStats(this, name);
            stats.put(name, toReturn);
        }

        return toReturn;
    }

    public void addPoints(int points, Game game) {
        getStats(game).addPoints(points);
        StatsManager.updateCaches(this, game);
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

    public int getPoints(@NotNull Game game) {
        return getPoints(game, TrackedPeriod.TOTAL);
    }

    public int getPoints(@NotNull Game game, TrackedPeriod period) {
        return getStats(game).getPoints(period);
    }

    public void registerTime(double speed, Game game) {
        getStats(game).registerTime(speed);
        StatsManager.updateCaches(this, game);
    }

    public double getFastestTime() {
        return getFastestTime(TrackedPeriod.TOTAL);
    }

    public double getFastestTime(TrackedPeriod period) {
        double fastest = Double.MAX_VALUE;
        for (GameStats stat : stats.values()) {
            fastest = Math.min(stat.getFastestTime(period), fastest);
        }
        return fastest;
    }

    public double getFastestTime(@NotNull Game game, TrackedPeriod period) {
        return getStats(game).getFastestTime(period);
    }

    public double getFastestTime(@NotNull Game game) {
        return getStats(game).getFastestTime(TrackedPeriod.TOTAL);
    }

    public void addStat(GameStats stats) {
        this.stats.put(stats.getGameName(), stats);
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public Collection<WbsRecord> getStatsRecords() {
        return this.stats.values().stream()
                .filter(GameStats::needsSaving)
                .map(RecordProducer::toRecord)
                .collect(Collectors.toList());
    }

    public Collection<GameStats> getAllStats() {
        return stats.values();
    }

    public void invalidatePlayer() {
        player = null;
    }
}
