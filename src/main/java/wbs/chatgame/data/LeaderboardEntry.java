package wbs.chatgame.data;

import wbs.utils.util.database.WbsRecord;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a player and their points in a given category. Used by the StatsManager to avoid
 * retrieving/creating entire player & stats objects just to query one leaderboard.
 */
public final class LeaderboardEntry {

    private final UUID uuid;
    private final String name;
    private final int points;
    private final GameStats.TrackedPeriod period;
    private final long createdTime;

    /**
     *
     */
    public LeaderboardEntry(UUID uuid, String name, int points, GameStats.TrackedPeriod period, long createdTime) {
        this.uuid = uuid;
        this.name = name;
        this.points = points;
        this.period = period;
        this.createdTime = createdTime;
    }

    public LeaderboardEntry(WbsRecord record, GameStats.TrackedPeriod period) {
        uuid = UUID.fromString(record.getValue(ChatGameDB.uuidField, String.class));
        name = record.getValue(ChatGameDB.nameField, String.class);
        points = (Integer) record.getAnonymousField(StatsManager.TOTAL_POINTS_NAME);
        this.period = period;
        createdTime = Instant.now().getEpochSecond();
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public int points() {
        return points;
    }

    public GameStats.TrackedPeriod period() {
        return period;
    }

    public long createdTime() {
        return createdTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LeaderboardEntry) obj;
        return Objects.equals(this.uuid, that.uuid) &&
                Objects.equals(this.name, that.name) &&
                this.points == that.points &&
                Objects.equals(this.period, that.period) &&
                this.createdTime == that.createdTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, points, period, createdTime);
    }

    @Override
    public String toString() {
        return "LeaderboardEntry[" +
                "uuid=" + uuid + ", " +
                "name=" + name + ", " +
                "points=" + points + ", " +
                "period=" + period + ", " +
                "createdTime=" + createdTime + ']';
    }


}
