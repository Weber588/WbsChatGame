package wbs.chatgame.data;

import wbs.chatgame.WbsChatGame;
import wbs.utils.util.database.WbsDatabase;
import wbs.utils.util.database.WbsField;
import wbs.utils.util.database.WbsFieldType;
import wbs.utils.util.database.WbsTable;

import java.util.HashMap;
import java.util.Map;

public final class ChatGameDB {

    private static PlayerManager playerManager;
    public static PlayerManager getPlayerManager() {
        if (playerManager == null) {
            playerManager = new PlayerManager(WbsChatGame.getInstance(), playerTable);
        }

        return playerManager;
    }

    private ChatGameDB() {}

    private static WbsChatGame plugin;

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

    public static void setupDatabase() {
        plugin = WbsChatGame.getInstance();
        database = new WbsDatabase(plugin, "data");

        // Player table
        playerTable = new WbsTable(database, "players", uuidField);
        playerTable.addField(
                nameField,
                listeningField
        );

        database.addTable(playerTable);

        // Stats table
        statsTable = new WbsTable(database, "stats", uuidField, gameField);

        if (!database.createDatabase()) {
            return;
        }

        if (database.createTables()) {
            addNewFields();
        }
    }


    /**
     * Add new fields added after the initial run.
     */
    private static void addNewFields() {
        for (GameStats.TrackedPeriod period : GameStats.TrackedPeriod.values()) {
            statsTable.addFieldIfNotExists(period.field);
        }
    }

}
