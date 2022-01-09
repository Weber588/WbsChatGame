package wbs.chatgame.data;

import wbs.utils.util.database.RecordProducer;
import wbs.utils.util.database.WbsField;
import wbs.utils.util.database.WbsFieldType;
import wbs.utils.util.database.WbsRecord;

import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameStats implements RecordProducer {

    public enum TrackedPeriod {
        TOTAL,
        MONTHLY,
        WEEKLY;

        public final WbsField field;

        TrackedPeriod() {
            field = new WbsField(toString().toLowerCase(), WbsFieldType.INT, 0);
        }

        public boolean inSamePeriod(LocalDateTime date1, LocalDateTime date2) {
            return switch (this) {
                case TOTAL -> true;
                case MONTHLY -> date1.getMonth().equals(date2.getMonth()) && date1.getYear() == date2.getYear();
                case WEEKLY -> date1.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == date2.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            };
        }

        public boolean inCurrentPeriod(LocalDateTime compare) {
            return inSamePeriod(compare, LocalDateTime.now());
        }
    }

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
        StatsManager.pointsUpdated = true;
        for (TrackedPeriod period : TrackedPeriod.values()) {
            int current = points.getOrDefault(period, 0);
            points.put(period, current + add);
        }
    }

    public int getPoints() {
        return getPoints(GameStats.TrackedPeriod.TOTAL);
    }

    public int getPoints(TrackedPeriod period) {
        Integer pointsVal = points.get(period);
        return pointsVal == null ? 0 : pointsVal;
    }

    public String getGameId() {
        return gameId;
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
