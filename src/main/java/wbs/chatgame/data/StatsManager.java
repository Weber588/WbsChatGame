package wbs.chatgame.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.utils.util.database.WbsDatabase;
import wbs.utils.util.database.WbsRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public final class StatsManager {
    private StatsManager() {}

    public static final String TOTAL_POINTS_NAME = "total_points";

    public static final int topListSize = 25;

    private static final Map<TrackedPeriod, Leaderboard> periodTops = new HashMap<>();
    private static final Map<Game, Map<TrackedPeriod, Leaderboard>> gameTops = new HashMap<>();

    public static boolean forceUpdate = false;

    /**
     * Update all leaderboard caches that contain an entry for the given player, updating leaderboard order accordingly.
     */
    public static void updateCaches(PlayerRecord record, @Nullable Game game) {
        for (TrackedPeriod period : TrackedPeriod.values()) {
            Leaderboard top = periodTops.get(period);
            if (top == null) {
                top = new Leaderboard(period);
            }

            top.update(record);
            periodTops.put(period, top);
        }

        Map<TrackedPeriod, Leaderboard> gamePeriodTops = gameTops.get(game);
        if (gamePeriodTops != null) {
            for (TrackedPeriod period : TrackedPeriod.values()) {
                Leaderboard top = gamePeriodTops.get(period);
                if (top == null) {
                    top = new Leaderboard(period, game);
                }

                top.update(record);
            }
        }
    }

    @NotNull
    public static Leaderboard getCachedTop(TrackedPeriod period) {
        Leaderboard top = periodTops.get(period);
        if (top == null) {
            top = new Leaderboard(period);
        }
        return top;
    }

    @NotNull
    public static Leaderboard getCachedTop(Game game) {
        return getCachedTop(TrackedPeriod.TOTAL, game);
    }

    @NotNull
    public static Leaderboard getCachedTop(TrackedPeriod period, Game game) {
        if (game == null) {
            return getCachedTop(period);
        }
        Map<TrackedPeriod, Leaderboard> gamePeriods = gameTops.get(game);
        if (gamePeriods == null) {
            gamePeriods = new HashMap<>();
        }

        Leaderboard top = gamePeriods.get(period);
        if (top == null) {
            top = new Leaderboard(period, game);
        }
        return top;
    }

    public static void recalculateAll() {
        for (TrackedPeriod stat : TrackedPeriod.values()) {
            recalculate(stat);
            for (Game game : GameManager.getGames()) {
                recalculate(stat, game);
            }
        }
    }

    public static Leaderboard recalculate(TrackedPeriod period) {
        Leaderboard leaderboard = new Leaderboard(period);

        String query =
                "SELECT " +
                        "p." + ChatGameDB.uuidField.getFieldName() + ", " +
                        "p." + ChatGameDB.nameField.getFieldName() + ", " +
                        "SUM(s." + period.field.getFieldName() + ") AS " + TOTAL_POINTS_NAME + " " +
                "FROM " + ChatGameDB.statsTable.getName() + " AS s " +
                "JOIN " + ChatGameDB.playerTable.getName() + " AS p " +
                    "ON s." + ChatGameDB.uuidField.getFieldName() + " = p." + ChatGameDB.uuidField.getFieldName() + " " +
                    "AND s." + period.field.getFieldName() + " != 0 " +
                "GROUP BY s." + ChatGameDB.uuidField.getFieldName() + " " +
                "ORDER BY " + TOTAL_POINTS_NAME + " DESC " +
                "LIMIT " + topListSize
                ;

        WbsDatabase db = ChatGameDB.getDatabase();
        List<WbsRecord> selected;
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            selected = db.select(statement);
        } catch (SQLException e) {
            e.printStackTrace();
            return leaderboard;
        }

        for (WbsRecord record : selected) {
            LeaderboardEntry entry = new LeaderboardEntry(record, period, null);
            leaderboard.add(entry);
        }

        leaderboard.sort();

        periodTops.put(period, leaderboard);
        return leaderboard;
    }


    public static Leaderboard recalculate(@NotNull TrackedPeriod period, @NotNull Game game) {
        Leaderboard leaderboard = new Leaderboard(period, game);

        String query =
                "SELECT " +
                        "p." + ChatGameDB.uuidField.getFieldName() + ", " +
                        "p." + ChatGameDB.nameField.getFieldName() + ", " +
                        "s." + period.field.getFieldName() + " AS " + TOTAL_POINTS_NAME + " " +
                "FROM " + ChatGameDB.statsTable.getName() + " AS s " +
                "JOIN " + ChatGameDB.playerTable.getName() + " AS p " +
                        "ON s." + ChatGameDB.uuidField.getFieldName() + " = p." + ChatGameDB.uuidField.getFieldName() + " " +
                        "AND s." + period.field.getFieldName() + " != 0 " +

                "WHERE s." + ChatGameDB.gameField.getFieldName() + " = ? " +
                "ORDER BY " + TOTAL_POINTS_NAME + " DESC " +
                "LIMIT " + topListSize
                ;

        WbsDatabase db = ChatGameDB.getDatabase();
        List<WbsRecord> selected;
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, game.getGameName());
            selected = db.select(statement);
        } catch (SQLException e) {
            WbsChatGame.getInstance().logger.severe("Failed to select from ");
            e.printStackTrace();
            return leaderboard;
        }

        for (WbsRecord record : selected) {
            LeaderboardEntry entry = new LeaderboardEntry(record, period, game);
            leaderboard.add(entry);
        }

        leaderboard.sort();

        Map<TrackedPeriod, Leaderboard> gamePeriods = gameTops.get(game);
        if (gamePeriods == null) {
            gamePeriods = new HashMap<>();
        }
        gamePeriods.put(period, leaderboard);
        gameTops.put(game, gamePeriods);
        return leaderboard;
    }

    public static Leaderboard getTop(TrackedPeriod period) {
        Leaderboard top = periodTops.get(period);

        if (top != null && !forceUpdate) {
            return top;
        }

        return recalculate(period);
    }

    public static int getTopAsync(TrackedPeriod period, Consumer<Leaderboard> callback) {
        Leaderboard top = periodTops.get(period);

        if (top != null && !forceUpdate) {
            callback.accept(top);
            return -1;
        }

        return WbsChatGame.getInstance().runAsync(
                () -> recalculate(period),
                () -> callback.accept(periodTops.get(period))
        );
    }

    public static Leaderboard getTop(Game game) {
        return getTop(TrackedPeriod.TOTAL, game);
    }

    public static Leaderboard getTop(TrackedPeriod period, Game game) {
        Map<TrackedPeriod, Leaderboard> gamePeriods = gameTops.get(game);

        if (gamePeriods != null) {
            Leaderboard top = gamePeriods.get(period);

            if (top != null && !forceUpdate) {
                return top;
            }
        }

        return recalculate(period);
    }

    public static int getTopAsync(Game game, Consumer<Leaderboard> callback) {
        return getTopAsync(TrackedPeriod.TOTAL, game, callback);
    }

    public static int getTopAsync(TrackedPeriod period, Game game, Consumer<Leaderboard> callback) {
        Map<TrackedPeriod, Leaderboard> gamePeriods = gameTops.get(game);

        if (gamePeriods != null) {
            Leaderboard top = gamePeriods.get(period);

            if (top != null && !forceUpdate) {
                callback.accept(top);
                return -1;
            }
        }

        return WbsChatGame.getInstance().runAsync(
                () -> recalculate(period, game),
                () -> callback.accept(getTop(period, game))
        );
    }
}

