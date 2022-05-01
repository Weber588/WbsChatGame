package wbs.chatgame.data;

import wbs.utils.util.database.RecordProducer;
import wbs.utils.util.database.WbsRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameStats implements RecordProducer {

    private final UUID uuid;
    private final String gameId;

    private final Map<TrackedPeriod, Integer> points = new HashMap<>();

    public GameStats(PlayerRecord player, String gameId) {
        this.uuid = player.getUUID();
        this.gameId = gameId;
    }

    public GameStats(WbsRecord record) {
        uuid = UUID.fromString(record.getValue(ChatGameDB.uuidField, String.class));
        gameId = record.getValue(ChatGameDB.gameField, String.class);

        for (TrackedPeriod period : TrackedPeriod.values()) {
            Integer current = record.getValue(period.field, Integer.class);
            if (current != null) {
                points.put(period, current);
            }
        }
    }

    public void addPoints(int add) {
        for (TrackedPeriod period : TrackedPeriod.values()) {
            int current = points.getOrDefault(period, 0);
            points.put(period, current + add);
        }
    }

    public void setPoints(TrackedPeriod period, int points) {
        this.points.put(period, points);
    }

    public int getPoints() {
        return getPoints(TrackedPeriod.TOTAL);
    }

    public int getPoints(TrackedPeriod period) {
        Integer pointsVal = points.get(period);
        return pointsVal == null ? 0 : pointsVal;
    }

    public String getGameId() {
        return gameId;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public WbsRecord toRecord() {
        WbsRecord record = new WbsRecord(ChatGameDB.getDatabase());

        record.setField(ChatGameDB.uuidField, uuid);
        record.setField(ChatGameDB.gameField, gameId);

        for (TrackedPeriod period : TrackedPeriod.values()) {
            Integer pointsVal = points.get(period);
            record.setField(period.field, pointsVal == null ? 0 : pointsVal);
        }

        return record;
    }
}
