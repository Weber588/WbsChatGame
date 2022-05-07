package wbs.chatgame.data;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.GameManager;
import wbs.chatgame.games.math.MathGame;
import wbs.chatgame.games.trivia.TriviaGame;
import wbs.chatgame.games.word.QuickTypeGame;
import wbs.chatgame.games.word.RevealGame;
import wbs.chatgame.games.word.UnscrambleGame;

import java.io.*;
import java.util.*;

public final class LegacyDataAdapter {

    private static final Map<String, PlayerRecord> legacyRecords = new HashMap<>();

    private enum LegacyGameType {
        // Order sensitive
        UNSCRAMBLE, MATH, TRIVIA, QUICKTYPE, REVEAL;

        private String gameId;

        static {
            UNSCRAMBLE.gameId = GameManager.getRegistrationId(UnscrambleGame.class);
            MATH.gameId = GameManager.getRegistrationId(MathGame.class);
            TRIVIA.gameId = GameManager.getRegistrationId(TriviaGame.class);
            QUICKTYPE.gameId = GameManager.getRegistrationId(QuickTypeGame.class);
            REVEAL.gameId = GameManager.getRegistrationId(RevealGame.class);
        }

        public String getGameId() {
            return gameId;
        }
    }

    public static void loadLegacyData() {
        try {
            File data = new File(WbsChatGame.getInstance().getDataFolder(), "legacy.data");
            if (!data.exists()) {
                WbsChatGame.getInstance().logger.info("Legacy data file not found. To load legacy data, place the file " +
                        "in the plugin folder named \"legacy.data\".");
                return;
            }
            FileReader reader = new FileReader(data);
            BufferedReader buffered = new BufferedReader(reader);

            String line = buffered.readLine();
            while (line != null) {
                PlayerRecord record = fromDataString(line);

                if (record != null) {
                    if (!isDefault(record)) {
                        legacyRecords.put(record.getName(), record);
                    }
                }

                line = buffered.readLine();
            }

            buffered.close();
        } catch (FileNotFoundException e) {
            WbsChatGame.getInstance().logger.info("Legacy player data file not found.");
            return;
        } catch (IOException e) {
            WbsChatGame.getInstance().logger.info("An unknown error occurred while trying to read the legacy player data file.");
            e.printStackTrace();
            return;
        }

        if (!legacyRecords.isEmpty()) {
            WbsChatGame.getInstance().logger.info(legacyRecords.size() + " legacy player records found. " +
                    "Beginning database submission.");

            startSaveBatch(legacyRecords.values());
        } else {
            WbsChatGame.getInstance().logger.info("No legacy records found.");
        }
    }

    private static boolean isDefault(PlayerRecord record) {
        boolean isDefault = true;

        for (GameStats stats : record.getAllStats()) {
            if (stats.getPoints() != 0) {
                isDefault = false;
                break;
            }
        }

        return isDefault;
    }

    private static void startSaveBatch(Collection<PlayerRecord> toSave) {
        scheduleNextBatch(toSave, toSave.iterator(), 0);
    }

    private static final int MAX_ATTEMPTS = 3;

    private static void scheduleNextBatch(Collection<PlayerRecord> toSave, Iterator<PlayerRecord> iterator, int index) {
        int batchSize = 50;

        if (!iterator.hasNext() || index >= toSave.size()) {
            savingComplete(index);
            return;
        }

        List<PlayerRecord> batch = new LinkedList<>();
        for (int i = 0; i < batchSize && iterator.hasNext(); i++) {
            batch.add(iterator.next());
            index++;
        }

        WbsChatGame plugin = WbsChatGame.getInstance();
        int finalIndex = index;
        plugin.runAsync(() -> runNextBatch(toSave, iterator, batch, finalIndex, 0));
    }

    private static void savingComplete(int saved) {
        WbsChatGame plugin = WbsChatGame.getInstance();
        plugin.logger.info("Done. " + saved + " player records saved.");

        StatsManager.recalculateAll();

        File newFileName = new File(plugin.getDataFolder(), "legacy_imported.data");
        File data = new File(plugin.getDataFolder(), "legacy.data");
        if (!data.renameTo(newFileName)) {
            plugin.logger.info("Failed to rename legacy data file. Please disable loading from legacy data, " +
                    "or delete/rename the file.");
        }
    }

    private static void runNextBatch(Collection<PlayerRecord> toSave, Iterator<PlayerRecord> iterator, List<PlayerRecord> batch, int index, int attempt) {
        WbsChatGame plugin = WbsChatGame.getInstance();
        if (attempt >= MAX_ATTEMPTS) {
            plugin.logger.info("Failed to save " + batch.size() + " legacy records after " + MAX_ATTEMPTS + " attempts. Skipping...");
            scheduleNextBatch(toSave, iterator, index);
            return;
        }

        boolean succeeded = ChatGameDB.getPlayerManager().saveWithResult(batch);

        if (succeeded) {
            plugin.logger.info("Saved " + batch.size() + " legacy records.");

            scheduleNextBatch(toSave, iterator, index);
        } else {
            plugin.logger.info("Failed to save " + batch.size() + " legacy records. Will try " + (MAX_ATTEMPTS - attempt) + " more times.");

            plugin.runAsync(() -> runNextBatch(toSave, iterator, batch, index, attempt + 1));
        }
    }

    @Nullable
    private static PlayerRecord fromDataString(String dataString) {
        try {
            String[] nodes = dataString.split("::");

            String username = nodes[0];
            String[] totalStrings = nodes[1].split(":");
            String[] weekStrings = nodes[2].split(":");
            String[] monthStrings = nodes[3].split(":");
            String[] correctStrings = nodes[4].split(":");
            @SuppressWarnings("unused")
            String[] incorrectStrings = nodes[5].split(":");
            String[] speedStrings = nodes[6].split(":");
            boolean ignore = Boolean.parseBoolean(nodes[7]);

            @SuppressWarnings("deprecation")
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);

            PlayerRecord record = new PlayerRecord(offlinePlayer.getUniqueId());

            record.setName(username);

            int i = 0;
            for (LegacyGameType type : LegacyGameType.values()) {
                GameStats stats = new GameStats(record, type.getGameId());

                stats.setPoints(TrackedPeriod.TOTAL, Integer.parseInt(totalStrings[i]));
                stats.setPoints(TrackedPeriod.WEEKLY, Integer.parseInt(weekStrings[i]));
                stats.setPoints(TrackedPeriod.MONTHLY, Integer.parseInt(monthStrings[i]));

                int correctGuesses = Integer.parseInt(correctStrings[i]);

                double speed = Double.parseDouble(speedStrings[i]);
                stats.registerTime(TrackedPeriod.TOTAL, speed / correctGuesses);

                record.addStat(stats);
                i++;
            }

            if (ignore) {
                record.setListening(false);
            }

            return record;
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            WbsChatGame.getInstance().logger.info("Invalid legacy dataString: " + dataString);
            return null;
        }
    }
}
