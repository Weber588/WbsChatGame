package wbs.chatgame.data;

import wbs.utils.util.database.RecordProducer;
import wbs.utils.util.database.WbsRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameStats implements RecordProducer {

    private final UUID uuid;
    private final String gameName;

    private final Map<TrackedPeriod, Integer> points = new HashMap<>();
    private final Map<TrackedPeriod, Double> speed = new HashMap<>();

    public GameStats(PlayerRecord player, String gameName) {
        this.uuid = player.getUUID();
        this.gameName = gameName;
    }

    public GameStats(WbsRecord record) {
        uuid = UUID.fromString(record.getValue(ChatGameDB.uuidField, String.class));
        gameName = record.getValue(ChatGameDB.gameField, String.class);

        for (TrackedPeriod period : TrackedPeriod.values()) {
            Integer current = record.getValue(period.pointsField, Integer.class);
            if (current != null) {
                points.put(period, current);
            }

            Double currentSpeed = record.getValue(period.speedField, Double.class);
            if (currentSpeed != null) {
                speed.put(period, currentSpeed);
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

    public void registerTime(double speed) {
        for (TrackedPeriod period : TrackedPeriod.values()) {
            double current = this.speed.getOrDefault(period, Double.MAX_VALUE);
            this.speed.put(period, Math.min(speed, current));
        }
    }

    public void registerTime(TrackedPeriod period, double speed) {
        this.speed.put(period, speed);
    }

    public double getFastestTime(TrackedPeriod period) {
        Double pointsVal = speed.get(period);
        return pointsVal == null ? Double.MAX_VALUE : pointsVal;
    }

    public String getGameName() {
        return gameName;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public WbsRecord toRecord() {
        WbsRecord record = new WbsRecord(ChatGameDB.getDatabase());

        record.setField(ChatGameDB.uuidField, uuid);
        record.setField(ChatGameDB.gameField, gameName);

        for (TrackedPeriod period : TrackedPeriod.values()) {
            record.setField(period.pointsField, getPoints(period));
            record.setField(period.speedField, getFastestTime(period));
        }

        return record;
    }

    public boolean needsSaving() {
        for (Map.Entry<TrackedPeriod, Integer> entry : points.entrySet()) {
            Integer pointsInPeriod = entry.getValue();

            if (pointsInPeriod != null && pointsInPeriod != 0) {
                return true;
            }
        }

        for (Map.Entry<TrackedPeriod, Double> entry : speed.entrySet()) {
            Double speedInPeriod = entry.getValue();

            if (speedInPeriod != null && speedInPeriod != Double.MAX_VALUE) {
                return true;
            }
        }

        return false;
    }
}
