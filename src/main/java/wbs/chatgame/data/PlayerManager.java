package wbs.chatgame.data;

import org.jetbrains.annotations.NotNull;
import wbs.utils.util.database.AbstractDataManager;
import wbs.utils.util.database.WbsRecord;
import wbs.utils.util.database.WbsTable;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PlayerManager extends AbstractDataManager<PlayerRecord, UUID> {

    public PlayerManager(WbsPlugin plugin, WbsTable table) {
        super(plugin, table);
    }

    /**
     * Synchronously get a record based on it's key. It's recommended
     * to use {@link AbstractDataManager#getAsync(Object, Consumer)} to avoid freezing the server,
     * or use this in an asynchronous thread.
     * @param key The key to retrieve by.
     * @return The value, or a new value based on the key.
     */
    @Override
    @NotNull
    public PlayerRecord get(UUID key) {
        if (getCache().containsKey(key)) return getCache().get(key);

        WbsRecord record = select(Collections.singletonList(key));

        PlayerRecord found;
        if (record != null) {
            found = fromRecord(record);
        } else {
            found = produceDefault(key);
        }

        List<WbsRecord> statRecords = ChatGameDB.statsTable.selectOnField(ChatGameDB.uuidField, found.getUUID());

        for (WbsRecord statRecord : statRecords) {
            found.addStat(new GameStats(statRecord));
        }

        getCache().put(key, found);
        return found;
    }

    @Override
    public void save(Collection<PlayerRecord> toInsert) {
        if (toInsert.isEmpty()) return;
        super.save(toInsert);

        List<WbsRecord> statsRecords = new LinkedList<>();

        for (PlayerRecord record : toInsert) {
            statsRecords.addAll(record.getStatsRecords());
        }

        ChatGameDB.statsTable.upsert(statsRecords);
    }

    @NotNull
    public List<UUID> getUUIDs(String username) {
        List<WbsRecord> records = ChatGameDB.playerTable.selectOnField(ChatGameDB.nameField, username);

        return records.stream().map(record -> record.getValue(ChatGameDB.uuidField, UUID.class)).collect(Collectors.toList());
    }

    public void getUUIDsAsync(String username, Consumer<List<UUID>> callback) {
        plugin.getAsync(() -> getUUIDs(username), callback);
    }

    @Override
    protected @NotNull PlayerRecord fromRecord(@NotNull WbsRecord wbsRecord) {
        return new PlayerRecord(wbsRecord);
    }

    @Override
    protected @NotNull PlayerRecord produceDefault(UUID uuid) {
        return new PlayerRecord(uuid);
    }
}
