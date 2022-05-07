package wbs.chatgame.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    private final Map<UUID, PlayerRecord> onlinePlayers = new HashMap<>();

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

        addToCache(key, found);
        return found;
    }

    @Override
    public void save(Collection<PlayerRecord> toInsert) {
        saveWithResult(toInsert);
    }

    public boolean saveWithResult(Collection<PlayerRecord> toInsert) {
        if (toInsert.isEmpty()) return true;
        super.save(toInsert);

        List<WbsRecord> statsRecords = new LinkedList<>();

        for (PlayerRecord record : toInsert) {
            statsRecords.addAll(record.getStatsRecords());
        }

        return ChatGameDB.statsTable.upsert(statsRecords);
    }

    @NotNull
    public List<UUID> getUUIDs(String username) {
        List<WbsRecord> records = ChatGameDB.playerTable.selectOnField(ChatGameDB.nameField, username);

        return records.stream()
                .map(record ->
                        record.getValue(ChatGameDB.uuidField, String.class))
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }

    public void getUUIDsAsync(String username, Consumer<List<UUID>> callback) {
        plugin.getAsync(() -> getUUIDs(username), callback);
    }

    /**
     * Load an online player's record into persistent memory rather than the cache, to
     * guarantee the records of online players are always synchronously available.
     * @param player The player to load
     */
    public void loadOnlinePlayer(Player player, @Nullable Consumer<PlayerRecord> consumer) {
        getAsync(player.getUniqueId(), record -> {
            // Check again if the player is online, in case they join and leave faster than the database can respond
            if (player.isOnline()) {
                onlinePlayers.put(player.getUniqueId(), record);
                if (consumer != null) {
                    consumer.accept(record);
                }
            }
        });
    }

    /**
     * Load an online player's record into persistent memory rather than the cache, to
     * guarantee the records of online players are always synchronously available.
     * @param player The player to load
     */
    public void loadOnlinePlayer(Player player) {
        loadOnlinePlayer(player, null);
    }

    /**
     * Remove a player from the online players map.
     * @param uuid The UUID of the player to unload
     */
    public void unloadPlayer(UUID uuid) {
        onlinePlayers.remove(uuid);
    }

    /**
     * Get the record for the given player, guaranteed to be available
     * while the player is online.
     * @param player The player to retrieve the record for.
     * @return The record associated with the given player.
     */
    public PlayerRecord getOnlinePlayer(Player player) {
        return getOnlinePlayer(player.getUniqueId());
    }

    /**
     * Get the record for the given player, guaranteed to be available
     * while the player is online.
     * @param uuid The uuid of the player to retrieve the record for.
     * @return The record associated with the given uuid.
     */
    public PlayerRecord getOnlinePlayer(UUID uuid) {
        return onlinePlayers.get(uuid);
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
