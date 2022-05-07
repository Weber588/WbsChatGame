package wbs.chatgame.data;

import wbs.chatgame.WbsChatGame;
import wbs.utils.util.database.*;

import java.time.*;
import java.util.List;

public final class ChatGameDB {

    private static PlayerManager playerManager;
    public static PlayerManager getPlayerManager() {
        if (playerManager == null) {
            playerManager = new PlayerManager(WbsChatGame.getInstance(), playerTable);
        }

        return playerManager;
    }

    private ChatGameDB() {}

    private static WbsDatabase database;
    public static WbsDatabase getDatabase() {
        return database;
    }

    public static WbsTable playerTable;

    public static final WbsField uuidField = new WbsField("uuid", WbsFieldType.STRING);
    public static final WbsField nameField = new WbsField("name", WbsFieldType.STRING);
    public static final WbsField listeningField = new WbsField("listening", WbsFieldType.BOOLEAN);

    public static WbsTable statsTable;

    public static final WbsField gameField = new WbsField("game", WbsFieldType.STRING);

    // Table to track the last time a specific event occurred, such as a weekly/monthly reset
    public static WbsTable datesTable;

    public static final WbsField dateKeyField = new WbsField("name", WbsFieldType.STRING);
    // Stores as epoch second
    public static final WbsField dateField = new WbsField("epoch_second", WbsFieldType.LONG);

    public static void setupDatabase() {
        WbsChatGame plugin = WbsChatGame.getInstance();
        database = new WbsDatabase(plugin, "data");

        // Player table
        playerTable = new WbsTable(database, "players", uuidField);
        playerTable.addField(
                nameField,
                listeningField
        );

        // Stats table
        statsTable = new WbsTable(database, "stats", uuidField, gameField);

        // Dates table
        datesTable = new WbsTable(database, "dates", dateKeyField);
        datesTable.addField(dateField);

        if (!database.createDatabase()) {
            return;
        }

        if (database.createTables()) {
            addNewFields();

            if (getFirstLoadDate() == null) {
                WbsRecord firstLoadRecord = new WbsRecord(database);

                firstLoadRecord.setField(dateKeyField, FIRST_LOAD_KEY);
                firstLoadRecord.setField(dateField, LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond());

                firstLoadRecord.insert(datesTable);
                plugin.logger.info("Initialized database for the first time!");
            }
        }
    }

    /**
     * Add new fields added after the initial run.
     */
    private static void addNewFields() {
        for (TrackedPeriod period : TrackedPeriod.values()) {
            statsTable.addFieldIfNotExists(period.pointsField);
            statsTable.addFieldIfNotExists(period.speedField);
        }
    }

    public static final String FIRST_LOAD_KEY = "first_load";

    public static LocalDateTime getFirstLoadDate() {
        List<WbsRecord> found = datesTable.selectOnField(dateKeyField, FIRST_LOAD_KEY);
        if (!found.isEmpty()) {
            WbsRecord firstLoadRecord = found.get(0);
            long epochSecond = firstLoadRecord.getValue(dateField, Long.class);
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond, 0), ZoneId.systemDefault());
        }

        return null;
    }
}
