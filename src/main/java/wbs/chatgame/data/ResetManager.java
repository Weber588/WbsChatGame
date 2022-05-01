package wbs.chatgame.data;

import org.bukkit.scheduler.BukkitRunnable;
import wbs.chatgame.WbsChatGame;
import wbs.utils.util.database.WbsRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class ResetManager {
    private ResetManager() {}

    private static final String DATE_KEY_PREFIX = "last_reset_";

    private static String getDateKey(TrackedPeriod period) {
        return DATE_KEY_PREFIX + period.name();
    }

    public static void scheduleResets() {
        WbsChatGame.getInstance().runAsync(() -> {
            for (TrackedPeriod period : TrackedPeriod.values()) {
                // Not strictly needed, but doesn't hurt
                if (period == TrackedPeriod.TOTAL) continue;

                String dateKey = getDateKey(period);

                List<WbsRecord> found = ChatGameDB.datesTable.selectOnField(ChatGameDB.dateKeyField, dateKey);
                LocalDateTime lastReset = null;
                if (!found.isEmpty()) {
                    WbsRecord resetRecord = found.get(0);

                    long epochSecond = resetRecord.getValue(ChatGameDB.dateField, Long.class);

                    lastReset = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond, 0), ZoneId.systemDefault());
                }

                if (lastReset == null) {
                    lastReset = ChatGameDB.getFirstLoadDate();
                    assert lastReset != null;
                }

                if (!period.inCurrentPeriod(lastReset)) {
                    WbsChatGame.getInstance().logger.info("Last reset for " + period + " in previous period; resetting now.");

                    reset(period);
                }

                LocalDateTime nextPeriodBegins = period.getNextPeriodStart();

                if (nextPeriodBegins != null) {
                    scheduleReset(period, nextPeriodBegins);
                }
            }
        });
    }

    private static void scheduleReset(TrackedPeriod period, LocalDateTime resetTime) {
        // To avoid depending on the TPS (which may be slow), check intermittently if
        // the reset time is within 1 minute or in the past.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (resetTime.isBefore(LocalDateTime.now())) {
                    WbsChatGame.getInstance().runAsync(() -> ResetManager.reset(period));
                    cancel();
                }
            }
        }.runTaskTimer(WbsChatGame.getInstance(), 1200, 1200);

        WbsChatGame.getInstance().logger.info("Next reset for " + period + " is scheduled to run at: " +
                resetTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    public static void reset(TrackedPeriod period) {
        String query = "UPDATE " + ChatGameDB.statsTable.getName() + " " +
                "SET " + period.field.getFieldName() + " = 0";

        boolean updated = ChatGameDB.getDatabase().queryWithoutReturns(query);
        if (updated) {
            ChatGameDB.getPlayerManager().getCache().forEach((uuid, playerRecord) -> {
                for (GameStats stats : playerRecord.getAllStats()) {
                    stats.setPoints(period, 0);
                }
            });

            StatsManager.recalculate(period);

            WbsRecord lastResetTime = new WbsRecord(ChatGameDB.getDatabase());

            lastResetTime.setField(ChatGameDB.dateKeyField, getDateKey(period));
            lastResetTime.setField(ChatGameDB.dateField, LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond());

            // Upsert since we don't know if this is the first reset or not
            lastResetTime.upsert(ChatGameDB.datesTable);

            WbsChatGame.getInstance().logger.info("Stats have been reset in period " + period + "!");
            LocalDateTime nextReset = period.getNextPeriodStart();
            if (nextReset != null) {
                scheduleReset(period, nextReset);
            }
        } else {
            WbsChatGame.getInstance().logger.severe("Stats failed to reset in period " + period + "!");
        }
    }
}
